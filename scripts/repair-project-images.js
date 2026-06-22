const fs = require("fs");
const path = require("path");
const crypto = require("crypto");

const ROOT = path.resolve(__dirname, "..");
const FIREBASE_CONFIG = JSON.parse(fs.readFileSync(path.join(ROOT, "app", "google-services.json"), "utf8"));
const STRINGS_XML = fs.readFileSync(path.join(ROOT, "app", "src", "main", "res", "values", "strings.xml"), "utf8");
const FIREBASE_PROJECT_ID = FIREBASE_CONFIG.project_info.project_id;
const DATABASE = "(default)";

const SUPABASE_URL = stringResource("supabase_url");
const SUPABASE_KEY = stringResource("supabase_publishable_key");
const SUPABASE_BUCKET = stringResource("supabase_storage_bucket");

main().catch((error) => {
  console.error(error.message || error);
  process.exit(1);
});

async function main() {
  const args = process.argv.slice(2);
  const applyIndex = args.indexOf("--apply");
  const mappingPath = applyIndex >= 0 ? args[applyIndex + 1] : "";

  const token = firebaseAccessToken();
  const projects = await listFirestoreDocuments(token, "proyectos");
  const imageDocs = await listFirestoreDocuments(token, "proyectos_imagenes");
  const storageObjects = await listSupabaseObjects("");
  const missingRemoteImages = projects.filter((project) => !field(project, "primaryImageUrl") && !field(project, "imageUrl"));

  console.log("Firebase project:", FIREBASE_PROJECT_ID);
  console.log("Firestore proyectos:", projects.length);
  console.log("Firestore proyectos_imagenes:", imageDocs.length);
  console.log("Supabase bucket:", SUPABASE_BUCKET);
  console.log("Supabase objetos reales:", storageObjects.length);
  console.log("Proyectos sin URL remota:", missingRemoteImages.length);
  for (const project of missingRemoteImages) {
    console.log("-", docId(project), "|", field(project, "nombre") || "(sin nombre)", "| fallback:", field(project, "imageKey") || field(project, "userImageKey") || "(sin fallback)");
  }

  if (applyIndex < 0) {
    console.log("");
    console.log("Modo auditoria solamente. Para reparar, crea un mapping JSON y ejecuta:");
    console.log("node scripts/repair-project-images.js --apply scripts/project-image-map.example.json");
    return;
  }
  if (!mappingPath) {
    throw new Error("Falta ruta del mapping JSON despues de --apply");
  }

  const mappings = normalizeMappings(JSON.parse(fs.readFileSync(path.resolve(ROOT, mappingPath), "utf8")));
  for (const mapping of mappings) {
    const projectId = sanitize(mapping.projectId);
    if (!projectId || mapping.files.length === 0) {
      console.log("Saltando mapping incompleto:", JSON.stringify(mapping));
      continue;
    }
    const uploads = [];
    for (const file of mapping.files) {
      uploads.push(await uploadSupabaseImage(projectId, path.resolve(ROOT, file)));
    }
    await patchProjectImages(token, projectId, uploads);
    console.log("Reparado:", projectId, "imagenes:", uploads.length);
  }
}

function stringResource(name) {
  const match = STRINGS_XML.match(new RegExp(`<string name="${name}">([\\s\\S]*?)<\\/string>`));
  if (!match) {
    throw new Error(`No se encontro string resource ${name}`);
  }
  return match[1].trim();
}

function firebaseAccessToken() {
  const configPath = path.join(process.env.USERPROFILE || process.env.HOME, ".config", "configstore", "firebase-tools.json");
  if (!fs.existsSync(configPath)) {
    throw new Error("No hay sesion Firebase CLI. Ejecuta: npx.cmd firebase-tools@latest login");
  }
  const config = JSON.parse(fs.readFileSync(configPath, "utf8"));
  const token = config.tokens?.access_token || config.access_token;
  if (!token) {
    throw new Error("No se encontro access_token Firebase CLI. Ejecuta: npx.cmd firebase-tools@latest login");
  }
  return token;
}

async function listFirestoreDocuments(token, collection) {
  const body = {
    structuredQuery: {
      from: [{ collectionId: collection }],
      limit: 500,
    },
  };
  const response = await fetch(
    `https://firestore.googleapis.com/v1/projects/${FIREBASE_PROJECT_ID}/databases/${DATABASE}/documents:runQuery`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body),
    }
  );
  await assertOk(response, `No se pudo leer Firestore ${collection}`);
  const rows = await response.json();
  return rows.map((row) => row.document).filter(Boolean);
}

async function listSupabaseObjects(prefix) {
  const response = await fetch(`${SUPABASE_URL}/storage/v1/object/list/${SUPABASE_BUCKET}`, {
    method: "POST",
    headers: supabaseHeaders(),
    body: JSON.stringify({
      prefix,
      limit: 1000,
      offset: 0,
      sortBy: { column: "created_at", order: "desc" },
    }),
  });
  await assertOk(response, `No se pudo listar Supabase prefix=${prefix}`);
  const entries = await response.json();
  const objects = [];
  for (const entry of entries) {
    const fullPath = prefix ? `${prefix}/${entry.name}` : entry.name;
    if (entry.id) {
      const size = entry.metadata?.size || 0;
      if (entry.name !== ".emptyFolderPlaceholder" && size > 0) {
        objects.push({
          path: fullPath,
          size,
          createdAt: entry.created_at,
          publicUrl: publicUrl(fullPath),
        });
      }
    } else if (entry.name && !entry.name.startsWith(".")) {
      objects.push(...await listSupabaseObjects(fullPath));
    }
  }
  return objects;
}

async function uploadSupabaseImage(projectId, filePath) {
  if (!fs.existsSync(filePath)) {
    throw new Error(`No existe archivo de imagen: ${filePath}`);
  }
  const storagePath = `projects/${sanitize(projectId)}/${crypto.randomUUID()}${extension(filePath)}`;
  const response = await fetch(`${SUPABASE_URL}/storage/v1/object/${SUPABASE_BUCKET}/${encodePath(storagePath)}`, {
    method: "POST",
    headers: {
      ...supabaseHeaders(),
      "Content-Type": mimeType(filePath),
      "Cache-Control": "3600",
      "x-upsert": "true",
    },
    body: fs.readFileSync(filePath),
  });
  await assertOk(response, `No se pudo subir ${filePath} a Supabase`);
  return {
    storagePath,
    publicUrl: publicUrl(storagePath),
  };
}

async function patchProjectImages(token, projectId, uploads) {
  const primary = uploads[0];
  const updateFields = {
    imageUrl: { stringValue: primary.publicUrl },
    primaryImageUrl: { stringValue: primary.publicUrl },
    imageStoragePath: { stringValue: primary.storagePath },
    imageProvider: { stringValue: "supabase" },
    updatedAt: { integerValue: String(Date.now()) },
  };
  await patchFirestoreDocument(token, `proyectos/${projectId}`, updateFields);

  for (let i = 0; i < uploads.length; i++) {
    const image = uploads[i];
    const imageId = `${projectId}_supabase_${Date.now()}_${i}`;
    await patchFirestoreDocument(token, `proyectos_imagenes/${imageId}`, {
      id: { stringValue: imageId },
      imageId: { stringValue: imageId },
      projectId: { stringValue: projectId },
      imageUrl: { stringValue: image.publicUrl },
      storagePath: { stringValue: image.storagePath },
      provider: { stringValue: "supabase" },
      createdAt: { integerValue: String(Date.now()) },
    });
  }
}

async function patchFirestoreDocument(token, documentPath, fields) {
  const masks = Object.keys(fields).map((field) => `updateMask.fieldPaths=${encodeURIComponent(field)}`).join("&");
  const response = await fetch(
    `https://firestore.googleapis.com/v1/projects/${FIREBASE_PROJECT_ID}/databases/${DATABASE}/documents/${documentPath}?${masks}`,
    {
      method: "PATCH",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ fields }),
    }
  );
  await assertOk(response, `No se pudo actualizar Firestore ${documentPath}`);
}

function normalizeMappings(value) {
  if (Array.isArray(value)) {
    return value.map((item) => ({ projectId: item.projectId, files: item.files || [] }));
  }
  return Object.keys(value).map((projectId) => ({ projectId, files: value[projectId] || [] }));
}

function supabaseHeaders() {
  return {
    apikey: SUPABASE_KEY,
    Authorization: `Bearer ${SUPABASE_KEY}`,
    "Content-Type": "application/json",
  };
}

async function assertOk(response, context) {
  if (response.ok) {
    return;
  }
  const text = await response.text();
  throw new Error(`${context}: HTTP ${response.status} ${text}`);
}

function field(document, name) {
  const value = document.fields?.[name];
  if (!value) {
    return "";
  }
  if (value.stringValue !== undefined) return value.stringValue;
  if (value.integerValue !== undefined) return value.integerValue;
  if (value.doubleValue !== undefined) return String(value.doubleValue);
  if (value.booleanValue !== undefined) return String(value.booleanValue);
  return "";
}

function docId(document) {
  return document.name.split("/").pop();
}

function publicUrl(storagePath) {
  return `${SUPABASE_URL}/storage/v1/object/public/${SUPABASE_BUCKET}/${encodePath(storagePath)}`;
}

function encodePath(value) {
  return value.split("/").map(encodeURIComponent).join("/");
}

function sanitize(value) {
  return String(value || "")
    .toLowerCase()
    .replace(/[^a-z0-9_-]+/g, "_")
    .replace(/^_+|_+$/g, "");
}

function extension(filePath) {
  const ext = path.extname(filePath).toLowerCase();
  return ext || ".jpg";
}

function mimeType(filePath) {
  const ext = extension(filePath);
  if (ext === ".png") return "image/png";
  if (ext === ".webp") return "image/webp";
  if (ext === ".gif") return "image/gif";
  return "image/jpeg";
}
