import { initializeApp } from "firebase-admin/app";
import { FieldPath, FieldValue, Timestamp, getFirestore } from "firebase-admin/firestore";
import { HttpsError, onCall } from "firebase-functions/v2/https";
import { onDocumentWritten } from "firebase-functions/v2/firestore";
import { onSchedule } from "firebase-functions/v2/scheduler";

initializeApp();
const db = getFirestore();
const HOLD_MS = 24 * 60 * 60 * 1000;
const ACTIVE_LOCK_STATES = new Set([
  "RETENIDA_TEMPORALMENTE",
  "PAGO_EN_VERIFICACION",
  "SEPARACION_CONFIRMADA"
]);

function text(value) {
  return typeof value === "string" ? value.trim() : "";
}

function requiredText(value, field) {
  const result = text(value);
  if (!result) throw new HttpsError("invalid-argument", `Falta ${field}.`);
  return result;
}

function projectIdOf(data = {}) {
  return text(data.propertyId) || text(data.projectId) || text(data.proyectoId);
}

function advisorIdOf(data = {}) {
  return text(data.asesorId) || text(data.advisorId);
}

function lockId(projectId, typologyId) {
  return `${projectId}__${typologyId}`.replaceAll("/", "_");
}

function number(value) {
  return typeof value === "number" && Number.isFinite(value) ? value : 0;
}

function amountFrom(data = {}, numeric, legacy) {
  const direct = number(data[numeric]) || number(data[legacy]);
  if (direct > 0) return direct;
  const raw = text(data[`${numeric}Label`]) || text(data[`${legacy}Label`]);
  const normalized = raw.replace(/[^0-9,.-]/g, "").replace(/,/g, "");
  const parsed = Number.parseFloat(normalized);
  return Number.isFinite(parsed) ? parsed : 0;
}

function isActiveAssignment(data, projectId, advisorId) {
  return text(data.estado).toUpperCase() === "ACTIVO"
    && projectIdOf(data) === projectId
    && advisorIdOf(data) === advisorId;
}

function activeLock(lock, now) {
  const status = text(lock.estadoDisponibilidad);
  const expiration = lock.expiresAt instanceof Timestamp ? lock.expiresAt.toMillis() : 0;
  if (status === "PAGO_EN_VERIFICACION" || status === "SEPARACION_CONFIRMADA") return true;
  return status === "RETENIDA_TEMPORALMENTE" && expiration > now.toMillis();
}

function projectAllowsSeparation(project) {
  const status = text(project.estadoProyecto || project.estadoComercial || project.estado).toLowerCase();
  return status !== "planos" && status !== "en planos";
}

async function clientProfile(uid) {
  const snapshot = await db.collection("usuarios").doc(uid).get();
  const data = snapshot.exists ? snapshot.data() || {} : {};
  if (text(data.rol).toLowerCase() !== "cliente" || text(data.estado).toLowerCase() !== "activo") {
    throw new HttpsError("permission-denied", "Solo un cliente activo puede separar una unidad.");
  }
  return data;
}

async function assignmentFor(projectId, advisorId, assignmentId) {
  const assignment = await db.collection("asignaciones").doc(assignmentId).get();
  if (!assignment.exists || !isActiveAssignment(assignment.data(), projectId, advisorId)) {
    throw new HttpsError("failed-precondition", "El asesor ya no tiene una asignación activa para esta unidad.");
  }
  return assignment.data();
}

export const createTemporarySeparation = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Debes iniciar sesión.");
  const payload = request.data || {};
  const projectId = requiredText(payload.projectId, "el proyecto");
  const typologyId = requiredText(payload.typologyId, "la unidad");
  const advisorId = requiredText(payload.advisorId, "el asesor");
  const assignmentId = requiredText(payload.assignmentId, "la asignación");
  const client = await clientProfile(request.auth.uid);
  await assignmentFor(projectId, advisorId, assignmentId);

  const projectRef = db.collection("proyectos").doc(projectId);
  const typologyRef = db.collection("proyectos_tipologias").doc(typologyId);
  const holdRef = db.collection("bloqueos_unidad").doc(lockId(projectId, typologyId));
  const separationRef = db.collection("separaciones").doc();
  const now = Timestamp.now();
  const expiresAt = Timestamp.fromMillis(now.toMillis() + HOLD_MS);

  await db.runTransaction(async (transaction) => {
    const [projectSnapshot, typologySnapshot, holdSnapshot] = await transaction.getAll(
      projectRef, typologyRef, holdRef
    );
    if (!projectSnapshot.exists || !projectAllowsSeparation(projectSnapshot.data() || {})) {
      throw new HttpsError("failed-precondition", "El proyecto seleccionado no permite separaciones.");
    }
    const typology = typologySnapshot.data() || {};
    if (!typologySnapshot.exists || projectIdOf(typology) !== projectId || typology.available === false) {
      throw new HttpsError("failed-precondition", "La unidad seleccionada ya no está disponible.");
    }
    if (holdSnapshot.exists && activeLock(holdSnapshot.data() || {}, now)) {
      throw new HttpsError("already-exists", "Esta unidad fue retenida por otro usuario.");
    }

    const project = projectSnapshot.data() || {};
    const totalAmount = amountFrom(typology, "totalAmount", "montoTotal");
    const separationAmount = amountFrom(typology, "separationAmount", "montoSeparacion");
    if (separationAmount <= 0) {
      throw new HttpsError("failed-precondition", "La unidad no tiene un monto de separación válido.");
    }
    const unitName = text(typology.title) || text(typology.nombre) || "Unidad seleccionada";
    const projectName = text(project.nombre) || "Proyecto";
    const imageUrl = text(project.primaryImageUrl) || text(project.imageUrl) || text(project.imagenUrl);
    const currency = text(typology.currency) || text(project.currency) || "PEN";
    const separation = {
      id: separationRef.id,
      clienteId: request.auth.uid,
      clienteNombre: text(client.nombre) || text(client.nombres) || text(client.displayName),
      asesorId: advisorId,
      assignmentId,
      propertyId: projectId,
      projectId,
      proyectoId: projectId,
      tipologiaId: typologyId,
      unidadNombre: unitName,
      inmuebleNombre: projectName,
      primaryImageUrl: imageUrl,
      montoSeparacion: separationAmount,
      montoSeparacionTexto: `S/ ${separationAmount.toFixed(2)}`,
      amount: separationAmount,
      montoTexto: `S/ ${separationAmount.toFixed(2)}`,
      precioTotal: totalAmount,
      precioTotalTexto: totalAmount > 0 ? `S/ ${totalAmount.toFixed(2)}` : "",
      currency,
      estado: "Pendiente",
      estadoOperacion: "SEPARACION_PENDIENTE_PAGO",
      createdByRole: "cliente",
      createdAt: now,
      updatedAt: now,
      expiresAt,
      lockId: holdRef.id
    };
    transaction.set(separationRef, separation);
    transaction.set(holdRef, {
      id: holdRef.id,
      projectId,
      propertyId: projectId,
      proyectoId: projectId,
      tipologiaId: typologyId,
      separationId: separationRef.id,
      clienteId: request.auth.uid,
      asesorId: advisorId,
      assignmentId,
      estadoDisponibilidad: "RETENIDA_TEMPORALMENTE",
      createdAt: now,
      updatedAt: now,
      expiresAt
    });
  });

  return { separationId: separationRef.id, expiresAtMillis: expiresAt.toMillis() };
});

export const submitExternalSeparationPayment = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Debes iniciar sesión.");
  const payload = request.data || {};
  const separationId = requiredText(payload.separationId, "la separación");
  const method = requiredText(payload.method, "el medio de pago");
  const operationNumber = requiredText(payload.operationNumber, "el número de operación");
  const paidAt = requiredText(payload.paidAt, "la fecha de pago");
  const receiptUrl = requiredText(payload.receiptUrl, "el comprobante");
  const separationRef = db.collection("separaciones").doc(separationId);

  await db.runTransaction(async (transaction) => {
    const separationSnapshot = await transaction.get(separationRef);
    if (!separationSnapshot.exists) throw new HttpsError("not-found", "La separación ya no existe.");
    const separation = separationSnapshot.data() || {};
    if (text(separation.clienteId) !== request.auth.uid) {
      throw new HttpsError("permission-denied", "No puedes registrar el pago de otra persona.");
    }
    if (text(separation.estadoOperacion) !== "SEPARACION_PENDIENTE_PAGO"
        && text(separation.estadoOperacion) !== "PAGO_EN_VERIFICACION") {
      throw new HttpsError("failed-precondition", "Esta separación ya no admite comprobantes.");
    }
    const holdRef = db.collection("bloqueos_unidad").doc(requiredText(separation.lockId, "el bloqueo"));
    const holdSnapshot = await transaction.get(holdRef);
    if (!holdSnapshot.exists || text(holdSnapshot.data()?.separationId) !== separationId) {
      throw new HttpsError("failed-precondition", "No se encontró el bloqueo activo de la unidad.");
    }
    transaction.update(separationRef, {
      estadoOperacion: "PAGO_EN_VERIFICACION",
      estado: "Pendiente",
      externalPayment: {
        method,
        operationNumber,
        paidAt,
        receiptUrl,
        comment: text(payload.comment),
        submittedAt: Timestamp.now()
      },
      updatedAt: Timestamp.now()
    });
    transaction.update(holdRef, {
      estadoDisponibilidad: "PAGO_EN_VERIFICACION",
      updatedAt: Timestamp.now()
    });
  });
  return { separationId };
});

export const cancelTemporarySeparation = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Debes iniciar sesión.");
  const separationId = requiredText(request.data?.separationId, "la separación");
  const separationRef = db.collection("separaciones").doc(separationId);
  await db.runTransaction(async (transaction) => {
    const separationSnapshot = await transaction.get(separationRef);
    if (!separationSnapshot.exists) throw new HttpsError("not-found", "La separación ya no existe.");
    const separation = separationSnapshot.data() || {};
    if (text(separation.clienteId) !== request.auth.uid) {
      throw new HttpsError("permission-denied", "No puedes cancelar la separación de otra persona.");
    }
    const operationalState = text(separation.estadoOperacion);
    if (!["SEPARACION_PENDIENTE_PAGO", "PAGO_EN_VERIFICACION"].includes(operationalState)) {
      throw new HttpsError("failed-precondition", "Esta separación ya no se puede cancelar.");
    }
    const holdRef = db.collection("bloqueos_unidad").doc(requiredText(separation.lockId, "el bloqueo"));
    transaction.update(separationRef, {
      estadoOperacion: "SEPARACION_CANCELADA",
      estado: "Rechazada",
      cancelledAt: Timestamp.now(),
      updatedAt: Timestamp.now()
    });
    transaction.set(holdRef, {
      estadoDisponibilidad: "DISPONIBLE",
      separationId,
      updatedAt: Timestamp.now(),
      releasedAt: Timestamp.now()
    }, { merge: true });
  });
  return { separationId };
});

export const expireTemporarySeparations = onSchedule({
  region: "us-central1",
  schedule: "every 5 minutes",
  timeZone: "America/Lima"
}, async () => {
  const now = Timestamp.now();
  const expiredLocks = await db.collection("bloqueos_unidad")
    .where("estadoDisponibilidad", "==", "RETENIDA_TEMPORALMENTE")
    .where("expiresAt", "<=", now)
    .limit(200)
    .get();
  await Promise.all(expiredLocks.docs.map(async (lockSnapshot) => {
    const lockRef = lockSnapshot.ref;
    await db.runTransaction(async (transaction) => {
      const currentLock = await transaction.get(lockRef);
      if (!currentLock.exists) return;
      const lock = currentLock.data() || {};
      if (text(lock.estadoDisponibilidad) !== "RETENIDA_TEMPORALMENTE"
          || !(lock.expiresAt instanceof Timestamp)
          || lock.expiresAt.toMillis() > Timestamp.now().toMillis()) return;
      const separationRef = db.collection("separaciones").doc(text(lock.separationId));
      transaction.set(lockRef, {
        estadoDisponibilidad: "DISPONIBLE",
        updatedAt: Timestamp.now(),
        releasedAt: Timestamp.now()
      }, { merge: true });
      if (text(lock.separationId)) {
        transaction.set(separationRef, {
          estadoOperacion: "SEPARACION_VENCIDA",
          estado: "Rechazada",
          expiredAt: Timestamp.now(),
          updatedAt: Timestamp.now()
        }, { merge: true });
      }
    });
  }));
});

/** Keeps locks synchronized when an advisor or administrator validates/rejects an external payment. */
export const synchronizeTemporarySeparationLock = onDocumentWritten(
  { document: "separaciones/{separationId}", region: "us-central1" },
  async (event) => {
    const after = event.data?.after;
    if (!after?.exists) return;
    const separation = after.data() || {};
    const lockKey = text(separation.lockId);
    if (!lockKey) return;
    const lockRef = db.collection("bloqueos_unidad").doc(lockKey);
    if (text(separation.estado) === "Aprobada") {
      const batch = db.batch();
      batch.set(lockRef, {
        estadoDisponibilidad: "SEPARACION_CONFIRMADA",
        updatedAt: FieldValue.serverTimestamp(),
        confirmedAt: FieldValue.serverTimestamp()
      }, { merge: true });
      if (text(separation.estadoOperacion) !== "SEPARACION_CONFIRMADA") {
        batch.set(after.ref, {
          estadoOperacion: "SEPARACION_CONFIRMADA",
          updatedAt: FieldValue.serverTimestamp(),
          confirmedAt: FieldValue.serverTimestamp()
        }, { merge: true });
      }
      await batch.commit();
    } else if (text(separation.estado) === "Rechazada"
        && !["SEPARACION_CANCELADA", "SEPARACION_VENCIDA"].includes(text(separation.estadoOperacion))) {
      const batch = db.batch();
      batch.set(lockRef, {
        estadoDisponibilidad: "DISPONIBLE",
        updatedAt: FieldValue.serverTimestamp(),
        releasedAt: FieldValue.serverTimestamp()
      }, { merge: true });
      if (text(separation.estadoOperacion) !== "SEPARACION_CANCELADA") {
        batch.set(after.ref, {
          estadoOperacion: "SEPARACION_CANCELADA",
          updatedAt: FieldValue.serverTimestamp()
        }, { merge: true });
      }
      await batch.commit();
    }
  }
);
