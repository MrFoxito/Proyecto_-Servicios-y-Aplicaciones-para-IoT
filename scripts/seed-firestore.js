const fs = require("fs");
const path = require("path");

const PROJECT_ID = "iot-g3-c3fa2";
const DATABASE_ID = "(default)";
const ROOT = path.resolve(__dirname, "..");
const FIREBASE_CONFIG = JSON.parse(fs.readFileSync(path.join(ROOT, "app", "google-services.json"), "utf8"));
const API_KEY = FIREBASE_CONFIG.client?.[0]?.api_key?.[0]?.current_key;
const CLI_CONFIG_PATH = path.join(process.env.USERPROFILE || process.env.HOME, ".config", "configstore", "firebase-tools.json");
const CLI_CONFIG = JSON.parse(fs.readFileSync(CLI_CONFIG_PATH, "utf8"));
const ACCESS_TOKEN = CLI_CONFIG.tokens?.access_token;

if (!API_KEY) {
  throw new Error("No se encontro API key en app/google-services.json");
}

if (!ACCESS_TOKEN) {
  throw new Error("No se encontro token del Firebase CLI. Ejecuta firebase login.");
}

const demoUsers = [
  {
    key: "superadmin",
    email: "superadmin@estate.pe",
    password: "super123",
    nombre: "Julian Reed",
    telefono: "",
    rol: "superadmin",
    estado: "activo",
    inmobiliariaNombre: "Sistema",
    avatarKey: "sa_avatar_07",
  },
  {
    key: "admin",
    email: "admin@editorialestate.com",
    password: "admin123",
    nombre: "Administrador Editorial",
    telefono: "+51 987 654 321",
    rol: "admin",
    estado: "activo",
    inmobiliariaId: "inmo_editorial",
    inmobiliariaNombre: "The Editorial Estate",
    avatarKey: "sa_profile_admin",
  },
  {
    key: "asesor",
    email: "evaldes@editorialestate.com",
    password: "asesor123",
    nombre: "Elena Valdes",
    telefono: "+51 987 111 222",
    rol: "asesor",
    estado: "activo",
    inmobiliariaId: "inmo_editorial",
    inmobiliariaNombre: "The Editorial Estate",
    avatarKey: "sa_profile_asesor_1",
    rating: "5.0",
  },
  {
    key: "cliente",
    email: "alicia.velarde@mail.com",
    password: "cliente123",
    nombre: "Alicia Velarde",
    telefono: "+51 987 456 210",
    rol: "cliente",
    estado: "activo",
    inmobiliariaNombre: "The Editorial Estate",
    avatarKey: "sa_profile_user_1",
  },
];

function splitName(nombre) {
  const parts = nombre.trim().split(/\s+/, 2);
  return {
    nombres: parts[0] || "",
    apellidos: parts[1] || "",
  };
}

async function identityToolkit(endpoint, body) {
  const response = await fetch(`https://identitytoolkit.googleapis.com/v1/${endpoint}?key=${API_KEY}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  const json = await response.json();
  if (!response.ok) {
    const code = json.error?.message || "UNKNOWN_AUTH_ERROR";
    throw new Error(code);
  }
  return json;
}

async function ensureAuthUser(user) {
  try {
    const created = await identityToolkit("accounts:signUp", {
      email: user.email,
      password: user.password,
      returnSecureToken: true,
    });
    return created.localId;
  } catch (error) {
    if (String(error.message).includes("EMAIL_EXISTS")) {
      const signedIn = await identityToolkit("accounts:signInWithPassword", {
        email: user.email,
        password: user.password,
        returnSecureToken: true,
      });
      return signedIn.localId;
    }
    throw error;
  }
}

function valueToFirestore(value) {
  if (value === null || value === undefined) {
    return { nullValue: null };
  }
  if (Array.isArray(value)) {
    return { arrayValue: { values: value.map(valueToFirestore) } };
  }
  if (typeof value === "boolean") {
    return { booleanValue: value };
  }
  if (Number.isInteger(value)) {
    return { integerValue: String(value) };
  }
  if (typeof value === "number") {
    return { doubleValue: value };
  }
  if (typeof value === "object") {
    const fields = {};
    for (const [key, child] of Object.entries(value)) {
      fields[key] = valueToFirestore(child);
    }
    return { mapValue: { fields } };
  }
  return { stringValue: String(value) };
}

function toFields(data) {
  const fields = {};
  for (const [key, value] of Object.entries(data)) {
    fields[key] = valueToFirestore(value);
  }
  return fields;
}

function docName(collection, id) {
  return `projects/${PROJECT_ID}/databases/${DATABASE_ID}/documents/${collection}/${id}`;
}

async function batchWrite(docs) {
  const writes = docs.map(({ collection, id, data }) => ({
    update: {
      name: docName(collection, id),
      fields: toFields(data),
    },
  }));

  const response = await fetch(
    `https://firestore.googleapis.com/v1/projects/${PROJECT_ID}/databases/${DATABASE_ID}/documents:batchWrite`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${ACCESS_TOKEN}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ writes }),
    }
  );
  const json = await response.json();
  if (!response.ok) {
    throw new Error(JSON.stringify(json, null, 2));
  }
  return json;
}

async function main() {
  const ids = {};
  let authCreated = true;
  for (const user of demoUsers) {
    try {
      ids[user.key] = await ensureAuthUser(user);
    } catch (error) {
      authCreated = false;
      ids[user.key] = `usr_${user.key}_001`;
      console.warn(`No se pudo crear Auth para ${user.email}: ${error.message}`);
    }
  }

  const adminId = ids.admin;
  const asesorId = ids.asesor;
  const clienteId = ids.cliente;

  const userDocs = demoUsers.map((user) => {
    const uid = ids[user.key];
    const name = splitName(user.nombre);
    return {
      collection: "usuarios",
      id: uid,
      data: {
        uid,
        id: uid,
        nombre: user.nombre,
        nombres: name.nombres,
        apellidos: name.apellidos,
        correo: user.email,
        email: user.email,
        telefono: user.telefono,
        rol: user.rol,
        estado: user.estado,
        inmobiliariaId: user.inmobiliariaId || "",
        inmobiliariaNombre: user.inmobiliariaNombre || "",
        avatarKey: user.avatarKey,
        rating: user.rating || "",
      },
    };
  });

  const docs = [
    ...userDocs,
    {
      collection: "proyectos",
      id: "proy_001",
      data: {
        id: "proy_001",
        projectId: "proy_001",
        adminId,
        nombre: "Villa Luminara",
        descripcion: "Proyecto residencial premium con amenidades urbanas.",
        direccion: "Av. Javier Prado 450, San Isidro",
        distrito: "San Isidro",
        estado: "EN PREVENTA",
        estadoComercial: "EN PREVENTA",
        fechaEntrega: "10/06/2025",
        precioDesde: "USD 1.2M",
        badge: "CURADURIA DESTACADA",
        imageKey: "sa_profile_admin",
        userImageKey: "user_featured_house",
        assignmentStatus: "ACTIVO",
      },
    },
    {
      collection: "proyectos",
      id: "proy_002",
      data: {
        id: "proy_002",
        projectId: "proy_002",
        adminId,
        nombre: "The Iron Works",
        descripcion: "Lofts modernos con acabados industriales.",
        direccion: "Calle Monte Real 210, Miraflores",
        distrito: "Miraflores",
        estado: "EN VENTA",
        estadoComercial: "EN VENTA",
        fechaEntrega: "20/09/2026",
        precioDesde: "USD 1.8M",
        badge: "LOFT INDUSTRIAL",
        imageKey: "sa_profile_admin",
        userImageKey: "user_popular_1",
        assignmentStatus: "EN CURSO",
      },
    },
    {
      collection: "proyectos_tipologias",
      id: "tip_proy_001_a",
      data: {
        id: "tip_proy_001_a",
        typologyId: "tip_proy_001_a",
        projectId: "proy_001",
        title: "Tipo A",
        nombre: "Tipo A",
        available: true,
        area: "70 m2",
        habitaciones: "2 habs",
        bedrooms: "2 habs",
        banos: "2 banos",
        bathrooms: "2 banos",
        montoTotal: "350,000 USD",
        totalAmount: "350,000 USD",
        montoSeparacion: "1,500 USD",
        separationAmount: "1,500 USD",
      },
    },
    {
      collection: "proyectos_tipologias",
      id: "tip_proy_001_b",
      data: {
        id: "tip_proy_001_b",
        typologyId: "tip_proy_001_b",
        projectId: "proy_001",
        title: "Tipo B",
        nombre: "Tipo B",
        available: true,
        area: "80 m2",
        habitaciones: "3 habs",
        bedrooms: "3 habs",
        banos: "2 banos",
        bathrooms: "2 banos",
        montoTotal: "400,000 USD",
        totalAmount: "400,000 USD",
        montoSeparacion: "1,700 USD",
        separationAmount: "1,700 USD",
      },
    },
    ...["Coworking", "Piscina", "Terraza", "Sala lounge", "Gimnasio", "Zona BBQ"].map((nombre, index) => ({
      collection: "proyectos_amenidades",
      id: `amen_proy_001_${index + 1}`,
      data: {
        id: `amen_proy_001_${index + 1}`,
        amenityId: `amen_proy_001_${index + 1}`,
        projectId: "proy_001",
        nombre,
        title: nombre,
        icono: nombre.toLowerCase().replace(/\s+/g, "_"),
        selected: true,
      },
    })),
    ...["sa_profile_admin", "user_featured_house", "user_popular_1"].map((imageKey, index) => ({
      collection: "proyectos_imagenes",
      id: `img_proy_001_${index + 1}`,
      data: {
        id: `img_proy_001_${index + 1}`,
        projectId: "proy_001",
        imageKey,
      },
    })),
    {
      collection: "asignaciones",
      id: "assignment_001",
      data: {
        assignmentId: "assignment_001",
        projectId: "proy_001",
        asesorId,
        adminId,
        fecha: "2026-06-08",
        estado: "activa",
      },
    },
    {
      collection: "solicitudes_asesor",
      id: "sol_001",
      data: {
        requestId: "sol_001",
        id: "sol_001",
        asesorId,
        adminId,
        nombre: "Elena Valdes",
        email: "evaldes@editorialestate.com",
        descripcion: "Solicita unirse a The Editorial Estate como asesora inmobiliaria.",
        estado: "pendiente",
        fecha: "2026-06-08",
      },
    },
    {
      collection: "citas",
      id: "cita_001",
      data: {
        citaId: "cita_001",
        id: "cita_001",
        clienteId,
        asesorId,
        projectId: "proy_001",
        fecha: "2026-06-10 10:00",
        estado: "pendiente",
      },
    },
    {
      collection: "eventos_cita",
      id: "evt_001",
      data: {
        id: "evt_001",
        citaId: "cita_001",
        titulo: "Cita agendada",
        detalle: "Agendada desde la app por el cliente",
        fechaHora: "2026-06-08T10:00",
        tipo: "AGENDADA",
      },
    },
    {
      collection: "separaciones",
      id: "sep_001",
      data: {
        separacionId: "sep_001",
        id: "sep_001",
        clienteId,
        asesorId,
        projectId: "proy_001",
        monto: "1,500 USD",
        estado: "Pendiente",
      },
    },
    {
      collection: "conversaciones",
      id: "conv_001",
      data: {
        conversationId: "conv_001",
        id: "conv_001",
        clienteId,
        asesorId,
        projectId: "proy_001",
        ultimoMensaje: "Hola, quiero informacion del proyecto.",
      },
    },
    {
      collection: "mensajes",
      id: "msg_001",
      data: {
        messageId: "msg_001",
        id: "msg_001",
        conversationId: "conv_001",
        senderId: clienteId,
        texto: "Hola, quiero informacion del proyecto.",
        fechaHora: "2026-06-08T10:05",
      },
    },
    {
      collection: "notificaciones",
      id: "notif_001",
      data: {
        notificationId: "notif_001",
        id: "notif_001",
        recipientId: clienteId,
        recipientRole: "cliente",
        tipo: "appointment",
        mensaje: "Tu cita para Villa Luminara fue registrada.",
        leida: false,
      },
    },
    {
      collection: "tramites",
      id: "tramite_001",
      data: {
        tramiteId: "tramite_001",
        id: "tramite_001",
        clienteId,
        projectId: "proy_001",
        estado: "pendiente",
        titulo: "Separacion de departamento",
      },
    },
    {
      collection: "historial_usuario",
      id: "hist_001",
      data: {
        historyId: "hist_001",
        id: "hist_001",
        clienteId,
        tipo: "registro",
        mensaje: "Usuario demo creado para presentacion.",
        fecha: "2026-06-08",
      },
    },
    {
      collection: "resenas",
      id: "res_001",
      data: {
        reviewId: "res_001",
        id: "res_001",
        clienteId,
        asesorId,
        proyectoNombre: "Villa Luminara",
        clienteNombre: "Alicia Velarde",
        comentario: "Buena atencion durante la visita.",
        rating: "5.0",
        fecha: "2026-06-08",
      },
    },
    {
      collection: "logs_sistema",
      id: "log_001",
      data: {
        logId: "log_001",
        id: "log_001",
        tipo: "seed",
        nivel: "info",
        titulo: "Base Firestore creada",
        resumen: "Colecciones iniciales cargadas por Firebase CLI.",
        tiempo: "2026-06-08",
      },
    },
    {
      collection: "app_meta",
      id: "schema_seed",
      data: {
        version: "firebase_schema_v1",
        seededAt: new Date().toISOString(),
        authUsersCreated: authCreated,
      },
    },
  ];

  await batchWrite(docs);
  console.log(`Firestore seed completado: ${docs.length} documentos.`);
  console.log("Usuarios demo:");
  for (const user of demoUsers) {
    console.log(`- ${user.rol}: ${user.email} / ${user.password}`);
  }
}

main().catch((error) => {
  console.error(error.message);
  process.exit(1);
});
