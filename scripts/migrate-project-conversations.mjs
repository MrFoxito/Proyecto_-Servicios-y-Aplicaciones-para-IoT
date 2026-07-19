import { applicationDefault, cert, getApps, initializeApp } from "firebase-admin/app";
import { FieldValue, getFirestore } from "firebase-admin/firestore";

const apply = process.argv.includes("--apply");
const serviceAccountPath = process.env.GOOGLE_APPLICATION_CREDENTIALS;

if (!getApps().length) {
  initializeApp({ credential: serviceAccountPath ? cert(serviceAccountPath) : applicationDefault() });
}

const db = getFirestore();

function text(value) {
  return typeof value === "string" ? value.trim() : "";
}

function projectIdFrom(data) {
  return text(data.projectId) || text(data.propertyId) || text(data.proyectoId);
}

function canonicalId(clienteUid, projectId) {
  return `project_chat_${clienteUid}_${projectId}`;
}

function timestampValue(value) {
  if (value?.toMillis) return value.toMillis();
  return Number(value) || 0;
}

async function resolveProjectId(conversation) {
  const direct = projectIdFrom(conversation.data());
  if (direct) return direct;
  const citaId = text(conversation.get("citaId"));
  if (!citaId) return "";
  const appointment = await db.collection("citas").doc(citaId).get();
  return appointment.exists ? projectIdFrom(appointment.data()) : "";
}

async function markLegacy(reference, reason, canonicalConversationId = "") {
  const values = {
    legacyState: canonicalConversationId ? "migrated" : "unlinked",
    legacyReason: reason,
    migratedAt: FieldValue.serverTimestamp(),
  };
  if (canonicalConversationId) values.canonicalConversationId = canonicalConversationId;
  if (apply) await reference.set(values, { merge: true });
}

async function migrateMessages(sourceId, targetId) {
  const messages = await db.collection("mensajes").where("conversationId", "==", sourceId).get();
  if (!apply || messages.empty || sourceId === targetId) return messages.size;
  const writer = db.bulkWriter();
  for (const message of messages.docs) {
    writer.update(message.ref, {
      conversationId: targetId,
      migratedFromConversationId: sourceId,
      migratedAt: FieldValue.serverTimestamp(),
    });
  }
  await writer.close();
  return messages.size;
}

async function main() {
  const conversations = await db.collection("conversaciones").get();
  const candidates = [];
  for (const conversation of conversations.docs) {
    const data = conversation.data();
    if (data.conversationType === "project" && data.schemaVersion === 2) continue;
    const clienteUid = text(data.clienteUid);
    const asesorUid = text(data.asesorUid);
    const participants = Array.isArray(data.participantUids) ? data.participantUids : [];
    const projectId = await resolveProjectId(conversation);
    if (!clienteUid || !asesorUid || !projectId
      || participants.length !== 2 || !participants.includes(clienteUid) || !participants.includes(asesorUid)) {
      await markLegacy(conversation.ref, "missing_or_ambiguous_project_context");
      continue;
    }
    candidates.push({ conversation, clienteUid, asesorUid, projectId, createdAt: timestampValue(data.createdAt) });
  }

  const groups = new Map();
  for (const candidate of candidates) {
    const key = `${candidate.clienteUid}\u0000${candidate.projectId}`;
    if (!groups.has(key)) groups.set(key, []);
    groups.get(key).push(candidate);
  }

  let migratedConversations = 0;
  let migratedMessages = 0;
  for (const group of groups.values()) {
    group.sort((left, right) => left.createdAt - right.createdAt || left.conversation.id.localeCompare(right.conversation.id));
    const owner = group[0];
    const targetId = canonicalId(owner.clienteUid, owner.projectId);
    const targetRef = db.collection("conversaciones").doc(targetId);
    const target = await targetRef.get();
    const targetAdvisor = target.exists ? text(target.get("asesorUid")) : owner.asesorUid;
    let targetInitialized = target.exists && target.get("conversationType") === "project" && target.get("schemaVersion") === 2;

    for (const candidate of group) {
      if (candidate.asesorUid !== targetAdvisor) {
        await markLegacy(candidate.conversation.ref, "different_original_advisor", targetId);
        continue;
      }
      const source = candidate.conversation.data();
      // El primer historial válido es la fuente de metadatos. Los historiales
      // posteriores únicamente aportan mensajes: así no se sobrescribe el
      // último mensaje, asesor original ni marca temporal del chat canónico.
      if (apply && !targetInitialized) {
        await targetRef.set({
          ...source,
          id: targetId,
          conversationType: "project",
          schemaVersion: 2,
          clienteUid: owner.clienteUid,
          asesorUid: targetAdvisor,
          participantUids: [owner.clienteUid, targetAdvisor],
          projectId: owner.projectId,
          projectName: text(source.projectName) || "Proyecto",
          updatedAt: FieldValue.serverTimestamp(),
        }, { merge: true });
        targetInitialized = true;
      }
      migratedMessages += await migrateMessages(candidate.conversation.id, targetId);
      if (candidate.conversation.id !== targetId) {
        await markLegacy(candidate.conversation.ref, "migrated_to_project_chat", targetId);
      }
      migratedConversations++;
    }
  }

  console.log(JSON.stringify({ mode: apply ? "apply" : "dry-run", migratedConversations, migratedMessages }, null, 2));
}

main().catch(error => {
  console.error(error);
  process.exitCode = 1;
});
