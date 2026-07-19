/*
 * Normalizes historical appointments before enabling customer cancellation.
 * Default is dry-run. Use --apply only after reviewing the emitted report.
 * Authentication uses Firebase CLI ADC (gcloud application-default login) or
 * FIREBASE_ACCESS_TOKEN; it never uses the Android application's credentials.
 */
import { execFileSync } from "node:child_process";

const projectId = process.env.FIREBASE_PROJECT_ID || "iot-g3-c3fa2";
const apply = process.argv.includes("--apply");
const database = "(default)";
const documentRoot = `projects/${projectId}/databases/${database}/documents`;
const root = `https://firestore.googleapis.com/v1/${documentRoot}`;

function token() {
  if (process.env.FIREBASE_ACCESS_TOKEN) return process.env.FIREBASE_ACCESS_TOKEN;
  try {
    return execFileSync("gcloud", ["auth", "application-default", "print-access-token"], { encoding: "utf8" }).trim();
  } catch {
    throw new Error("No hay credenciales administrativas. Define FIREBASE_ACCESS_TOKEN o instala gcloud y ejecuta 'gcloud auth application-default login'.");
  }
}

async function request(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: { Authorization: `Bearer ${token()}`, "Content-Type": "application/json", ...(options.headers || {}) },
  });
  if (!response.ok) throw new Error(`${response.status} ${await response.text()}`);
  return response.status === 204 ? null : response.json();
}

function decode(value) {
  if (!value) return null;
  if ("stringValue" in value) return value.stringValue;
  if ("integerValue" in value) return Number(value.integerValue);
  if ("doubleValue" in value) return value.doubleValue;
  if ("booleanValue" in value) return value.booleanValue;
  if ("arrayValue" in value) return (value.arrayValue.values || []).map(decode);
  if ("mapValue" in value) return Object.fromEntries(Object.entries(value.mapValue.fields || {}).map(([key, item]) => [key, decode(item)]));
  return null;
}

function encode(value) {
  if (Array.isArray(value)) return { arrayValue: { values: value.map(encode) } };
  if (typeof value === "number") return Number.isInteger(value) ? { integerValue: String(value) } : { doubleValue: value };
  if (typeof value === "boolean") return { booleanValue: value };
  return { stringValue: String(value ?? "") };
}

function first(...values) {
  return values.find(value => typeof value === "string" && value.trim())?.trim() || "";
}

function slotKey(hora) {
  const match = first(hora).match(/^(\d{1,2})[:_](\d{2})/);
  return match ? `${match[1].padStart(2, "0")}_${match[2]}` : "";
}

function appointmentFrom(document) {
  const data = Object.fromEntries(Object.entries(document.fields || {}).map(([key, value]) => [key, decode(value)]));
  const id = document.name.split("/").pop();
  const clienteId = first(data.clienteId, data.clientId, data.clienteUid, data.uidCliente);
  const asesorId = first(data.asesorId, data.advisorId, data.asesorUid, data.uidAsesor);
  const propertyId = first(data.propertyId, data.projectId, data.proyectoId);
  const fechaISO = first(data.fechaISO, data.fecha);
  const normalizedSlotKey = first(data.slotKey, slotKey(data.hora));
  const estado = first(data.estado, "Confirmada");
  return { id, name: document.name, data, clienteId, asesorId, propertyId, fechaISO, slotKey: normalizedSlotKey, estado,
    slotId: first(data.slotId, `${asesorId}_${fechaISO}_${normalizedSlotKey}`) };
}

async function listAppointments() {
  const documents = [];
  let pageToken = "";
  do {
    const result = await request(`${root}/citas?pageSize=300${pageToken ? `&pageToken=${encodeURIComponent(pageToken)}` : ""}`);
    documents.push(...(result.documents || []));
    pageToken = result.nextPageToken || "";
  } while (pageToken);
  return documents.map(appointmentFrom);
}

async function documentExists(path) {
  try { return await request(`${root}/${path}`); } catch (error) {
    if (String(error.message).startsWith("404")) return null;
    throw error;
  }
}

function canonicalFields(item) {
  return {
    id: item.id, clienteId: item.clienteId, asesorId: item.asesorId,
    propertyId: item.propertyId, projectId: item.propertyId, proyectoId: item.propertyId,
    fechaISO: item.fechaISO, slotKey: item.slotKey, slotId: item.slotId,
    participantUids: [item.clienteId, item.asesorId],
  };
}

async function buildPlan() {
  const all = await listAppointments();
  const candidates = all.filter(item => /^(confirmada|reprogramada)$/i.test(item.estado));
  const conflicts = [];
  const bySlot = new Map();
  for (const item of candidates) {
    if (!item.clienteId || !item.asesorId || !item.propertyId || !/^\d{4}-\d{2}-\d{2}$/.test(item.fechaISO) || !item.slotKey) {
      conflicts.push({ citaId: item.id, reason: "Campos obligatorios incompletos para crear slot seguro." });
      continue;
    }
    bySlot.set(item.slotId, [...(bySlot.get(item.slotId) || []), item]);
  }

  const safe = [];
  for (const [id, items] of bySlot) {
    if (items.length !== 1) {
      items.forEach(item => conflicts.push({ citaId: item.id, reason: `Horario duplicado: ${id}.` }));
      continue;
    }
    const item = items[0];
    const existingSlot = await documentExists(`citas_slots/${id}`);
    if (existingSlot) {
      const slot = Object.fromEntries(Object.entries(existingSlot.fields || {}).map(([key, value]) => [key, decode(value)]));
      const sameAppointment = slot.citaId === item.id || (slot.citaIds || []).includes(item.id);
      if (!sameAppointment) {
        conflicts.push({ citaId: item.id, reason: `El slot ${id} ya pertenece a otra cita.` });
        continue;
      }
    }
    safe.push({ item, existingSlot });
  }
  return { scanned: all.length, active: candidates.length, safe, conflicts };
}

function migrationWrites(entry) {
  const { item, existingSlot } = entry;
  const lockId = `client_${item.clienteId}_${item.fechaISO}_${item.slotKey}`;
  const writes = [{ update: { name: item.name, fields: Object.fromEntries(Object.entries(canonicalFields(item)).map(([key, value]) => [key, encode(value)])) },
    updateMask: { fieldPaths: Object.keys(canonicalFields(item)) } }];
  if (!existingSlot) {
    const slotFields = {
      id: item.slotId, citaId: item.id, citaIds: [item.id], clienteId: item.clienteId, clientIds: [item.clienteId],
      asesorId: item.asesorId, propertyId: item.propertyId, fechaISO: item.fechaISO,
      hora: first(item.data.hora, item.slotKey.replace("_", ":")), slotKey: item.slotKey,
      capacidadMaxima: 1, reservedCount: 1, estado: "ocupado", participantUids: [item.clienteId, item.asesorId],
      createdAt: Date.now(), updatedAt: Date.now(),
    };
    writes.push({ update: { name: `${documentRoot}/citas_slots/${item.slotId}`, fields: Object.fromEntries(Object.entries(slotFields).map(([key, value]) => [key, encode(value)])) } });
  }
  writes.push({ update: { name: `${documentRoot}/cliente_citas_slots/${lockId}`, fields: Object.fromEntries(Object.entries({
    citaId: item.id, clienteId: item.clienteId, asesorId: item.asesorId, propertyId: item.propertyId,
    fechaISO: item.fechaISO, slotKey: item.slotKey, createdAt: Date.now(),
  }).map(([key, value]) => [key, encode(value)])) } });
  return writes;
}

const plan = await buildPlan();
console.log(JSON.stringify({ projectId, mode: apply ? "apply" : "dry-run", scanned: plan.scanned,
  active: plan.active, ready: plan.safe.map(entry => entry.item.id), conflicts: plan.conflicts }, null, 2));
if (!apply) process.exit(0);

for (let index = 0; index < plan.safe.length; index += 120) {
  const writes = plan.safe.slice(index, index + 120).flatMap(migrationWrites);
  await request(`https://firestore.googleapis.com/v1/projects/${projectId}/databases/${database}/documents:commit`, {
    method: "POST", body: JSON.stringify({ writes }),
  });
}
console.log(`Migración completada: ${plan.safe.length} citas normalizadas; ${plan.conflicts.length} conflictos sin modificar.`);
