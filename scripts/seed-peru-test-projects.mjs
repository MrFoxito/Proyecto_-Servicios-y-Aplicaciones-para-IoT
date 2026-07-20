/*
 * Seeds exactly ten fictional Peruvian projects using the same Firestore fields
 * written by FirebaseDataRepository. Dry-run is the default; production writes
 * require --apply --confirm-production and an explicit service-account path.
 */
import { existsSync } from "node:fs";
import { mkdir, readFile, writeFile } from "node:fs/promises";
import path from "node:path";
import process from "node:process";
import { fileURLToPath } from "node:url";
import { applicationDefault, getApps, initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";

const PRODUCTION_PROJECT_ID = "iot-g3-c3fa2";
const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const DEFAULT_MANIFEST = path.join(ROOT, "tmp", "peru-test-projects-manifest.json");
const REQUIRED_PROJECT_FIELDS = [
  "id", "projectId", "propertyId", "adminId", "empresaId", "inmobiliariaId",
  "nombre", "descripcion", "direccion", "distrito", "mapa", "estado",
  "estadoComercial", "estadoProyecto", "estadoProyectoLabel", "precioDesde",
  "currency", "badge", "lat", "lng", "ubicacion", "puntosInteres", "qrValue",
  "deepLink", "fechaEntrega", "fechaEntregaEstimada", "fechaEntregaISO",
  "fechaEntregaMillis", "deliveryReminderSent", "assignmentStatus", "createdAt", "updatedAt",
];
const REQUIRED_TYPOLOGY_FIELDS = [
  "id", "typologyId", "projectId", "title", "nombre", "available", "area",
  "habitaciones", "bedrooms", "banos", "bathrooms", "montoTotal", "totalAmount",
  "montoTotalLabel", "totalAmountLabel", "montoSeparacion", "separationAmount",
  "montoSeparacionLabel", "separationAmountLabel", "currency",
];
const REQUIRED_AMENITY_FIELDS = ["id", "amenityId", "projectId", "nombre", "title", "iconKey", "selected"];

const PROJECT_TEMPLATES = [
  ["prueba_peru_miraflores_01", "Altura del Parque", "Proyecto residencial ficticio de departamentos contemporáneos, pensado para una vida urbana cerca de áreas verdes.", "Av. del Horizonte 480, Miraflores, Lima", "Miraflores", -12.1213, -77.0291, "En venta", "15/12/2027", 468000, 18000, "72 m2", "2 habitaciones", "2 banos", "Terraza"],
  ["prueba_peru_barranco_02", "Lumen Barranco", "Edificio ficticio de diseño sobrio con espacios luminosos y acceso cercano a la vida cultural del distrito.", "Calle Puente de Piedra 214, Barranco, Lima", "Barranco", -12.1456, -77.0201, "En preventa", "30/06/2028", 512000, 20000, "78 m2", "2 habitaciones", "2 banos", "Sala de usos múltiples"],
  ["prueba_peru_san_isidro_03", "Arboleda Ejecutiva", "Proyecto ficticio orientado a familias y profesionales que buscan conectividad, áreas verdes y acabados contemporáneos.", "Calle Los Olivos 725, San Isidro, Lima", "San Isidro", -12.0964, -77.0369, "En venta", "20/11/2027", 785000, 30000, "95 m2", "3 habitaciones", "2 banos", "Gimnasio"],
  ["prueba_peru_surco_04", "Valle Claro Surco", "Condominio ficticio con distribución funcional, iluminación natural y zonas comunes para el día a día familiar.", "Av. Alameda Sur 1260, Santiago de Surco, Lima", "Santiago de Surco", -12.1404, -76.9871, "En planos", "18/09/2028", 438000, 16000, "68 m2", "2 habitaciones", "2 banos", "Juegos para niños"],
  ["prueba_peru_molina_05", "Jardines de Montalvo", "Proyecto ficticio de baja densidad con departamentos amplios y un entorno residencial tranquilo.", "Calle Las Acacias 340, La Molina, Lima", "La Molina", -12.0837, -76.946, "En preventa", "28/02/2028", 624000, 24000, "88 m2", "3 habitaciones", "2 banos", "Zona de parrillas"],
  ["prueba_peru_magdalena_06", "Costa Serena Magdalena", "Edificio ficticio de líneas modernas con departamentos prácticos y conexión rápida hacia la costa limeña.", "Jr. Mar Azul 610, Magdalena del Mar, Lima", "Magdalena del Mar", -12.0905, -77.0733, "En venta", "10/08/2027", 496000, 19000, "74 m2", "2 habitaciones", "2 banos", "Bicicletero"],
  ["prueba_peru_jesus_maria_07", "Nexo Central", "Proyecto ficticio cerca de servicios, parques y vías principales, diseñado para una vida conectada en Lima.", "Av. Los Próceres 530, Jesús María, Lima", "Jesús María", -12.0788, -77.0491, "En planos", "15/03/2028", 452000, 17000, "70 m2", "2 habitaciones", "2 banos", "Coworking"],
  ["prueba_peru_san_miguel_08", "Brisa del Pacífico", "Proyecto ficticio de departamentos eficientes con espacios comunes para disfrutar la cercanía al litoral.", "Calle Costa Norte 285, San Miguel, Lima", "San Miguel", -12.0771, -77.0924, "En preventa", "22/07/2028", 415000, 15000, "64 m2", "2 habitaciones", "2 banos", "Piscina"],
  ["prueba_peru_arequipa_09", "Sillar Vivo", "Proyecto ficticio arequipeño de arquitectura contemporánea con ambientes cómodos y una ubicación residencial.", "Calle Mirador del Sur 190, Yanahuara, Arequipa", "Arequipa", -16.3989, -71.5369, "En venta", "05/10/2027", 378000, 14000, "76 m2", "3 habitaciones", "2 banos", "Lobby"],
  ["prueba_peru_trujillo_10", "Sol de Chan Chan", "Condominio ficticio trujillano con departamentos funcionales, áreas compartidas y fácil conexión urbana.", "Av. Palmeras del Norte 450, Víctor Larco, Trujillo", "Trujillo", -8.1116, -79.0287, "En planos", "12/12/2028", 342000, 12000, "69 m2", "2 habitaciones", "2 banos", "Estacionamiento de visitas"],
];

function valueFor(flag) { const index = process.argv.indexOf(flag); return index >= 0 ? process.argv[index + 1] || "" : ""; }
function has(flag) { return process.argv.includes(flag); }
function required(flag) { const value = valueFor(flag).trim(); if (!value) throw new Error(`Falta ${flag}.`); return value; }

function formatDate(date) {
  const [day, month, year] = date.split("/");
  if (!/^\d{2}\/\d{2}\/\d{4}$/.test(date)) throw new Error(`Fecha inválida: ${date}`);
  // FirebaseDataRepository stores the local calendar date used in Peru (UTC-5), not a Timestamp.
  return { display: date, iso: `${year}-${month}-${day}`, millis: Date.UTC(Number(year), Number(month) - 1, Number(day)) + (5 * 60 * 60 * 1000) };
}

function statusFields(status) {
  const map = { "En planos": ["en_planos", "EN PLANOS"], "En preventa": ["preventa", "EN PREVENTA"], "En venta": ["venta", "EN VENTA"] };
  if (!map[status]) throw new Error(`Estado no permitido: ${status}`);
  return { estadoProyecto: map[status][0], estadoProyectoLabel: map[status][1] };
}

function iconKey(value) { return value.toLowerCase().replaceAll(/[^a-z0-9]+/g, "_").replaceAll(/^_+|_+$/g, ""); }

function assertExactFields(data, expected, label) {
  const actual = Object.keys(data).sort(); const required = [...expected].sort();
  const missing = required.filter(key => !actual.includes(key)); const extra = actual.filter(key => !required.includes(key));
  if (missing.length || extra.length) throw new Error(`${label}: campos incompatibles. Faltan: ${missing.join(", ") || "ninguno"}; adicionales: ${extra.join(", ") || "ninguno"}.`);
}

function assertProjectTypes(project) {
  const stringFields = REQUIRED_PROJECT_FIELDS.filter(field => !["lat", "lng", "fechaEntregaMillis", "deliveryReminderSent", "puntosInteres", "ubicacion", "createdAt", "updatedAt"].includes(field));
  stringFields.forEach(field => { if (typeof project[field] !== "string" || !project[field].trim()) throw new Error(`proyecto.${field} debe ser string no vacío.`); });
  ["lat", "lng", "fechaEntregaMillis", "createdAt", "updatedAt"].forEach(field => { if (typeof project[field] !== "number" || !Number.isFinite(project[field])) throw new Error(`proyecto.${field} debe ser number.`); });
  if (typeof project.deliveryReminderSent !== "boolean") throw new Error("proyecto.deliveryReminderSent debe ser boolean.");
  if (!Array.isArray(project.puntosInteres)) throw new Error("proyecto.puntosInteres debe ser arreglo.");
  if (!project.ubicacion || typeof project.ubicacion !== "object" || Array.isArray(project.ubicacion)) throw new Error("proyecto.ubicacion debe ser mapa.");
  if (project.ubicacion.direccion !== project.direccion || project.ubicacion.distrito !== project.distrito || project.ubicacion.lat !== project.lat || project.ubicacion.lng !== project.lng) throw new Error("proyecto.ubicacion no coincide con los campos principales.");
  if (!/^\d{4}-\d{2}-\d{2}$/.test(project.fechaEntregaISO) || !/^\d{2}\/\d{2}\/\d{4}$/.test(project.fechaEntrega)) throw new Error("Fechas de proyecto inválidas.");
  if (project.qrValue !== `app://proyecto/${project.id}` || project.deepLink !== project.qrValue) throw new Error("QR o deepLink no canónico.");
  if (project.id !== project.projectId || project.id !== project.propertyId) throw new Error("IDs de proyecto no sincronizados.");
  if (!["en_planos", "preventa", "venta"].includes(project.estadoProyecto)) throw new Error("estadoProyecto inválido.");
  if (project.currency !== "PEN" || project.assignmentStatus !== "ACTIVO") throw new Error("Moneda o estado de asignación inválido.");
}

function assertChildTypes(typology, amenity) {
  ["id", "typologyId", "projectId", "title", "nombre", "area", "habitaciones", "bedrooms", "banos", "bathrooms", "montoTotalLabel", "totalAmountLabel", "montoSeparacionLabel", "separationAmountLabel", "currency"].forEach(field => { if (typeof typology[field] !== "string" || !typology[field]) throw new Error(`tipología.${field} debe ser string.`); });
  ["montoTotal", "totalAmount", "montoSeparacion", "separationAmount"].forEach(field => { if (typeof typology[field] !== "number" || !Number.isFinite(typology[field])) throw new Error(`tipología.${field} debe ser number.`); });
  if (typeof typology.available !== "boolean" || typology.currency !== "PEN") throw new Error("Tipología inválida.");
  ["id", "amenityId", "projectId", "nombre", "title", "iconKey"].forEach(field => { if (typeof amenity[field] !== "string" || !amenity[field]) throw new Error(`amenidad.${field} debe ser string.`); });
  if (typeof amenity.selected !== "boolean") throw new Error("amenidad.selected debe ser boolean.");
}

function buildDocuments(adminId, empresaId) {
  const now = Date.now();
  return PROJECT_TEMPLATES.map(([id, nombre, descripcion, direccion, distrito, lat, lng, estado, fecha, totalAmount, separationAmount, area, bedrooms, bathrooms, amenityTitle], index) => {
    const { display, iso, millis } = formatDate(fecha); const { estadoProyecto, estadoProyectoLabel } = statusFields(estado);
    const typologyId = `${id}_tipologia_1`; const amenityId = `${id}_amenidad_1`; const totalLabel = `S/ ${totalAmount}`; const separationLabel = `S/ ${separationAmount}`;
    const project = { id, projectId: id, propertyId: id, adminId, empresaId, inmobiliariaId: empresaId, nombre, descripcion, direccion, distrito, mapa: `Ubicación seleccionada | Lat ${lat.toFixed(5)}, Lng ${lng.toFixed(5)}`, estado, estadoComercial: estado, estadoProyecto, estadoProyectoLabel, precioDesde: totalLabel, currency: "PEN", badge: estadoProyectoLabel, lat, lng, ubicacion: { direccion, distrito, lat, lng }, puntosInteres: [], qrValue: `app://proyecto/${id}`, deepLink: `app://proyecto/${id}`, fechaEntrega: display, fechaEntregaEstimada: display, fechaEntregaISO: iso, fechaEntregaMillis: millis, deliveryReminderSent: false, assignmentStatus: "ACTIVO", createdAt: now + index, updatedAt: now + index };
    const typology = { id: typologyId, typologyId, projectId: id, title: "Departamento", nombre: "Departamento", available: true, area, habitaciones: bedrooms, bedrooms, banos: bathrooms, bathrooms, montoTotal: totalAmount, totalAmount, montoTotalLabel: totalLabel, totalAmountLabel: totalLabel, montoSeparacion: separationAmount, separationAmount, montoSeparacionLabel: separationLabel, separationAmountLabel: separationLabel, currency: "PEN" };
    const amenity = { id: amenityId, amenityId, projectId: id, nombre: amenityTitle, title: amenityTitle, iconKey: iconKey(amenityTitle), selected: true };
    assertExactFields(project, REQUIRED_PROJECT_FIELDS, `Proyecto ${id}`); assertExactFields(typology, REQUIRED_TYPOLOGY_FIELDS, `Tipología ${typologyId}`); assertExactFields(amenity, REQUIRED_AMENITY_FIELDS, `Amenidad ${amenityId}`); assertProjectTypes(project); assertChildTypes(typology, amenity);
    return { id, typologyId, amenityId, project, typology, amenity };
  });
}

function startFirestore() {
  const credentialPath = process.env.GOOGLE_APPLICATION_CREDENTIALS || "";
  if (!credentialPath || !existsSync(credentialPath)) throw new Error("Define GOOGLE_APPLICATION_CREDENTIALS con la ruta a una cuenta de servicio autorizada para iot-g3-c3fa2.");
  const app = getApps().length ? getApps()[0] : initializeApp({ credential: applicationDefault(), projectId: PRODUCTION_PROJECT_ID });
  if (app.options.projectId !== PRODUCTION_PROJECT_ID) throw new Error("La credencial no está configurada para el proyecto de producción esperado.");
  return getFirestore(app);
}

function schemaSummary(documents) {
  const fields = {};
  documents.forEach(document => Object.entries(document.data()).forEach(([field, value]) => { const type = Array.isArray(value) ? "array" : value === null ? "null" : typeof value; fields[field] = fields[field] || { count: 0, types: new Set() }; fields[field].count += 1; fields[field].types.add(type); }));
  return Object.fromEntries(Object.entries(fields).sort().map(([field, value]) => [field, { count: value.count, types: [...value.types].sort() }]));
}

async function validateReferences(db, adminId, empresaId) {
  const [adminSnapshot, empresaSnapshot, sampleSnapshot] = await Promise.all([db.collection("usuarios").doc(adminId).get(), db.collection("empresas").doc(empresaId).get(), db.collection("proyectos").limit(10).get()]);
  if (!adminSnapshot.exists) throw new Error(`No existe usuarios/${adminId}.`); if (!empresaSnapshot.exists) throw new Error(`No existe empresas/${empresaId}.`);
  const admin = adminSnapshot.data(); if (!["admin", "superadmin"].includes(admin.rol)) throw new Error("El usuario de referencia debe tener rol admin o superadmin.");
  const linkedEmpresa = admin.empresaId || admin.inmobiliariaId || ""; if (admin.rol === "admin" && linkedEmpresa !== empresaId) throw new Error("La empresa indicada no coincide con el administrador indicado.");
  if (sampleSnapshot.empty) throw new Error("No se encontraron proyectos existentes para validar el esquema real de producción.");
  return { admin, empresa: empresaSnapshot.data(), schema: schemaSummary(sampleSnapshot.docs), sampleCount: sampleSnapshot.size };
}

async function ensureNoDuplicates(db, plan) {
  const snapshots = await db.getAll(...plan.map(item => db.collection("proyectos").doc(item.id)));
  const duplicates = snapshots.filter(snapshot => snapshot.exists).map(snapshot => snapshot.id);
  if (duplicates.length) throw new Error(`Ya existen IDs de prueba: ${duplicates.join(", ")}. No se sobrescribió nada.`);
}

async function writeManifest(manifestPath, manifest) { await mkdir(path.dirname(manifestPath), { recursive: true }); await writeFile(manifestPath, JSON.stringify(manifest, null, 2), "utf8"); }

async function applySeed(db, plan, adminId, empresaId, manifestPath) {
  const batch = db.batch();
  plan.forEach(item => { batch.create(db.collection("proyectos").doc(item.id), item.project); batch.create(db.collection("proyectos_tipologias").doc(item.typologyId), item.typology); batch.create(db.collection("proyectos_amenidades").doc(item.amenityId), item.amenity); });
  await batch.commit();
  const created = await db.getAll(...plan.map(item => db.collection("proyectos").doc(item.id))); if (created.some(snapshot => !snapshot.exists)) throw new Error("La escritura terminó sin crear todos los proyectos esperados.");
  const manifest = { projectId: PRODUCTION_PROJECT_ID, adminId, empresaId, createdAt: new Date().toISOString(), projectIds: plan.map(item => item.id), typologyIds: plan.map(item => item.typologyId), amenityIds: plan.map(item => item.amenityId) };
  await writeManifest(manifestPath, manifest); return manifest;
}

async function revertSeed(db, manifestPath) {
  const manifest = JSON.parse(await readFile(manifestPath, "utf8")); if (manifest.projectId !== PRODUCTION_PROJECT_ID) throw new Error("El manifiesto no pertenece al proyecto de producción esperado.");
  const projectSnapshots = await db.getAll(...manifest.projectIds.map(id => db.collection("proyectos").doc(id)));
  for (const snapshot of projectSnapshots) { if (!snapshot.exists) continue; const data = snapshot.data(); if (data.id !== snapshot.id || data.projectId !== snapshot.id || data.propertyId !== snapshot.id || data.adminId !== manifest.adminId || data.empresaId !== manifest.empresaId) throw new Error(`No se revierte ${snapshot.id}: ya no coincide con el manifiesto de prueba.`); }
  const batch = db.batch(); manifest.projectIds.forEach(id => batch.delete(db.collection("proyectos").doc(id))); manifest.typologyIds.forEach(id => batch.delete(db.collection("proyectos_tipologias").doc(id))); manifest.amenityIds.forEach(id => batch.delete(db.collection("proyectos_amenidades").doc(id))); await batch.commit(); return manifest;
}

async function main() {
  const apply = has("--apply"); const revert = has("--revert"); const manifestPath = path.resolve(valueFor("--manifest") || DEFAULT_MANIFEST); const requestedProject = valueFor("--project") || PRODUCTION_PROJECT_ID;
  if (requestedProject !== PRODUCTION_PROJECT_ID) throw new Error(`El script solo permite ${PRODUCTION_PROJECT_ID}.`); if (apply && !has("--confirm-production")) throw new Error("Para escribir o revertir en producción debes añadir --confirm-production.");
  const db = startFirestore();
  if (revert) {
    if (!apply) { const manifest = JSON.parse(await readFile(manifestPath, "utf8")); console.log(JSON.stringify({ mode: "dry-run-revert", manifestPath, projectIds: manifest.projectIds, typologyIds: manifest.typologyIds, amenityIds: manifest.amenityIds }, null, 2)); return; }
    const manifest = await revertSeed(db, manifestPath); console.log(JSON.stringify({ mode: "reverted", manifestPath, removedProjects: manifest.projectIds }, null, 2)); return;
  }
  const adminId = required("--admin-id"); const empresaId = required("--empresa-id"); const plan = buildDocuments(adminId, empresaId); const references = await validateReferences(db, adminId, empresaId); await ensureNoDuplicates(db, plan);
  const report = { mode: apply ? "apply" : "dry-run", projectId: PRODUCTION_PROJECT_ID, adminId, empresaId, verifiedReferences: { adminRole: references.admin.rol, empresaNombre: references.empresa.nombre || "", sampledProjects: references.sampleCount }, observedProjectSchema: references.schema, generatedProjectSchema: REQUIRED_PROJECT_FIELDS, imagePolicy: "Sin campos de imagen ni proyectos_imagenes; la app usa su placeholder existente.", projectIds: plan.map(item => item.id), typologyIds: plan.map(item => item.typologyId), amenityIds: plan.map(item => item.amenityId), example: plan[0].project, pendingWrites: plan.length * 3, manifestPath };
  console.log(JSON.stringify(report, null, 2)); if (!apply) return;
  const manifest = await applySeed(db, plan, adminId, empresaId, manifestPath); console.log(JSON.stringify({ mode: "inserted", insertedProjects: manifest.projectIds, manifestPath }, null, 2));
}

main().catch(error => { console.error(`ERROR: ${error.message}`); process.exitCode = 1; });
