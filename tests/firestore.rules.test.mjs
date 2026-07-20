import fs from "node:fs";
import test, { after, before, beforeEach } from "node:test";
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} from "@firebase/rules-unit-testing";
import {
  deleteDoc,
  doc,
  collection,
  getDoc,
  getDocs,
  query,
  setDoc,
  runTransaction,
  where,
  writeBatch,
} from "firebase/firestore";

const projectId = "iot-g3-c3fa2";
let env;

before(async () => {
  env = await initializeTestEnvironment({
    projectId,
    firestore: {
      host: "127.0.0.1",
      port: 8080,
      rules: fs.readFileSync("firestore.rules", "utf8"),
    },
  });
});

beforeEach(async () => {
  await env.clearFirestore();
  await env.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await Promise.all([
      setDoc(doc(db, "usuarios/admin-a"), {
        uid: "admin-a", rol: "admin", estado: "activo", empresaId: "empresa-a",
      }),
      setDoc(doc(db, "usuarios/admin-b"), {
        uid: "admin-b", rol: "admin", estado: "activo", empresaId: "empresa-b",
      }),
      setDoc(doc(db, "usuarios/asesor-a"), {
        uid: "asesor-a", rol: "asesor", estado: "activo", empresaId: "empresa-a",
      }),
      setDoc(doc(db, "usuarios/cliente-a"), {
        uid: "cliente-a", rol: "cliente", estado: "activo",
      }),
      setDoc(doc(db, "usuarios/cliente-b"), {
        uid: "cliente-b", rol: "cliente", estado: "activo",
      }),
      setDoc(doc(db, "usuarios/superadmin-a"), {
        uid: "superadmin-a", rol: "superadmin", estado: "activo",
      }),
      setDoc(doc(db, "empresas/empresa-a"), {
        nombre: "Empresa A", estado: "activo", adminUid: "admin-a",
      }),
      setDoc(doc(db, "empresas/empresa-b"), {
        nombre: "Empresa B", estado: "activo", adminUid: "admin-b",
      }),
      setDoc(doc(db, "empresas/empresa-c"), {
        nombre: "Empresa C", estado: "ACTIVO", adminUid: "admin-c",
      }),
      setDoc(doc(db, "proyectos/proyecto-a"), {
        projectId: "proyecto-a", adminId: "admin-a", empresaId: "empresa-a",
      }),
      setDoc(doc(db, "proyectos_tipologias/proyecto-a_tipologia_1"), {
        projectId: "proyecto-a", nombre: "Tipo A",
      }),
      setDoc(doc(db, "asignaciones/proyecto-a_asesor-a"), {
        id: "proyecto-a_asesor-a",
        projectId: "proyecto-a",
        asesorId: "asesor-a",
        adminId: "admin-a",
        empresaId: "empresa-a",
        estado: "ACTIVO",
      }),
    ]);
  });
});

after(async () => {
  await env.cleanup();
});

test("el admin propietario puede eliminar una tipologia", async () => {
  const db = env.authenticatedContext("admin-a").firestore();
  await assertSucceeds(deleteDoc(doc(db, "proyectos_tipologias/proyecto-a_tipologia_1")));
});

test("el admin puede consultar asesores de su propia empresa", async () => {
  const db = env.authenticatedContext("admin-a").firestore();
  const snapshot = await assertSucceeds(getDocs(query(
    collection(db, "usuarios"),
    where("rol", "==", "asesor"),
    where("empresaId", "==", "empresa-a"),
  )));
  if (snapshot.size !== 1) throw new Error("La consulta no devolvió al asesor esperado.");
});

test("el admin no puede consultar asesores de otra empresa", async () => {
  const db = env.authenticatedContext("admin-b").firestore();
  await assertFails(getDocs(query(
    collection(db, "usuarios"),
    where("rol", "==", "asesor"),
    where("empresaId", "==", "empresa-a"),
  )));
});

test("el guardado atomico puede reemplazar colecciones del proyecto", async () => {
  const db = env.authenticatedContext("admin-a").firestore();
  const batch = writeBatch(db);
  batch.set(doc(db, "proyectos/proyecto-a"), {
    projectId: "proyecto-a",
    adminId: "admin-a",
    empresaId: "empresa-a",
    updatedAt: Date.now(),
  }, { merge: true });
  batch.delete(doc(db, "proyectos_tipologias/proyecto-a_tipologia_1"));
  batch.set(doc(db, "proyectos_tipologias/proyecto-a_tipologia_2"), {
    projectId: "proyecto-a",
    nombre: "Tipo B",
  });
  await assertSucceeds(batch.commit());
});

test("otro admin no puede eliminar datos del proyecto", async () => {
  const db = env.authenticatedContext("admin-b").firestore();
  await assertFails(deleteDoc(doc(db, "proyectos_tipologias/proyecto-a_tipologia_1")));
});

test("el propietario puede crear una asignacion para un asesor de su empresa", async () => {
  const db = env.authenticatedContext("admin-a").firestore();
  await assertSucceeds(setDoc(doc(db, "asignaciones/proyecto-a_asesor-a"), {
    id: "proyecto-a_asesor-a",
    projectId: "proyecto-a",
    propertyId: "proyecto-a",
    proyectoId: "proyecto-a",
    asesorId: "asesor-a",
    adminId: "admin-a",
    empresaId: "empresa-a",
    estado: "ACTIVO",
  }));
});

test("un admin ajeno no puede asignar sobre el proyecto", async () => {
  const db = env.authenticatedContext("admin-b").firestore();
  await assertFails(setDoc(doc(db, "asignaciones/proyecto-a_asesor-a"), {
    id: "proyecto-a_asesor-a",
    projectId: "proyecto-a",
    propertyId: "proyecto-a",
    proyectoId: "proyecto-a",
    asesorId: "asesor-a",
    adminId: "admin-b",
    empresaId: "empresa-b",
    estado: "ACTIVO",
  }));
});

test("el asesor solo puede leer citas de proyectos con asignacion activa", async () => {
  await env.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), "citas/cita-agenda"), {
      id: "cita-agenda",
      clienteId: "cliente-a",
      asesorId: "asesor-a",
      propertyId: "proyecto-a",
      projectId: "proyecto-a",
      proyectoId: "proyecto-a",
      fechaISO: "2026-06-20",
      hora: "09:00",
      estado: "Confirmada",
    });
  });

  const advisorDb = env.authenticatedContext("asesor-a").firestore();
  await assertSucceeds(getDoc(doc(advisorDb, "citas/cita-agenda")));

  await env.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), "asignaciones/proyecto-a_asesor-a"), {
      estado: "INACTIVO",
    }, { merge: true });
  });
  await assertFails(getDoc(doc(advisorDb, "citas/cita-agenda")));
});

test("un usuario no puede autoconcederse rol admin sin invitacion", async () => {
  const db = env.authenticatedContext("cliente-x", { email: "cliente@example.com" }).firestore();
  await assertFails(setDoc(doc(db, "usuarios/cliente-x"), {
    uid: "cliente-x",
    rol: "admin",
    empresaId: "empresa-a",
  }));
});

test("un asesor puede postular solo como pendiente a una empresa activa", async () => {
  const db = env.authenticatedContext("asesor-nuevo", { email: "asesor@example.com" }).firestore();
  await assertSucceeds(setDoc(doc(db, "usuarios/asesor-nuevo"), {
    uid: "asesor-nuevo",
    email: "asesor@example.com",
    nombres: "Asesor",
    apellidos: "Nuevo",
    telefono: "999999999",
    rol: "asesor",
    estado: "pendiente",
    empresaSolicitadaId: "empresa-a",
    empresaSolicitadaNombre: "Empresa A",
    createdAt: Date.now(),
  }));
});

test("el registro sin sesión solo puede consultar inmobiliarias activas", async () => {
  const publicDb = env.unauthenticatedContext().firestore();
  await assertSucceeds(getDocs(query(
    collection(publicDb, "empresas"),
    where("estado", "==", "activo"),
  )));
  await assertSucceeds(getDocs(query(
    collection(publicDb, "empresas"),
    where("estado", "in", ["activo", "ACTIVO", "Activo"]),
  )));
  await assertFails(getDocs(query(
    collection(publicDb, "empresas"),
    where("estado", "==", "pendiente"),
  )));
});

test("un usuario no puede autoconcederse asesor activo ni cambiar su rol", async () => {
  const newUserDb = env.authenticatedContext("asesor-falso", { email: "falso@example.com" }).firestore();
  await assertFails(setDoc(doc(newUserDb, "usuarios/asesor-falso"), {
    uid: "asesor-falso",
    rol: "asesor",
    estado: "activo",
    empresaSolicitadaId: "empresa-a",
    empresaSolicitadaNombre: "Empresa A",
  }));

  const clientDb = env.authenticatedContext("cliente-a").firestore();
  await assertFails(setDoc(doc(clientDb, "usuarios/cliente-a"), {
    rol: "asesor",
    estado: "pendiente",
    empresaSolicitadaId: "empresa-a",
    empresaSolicitadaNombre: "Empresa A",
  }, { merge: true }));
});

test("solo el superadministrador puede activar y vincular un asesor pendiente", async () => {
  await env.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), "usuarios/asesor-pendiente"), {
      uid: "asesor-pendiente",
      rol: "asesor",
      estado: "pendiente",
      empresaSolicitadaId: "empresa-a",
      empresaSolicitadaNombre: "Empresa A",
    });
  });

  const adminDb = env.authenticatedContext("admin-a").firestore();
  await assertFails(setDoc(doc(adminDb, "usuarios/asesor-pendiente"), {
    estado: "activo",
    empresaId: "empresa-a",
    inmobiliariaId: "empresa-a",
  }, { merge: true }));

  const superadminDb = env.authenticatedContext("superadmin-a").firestore();
  await assertSucceeds(setDoc(doc(superadminDb, "usuarios/asesor-pendiente"), {
    estado: "activo",
    empresaId: "empresa-a",
    inmobiliariaId: "empresa-a",
  }, { merge: true }));
});

test("una invitacion pendiente permite crear el perfil admin correspondiente", async () => {
  await env.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), "admin_invitations/invite-x"), {
      email: "admin@example.com",
      empresaId: "empresa-a",
      estado: "pendiente",
    });
  });
  const db = env.authenticatedContext("admin-x", { email: "admin@example.com" }).firestore();
  await assertSucceeds(setDoc(doc(db, "usuarios/admin-x"), {
    uid: "admin-x",
    rol: "admin",
    empresaId: "empresa-a",
    invitationId: "invite-x",
  }));
});

test("el admin invitado puede aceptar su invitacion sin alterar correo o empresa", async () => {
  await env.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), "admin_invitations/invite-x"), {
      email: "admin@example.com",
      empresaId: "empresa-a",
      estado: "pendiente",
    });
  });
  const db = env.authenticatedContext("admin-x", { email: "admin@example.com" }).firestore();
  await assertSucceeds(setDoc(doc(db, "admin_invitations/invite-x"), {
    email: "admin@example.com",
    empresaId: "empresa-a",
    estado: "aceptada",
    acceptedByUid: "admin-x",
  }, { merge: true }));
});

test("el cliente puede crear un chat del proyecto con un asesor asignado", async () => {
  await env.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), "asignaciones/proyecto-a_asesor-a"), {
      projectId: "proyecto-a", asesorId: "asesor-a", estado: "ACTIVO",
    });
  });
  const db = env.authenticatedContext("cliente-a").firestore();
  await assertSucceeds(setDoc(doc(db, "conversaciones/project_chat_cliente-a_proyecto-a"), {
    id: "project_chat_cliente-a_proyecto-a",
    conversationType: "project",
    schemaVersion: 2,
    clienteUid: "cliente-a",
    asesorUid: "asesor-a",
    participantUids: ["cliente-a", "asesor-a"],
    projectId: "proyecto-a",
    projectName: "Proyecto A",
    lastMessage: "Sin mensajes aun",
    lastMessageAt: Date.now(),
    updatedAt: Date.now(),
    unreadForCliente: false,
    unreadForAsesor: false,
    active: true,
  }));
});

test("el cliente puede crear un chat cuando el asesor activo usa estado ACTIVO", async () => {
  await env.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), "usuarios/asesor-a"), {
      estado: "ACTIVO",
    }, { merge: true });
  });
  const db = env.authenticatedContext("cliente-a").firestore();
  await assertSucceeds(setDoc(doc(db, "conversaciones/project_chat_cliente-a_proyecto-a"), {
    id: "project_chat_cliente-a_proyecto-a",
    conversationType: "project",
    schemaVersion: 2,
    clienteUid: "cliente-a",
    asesorUid: "asesor-a",
    participantUids: ["cliente-a", "asesor-a"],
    projectId: "proyecto-a",
    projectName: "Proyecto A",
    lastMessage: "Sin mensajes aun",
    lastMessageAt: Date.now(),
    updatedAt: Date.now(),
    unreadForCliente: false,
    unreadForAsesor: false,
    active: true,
  }));
});

test("un mismo asesor mantiene chats independientes por proyecto y no se admiten IDs duplicados", async () => {
  await env.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await setDoc(doc(db, "proyectos/proyecto-b"), {
      projectId: "proyecto-b", adminId: "admin-a", empresaId: "empresa-a",
    });
    await setDoc(doc(db, "asignaciones/proyecto-b_asesor-a"), {
      id: "proyecto-b_asesor-a", projectId: "proyecto-b", asesorId: "asesor-a", estado: "ACTIVO",
    });
  });
  const db = env.authenticatedContext("cliente-a").firestore();
  const base = {
    conversationType: "project", schemaVersion: 2, clienteUid: "cliente-a", asesorUid: "asesor-a",
    participantUids: ["cliente-a", "asesor-a"], projectName: "Proyecto", lastMessage: "Sin mensajes aun",
    lastMessageAt: 1, updatedAt: 1, createdAt: 1, unreadForCliente: false, unreadForAsesor: false, active: true,
  };
  await assertSucceeds(setDoc(doc(db, "conversaciones/project_chat_cliente-a_proyecto-a"), {
    ...base, id: "project_chat_cliente-a_proyecto-a", projectId: "proyecto-a",
  }));
  await assertSucceeds(setDoc(doc(db, "conversaciones/project_chat_cliente-a_proyecto-b"), {
    ...base, id: "project_chat_cliente-a_proyecto-b", projectId: "proyecto-b",
  }));
  await assertFails(setDoc(doc(db, "conversaciones/chat-duplicado"), {
    ...base, id: "chat-duplicado", projectId: "proyecto-a",
  }));
  await assertFails(setDoc(doc(db, "conversaciones/project_chat_cliente-a_proyecto-a"), {
    asesorUid: "asesor-ajeno", participantUids: ["cliente-a", "asesor-ajeno"],
  }, { merge: true }));
});

test("el cliente no puede crear un chat de proyecto con asesor no asignado", async () => {
  const db = env.authenticatedContext("cliente-a").firestore();
  await assertFails(setDoc(doc(db, "conversaciones/chat-no-asignado"), {
    id: "chat-no-asignado",
    clienteUid: "cliente-a",
    asesorUid: "asesor-ajeno",
    participantUids: ["cliente-a", "asesor-ajeno"],
    projectId: "proyecto-a",
    lastMessage: "Sin mensajes aun",
    lastMessageAt: Date.now(),
    updatedAt: Date.now(),
    unreadForCliente: false,
    unreadForAsesor: false,
    active: true,
  }));
});

test("no se pueden alterar participantes y el asesor participante puede responder", async () => {
  await env.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await setDoc(doc(db, "conversaciones/chat-existente"), {
      id: "chat-existente", clienteUid: "cliente-a", asesorUid: "asesor-a",
      participantUids: ["cliente-a", "asesor-a"], projectId: "proyecto-a",
      lastMessage: "Hola", lastMessageAt: Date.now(), updatedAt: Date.now(),
      unreadForCliente: false, unreadForAsesor: false, active: true,
    });
  });
  const clientDb = env.authenticatedContext("cliente-a").firestore();
  await assertFails(setDoc(doc(clientDb, "conversaciones/chat-existente"), {
    participantUids: ["cliente-a", "intruso"],
  }, { merge: true }));

  const outsiderDb = env.authenticatedContext("intruso").firestore();
  await assertFails(setDoc(doc(outsiderDb, "mensajes/mensaje-intruso"), {
    id: "mensaje-intruso", conversationId: "chat-existente", senderUid: "intruso",
    receiverUid: "cliente-a", participantUids: ["cliente-a", "asesor-a"],
    text: "Mensaje no autorizado", createdAt: Date.now(),
  }));

  const advisorDb = env.authenticatedContext("asesor-a").firestore();
  await assertSucceeds(setDoc(doc(advisorDb, "mensajes/mensaje-asesor"), {
    id: "mensaje-asesor", conversationId: "chat-existente", senderUid: "asesor-a",
    receiverUid: "cliente-a", participantUids: ["cliente-a", "asesor-a"],
    text: "Hola, ¿en qué te ayudo?", createdAt: Date.now(),
  }));
});

test("una reserva atomica crea cita, slot, bloqueo del cliente y evento", async () => {
  const db = env.authenticatedContext("cliente-a").firestore();
  const citaId = "cita_asesor-a_2026-06-20_09_00_cliente-a";
  const slotId = "asesor-a_2026-06-20_09_00";
  const lockId = "client_cliente-a_2026-06-20_09_00";
  const batch = writeBatch(db);

  batch.set(doc(db, `citas/${citaId}`), {
    id: citaId,
    clienteId: "cliente-a",
    asesorId: "asesor-a",
    propertyId: "proyecto-a",
    fechaISO: "2026-06-20",
    fechaTexto: "20 Jun 2026",
    hora: "09:00",
    slotKey: "09_00",
    slotId,
    participantUids: ["cliente-a", "asesor-a"],
    estado: "Confirmada",
    createdAt: 1,
  });
  batch.set(doc(db, `citas_slots/${slotId}`), {
    id: slotId,
    citaId,
    citaIds: [citaId],
    clienteId: "cliente-a",
    clientIds: ["cliente-a"],
    asesorId: "asesor-a",
    propertyId: "proyecto-a",
    fechaISO: "2026-06-20",
    hora: "09:00",
    slotKey: "09_00",
    capacidadMaxima: 1,
    reservedCount: 1,
    estado: "ocupado",
    participantUids: ["cliente-a", "asesor-a"],
  });
  batch.set(doc(db, `cliente_citas_slots/${lockId}`), {
    citaId,
    clienteId: "cliente-a",
    asesorId: "asesor-a",
    propertyId: "proyecto-a",
    fechaISO: "2026-06-20",
    slotKey: "09_00",
  });
  batch.set(doc(db, `eventos_cita/evt_${citaId}`), {
    citaId,
    clienteId: "cliente-a",
    asesorId: "asesor-a",
    tipo: "AGENDADA",
  });
  await assertSucceeds(batch.commit());
});

test("los clientes pueden ver pero no modificar bloqueos temporales de unidad", async () => {
  await env.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), "bloqueos_unidad/proyecto-a__proyecto-a_tipologia_1"), {
      projectId: "proyecto-a", tipologiaId: "proyecto-a_tipologia_1",
      separationId: "sep-servidor", estadoDisponibilidad: "RETENIDA_TEMPORALMENTE",
    });
  });
  const clientDb = env.authenticatedContext("cliente-a").firestore();
  await assertSucceeds(getDoc(doc(clientDb, "bloqueos_unidad/proyecto-a__proyecto-a_tipologia_1")));
  await assertFails(setDoc(doc(clientDb, "bloqueos_unidad/proyecto-a__proyecto-a_tipologia_1"), {
    estadoDisponibilidad: "DISPONIBLE",
  }, { merge: true }));
});

test("el cliente crea una separación solo para un asesor asignado y con referencias canónicas", async () => {
  const db = env.authenticatedContext("cliente-a").firestore();
  await assertSucceeds(setDoc(doc(db, "separaciones/sep-cliente-a"), {
    id: "sep-cliente-a",
    clienteId: "cliente-a",
    clienteNombre: "Cliente A",
    asesorId: "asesor-a",
    asesorNombre: "Asesor A",
    assignmentId: "proyecto-a_asesor-a",
    propertyId: "proyecto-a",
    projectId: "proyecto-a",
    proyectoId: "proyecto-a",
    estado: "Pagada",
    createdByRole: "cliente",
    createdAt: 1,
  }));

  const otherClient = env.authenticatedContext("cliente-b").firestore();
  await assertFails(setDoc(doc(otherClient, "separaciones/sep-ajena"), {
    id: "sep-ajena",
    clienteId: "cliente-a",
    asesorId: "asesor-a",
    assignmentId: "proyecto-a_asesor-a",
    propertyId: "proyecto-a",
    projectId: "proyecto-a",
    proyectoId: "proyecto-a",
    estado: "Pagada",
    createdByRole: "cliente",
    createdAt: 1,
  }));
});

test("el asesor puede vincular atómicamente una separación a su cita activa", async () => {
  const citaId = "cita-separacion-a";
  await env.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), `citas/${citaId}`), {
      id: citaId,
      clienteId: "cliente-a",
      asesorId: "asesor-a",
      assignmentId: "proyecto-a_asesor-a",
      propertyId: "proyecto-a",
      projectId: "proyecto-a",
      proyectoId: "proyecto-a",
      participantUids: ["cliente-a", "asesor-a"],
      estado: "Confirmada",
      createdAt: 1,
      hasCierre: false,
    });
  });
  const db = env.authenticatedContext("asesor-a").firestore();
  const batch = writeBatch(db);
  batch.set(doc(db, "separaciones/sep-cita-a"), {
    id: "sep-cita-a",
    clienteId: "cliente-a",
    clienteNombre: "Cliente A",
    asesorId: "asesor-a",
    asesorNombre: "Asesor A",
    assignmentId: "proyecto-a_asesor-a",
    citaId,
    propertyId: "proyecto-a",
    projectId: "proyecto-a",
    proyectoId: "proyecto-a",
    estado: "Pendiente",
    createdByRole: "asesor",
    createdAt: 2,
  });
  batch.set(doc(db, `citas/${citaId}`), {
    hasCierre: true,
    separacionId: "sep-cita-a",
  }, { merge: true });
  await assertSucceeds(batch.commit());
});

test("la transaccion de reserva puede comprobar documentos inexistentes sin exponer los existentes", async () => {
  const db = env.authenticatedContext("cliente-a").firestore();
  const citaId = "cita_asesor-a_2026-06-26_16_00_cliente-a";
  const slotId = "asesor-a_2026-06-26_16_00";
  const lockId = "client_cliente-a_2026-06-26_16_00";

  await assertSucceeds(runTransaction(db, async (transaction) => {
    const citaRef = doc(db, `citas/${citaId}`);
    const lockRef = doc(db, `cliente_citas_slots/${lockId}`);
    const slotRef = doc(db, `citas_slots/${slotId}`);
    const eventRef = doc(db, `eventos_cita/evt_${citaId}`);

    const [cita, lock, slot] = await Promise.all([
      transaction.get(citaRef),
      transaction.get(lockRef),
      transaction.get(slotRef),
    ]);
    if (cita.exists() || lock.exists() || slot.exists()) {
      throw new Error("La prueba requiere un horario sin reservas previas.");
    }

    transaction.set(citaRef, {
      id: citaId, clienteId: "cliente-a", asesorId: "asesor-a",
      assignmentId: "proyecto-a_asesor-a", propertyId: "proyecto-a",
      projectId: "proyecto-a", proyectoId: "proyecto-a", fechaISO: "2026-06-26",
      fechaTexto: "26 Jun 2026", hora: "16:00", slotKey: "16_00", slotId,
      participantUids: ["cliente-a", "asesor-a"], estado: "Confirmada",
      createdAt: 1, updatedAt: 1,
    }, { merge: true });
    transaction.set(slotRef, {
      id: slotId, citaId, citaIds: [citaId], clienteId: "cliente-a",
      clientIds: ["cliente-a"], asesorId: "asesor-a",
      assignmentId: "proyecto-a_asesor-a", propertyId: "proyecto-a",
      fechaISO: "2026-06-26", hora: "16:00", slotKey: "16_00",
      capacidadMaxima: 1, reservedCount: 1, estado: "ocupado",
      participantUids: ["cliente-a", "asesor-a"], createdAt: 1, updatedAt: 1,
    }, { merge: true });
    transaction.set(lockRef, {
      citaId, clienteId: "cliente-a", asesorId: "asesor-a",
      assignmentId: "proyecto-a_asesor-a", propertyId: "proyecto-a",
      fechaISO: "2026-06-26", slotKey: "16_00", createdAt: 1,
    });
    transaction.set(eventRef, {
      citaId, clienteId: "cliente-a", asesorId: "asesor-a", tipo: "AGENDADA",
    }, { merge: true });
  }));

  const outsiderDb = env.authenticatedContext("cliente-b").firestore();
  await assertFails(getDoc(doc(outsiderDb, `citas/${citaId}`)));
  await assertFails(getDoc(doc(outsiderDb, `cliente_citas_slots/${lockId}`)));
});

test("una reserva acepta una asignacion historica con ID no canonico", async () => {
  const assignmentId = "asignacion-historica-001";
  await env.withSecurityRulesDisabled(async (context) => {
    const adminDb = context.firestore();
    await deleteDoc(doc(adminDb, "asignaciones/proyecto-a_asesor-a"));
    await setDoc(doc(adminDb, `asignaciones/${assignmentId}`), {
      id: assignmentId,
      projectId: "proyecto-a",
      asesorId: "asesor-a",
      estado: "Activo",
    });
  });

  const db = env.authenticatedContext("cliente-a").firestore();
  const citaId = "cita-legacy-assignment";
  const slotId = "asesor-a_2026-06-21_16_00";
  const lockId = "client_cliente-a_2026-06-21_16_00";
  const batch = writeBatch(db);

  batch.set(doc(db, `citas/${citaId}`), {
    id: citaId,
    clienteId: "cliente-a",
    asesorId: "asesor-a",
    assignmentId,
    propertyId: "proyecto-a",
    fechaISO: "2026-06-21",
    fechaTexto: "21 Jun 2026",
    hora: "16:00",
    slotKey: "16_00",
    slotId,
    participantUids: ["cliente-a", "asesor-a"],
    estado: "Confirmada",
    createdAt: 1,
  });
  batch.set(doc(db, `citas_slots/${slotId}`), {
    id: slotId,
    citaId,
    citaIds: [citaId],
    clienteId: "cliente-a",
    clientIds: ["cliente-a"],
    asesorId: "asesor-a",
    assignmentId,
    propertyId: "proyecto-a",
    fechaISO: "2026-06-21",
    hora: "16:00",
    slotKey: "16_00",
    capacidadMaxima: 1,
    reservedCount: 1,
    estado: "ocupado",
    participantUids: ["cliente-a", "asesor-a"],
  });
  batch.set(doc(db, `cliente_citas_slots/${lockId}`), {
    citaId,
    clienteId: "cliente-a",
    asesorId: "asesor-a",
    assignmentId,
    propertyId: "proyecto-a",
    fechaISO: "2026-06-21",
    slotKey: "16_00",
  });
  batch.set(doc(db, `eventos_cita/evt_${citaId}`), {
    citaId,
    clienteId: "cliente-a",
    asesorId: "asesor-a",
    tipo: "AGENDADA",
  });

  await assertSucceeds(batch.commit());
});

test("un cliente no puede reservar con un asesor no asignado ni alterar la cita", async () => {
  const db = env.authenticatedContext("cliente-a").firestore();
  await assertFails(setDoc(doc(db, "citas/cita-invalida"), {
    id: "cita-invalida",
    clienteId: "cliente-a",
    asesorId: "asesor-ajeno",
    propertyId: "proyecto-a",
    fechaISO: "2026-06-20",
    hora: "10:00",
    slotKey: "10_00",
    slotId: "asesor-ajeno_2026-06-20_10_00",
    participantUids: ["cliente-a", "asesor-ajeno"],
    estado: "Confirmada",
  }));

  await env.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), "citas/cita-protegida"), {
      id: "cita-protegida", clienteId: "cliente-a", asesorId: "asesor-a",
      propertyId: "proyecto-a", fechaISO: "2026-06-20", slotKey: "09_00",
      slotId: "asesor-a_2026-06-20_09_00", participantUids: ["cliente-a", "asesor-a"],
      estado: "Confirmada", createdAt: 1,
    });
  });
  await assertFails(setDoc(doc(db, "citas/cita-protegida"), { asesorId: "asesor-ajeno" }, { merge: true }));
});

test("dos clientes no pueden ocupar simultaneamente un slot con capacidad uno", async () => {
  const slotId = "asesor-a_2026-06-25_09_00";
  const citaInicial = "cita-slot-ocupado";
  await env.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await setDoc(doc(db, `citas/${citaInicial}`), {
      id: citaInicial, clienteId: "cliente-a", asesorId: "asesor-a", propertyId: "proyecto-a",
      fechaISO: "2026-06-25", hora: "09:00", slotKey: "09_00", slotId,
      participantUids: ["cliente-a", "asesor-a"], estado: "Confirmada", createdAt: 1,
    });
    await setDoc(doc(db, `citas_slots/${slotId}`), {
      id: slotId, citaId: citaInicial, citaIds: [citaInicial], clienteId: "cliente-a",
      clientIds: ["cliente-a"], asesorId: "asesor-a", propertyId: "proyecto-a",
      fechaISO: "2026-06-25", hora: "09:00", slotKey: "09_00", capacidadMaxima: 1,
      reservedCount: 1,
    });
  });

  const db = env.authenticatedContext("cliente-b").firestore();
  const citaId = "cita-intento-concurrente";
  const batch = writeBatch(db);
  batch.set(doc(db, `citas/${citaId}`), {
    id: citaId, clienteId: "cliente-b", asesorId: "asesor-a", propertyId: "proyecto-a",
    fechaISO: "2026-06-25", hora: "09:00", slotKey: "09_00", slotId,
    participantUids: ["cliente-b", "asesor-a"], estado: "Confirmada", createdAt: 2,
  });
  batch.set(doc(db, `citas_slots/${slotId}`), {
    citaId, citaIds: [citaId], clienteId: "cliente-b", clientIds: ["cliente-b"],
    reservedCount: 1,
  }, { merge: true });
  batch.set(doc(db, "cliente_citas_slots/client_cliente-b_2026-06-25_09_00"), {
    citaId, clienteId: "cliente-b", asesorId: "asesor-a", propertyId: "proyecto-a",
    fechaISO: "2026-06-25", slotKey: "09_00",
  });
  await assertFails(batch.commit());
});

test("la reprogramacion del asesor mueve en forma atomica la cita, el slot y el bloqueo", async () => {
  const citaId = "cita-para-reprogramar";
  const oldSlotId = "asesor-a_2026-06-22_09_00";
  const newSlotId = "asesor-a_2026-06-23_10_00";
  const oldLockId = "client_cliente-a_2026-06-22_09_00";
  const newLockId = "client_cliente-a_2026-06-23_10_00";

  await env.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await setDoc(doc(db, `citas/${citaId}`), {
      id: citaId, clienteId: "cliente-a", asesorId: "asesor-a", propertyId: "proyecto-a",
      fechaISO: "2026-06-22", fechaTexto: "22 Jun 2026", hora: "09:00",
      slotKey: "09_00", slotId: oldSlotId, participantUids: ["cliente-a", "asesor-a"],
      estado: "Confirmada", createdAt: 1,
    });
    await setDoc(doc(db, `citas_slots/${oldSlotId}`), {
      id: oldSlotId, citaId, citaIds: [citaId], clienteId: "cliente-a", clientIds: ["cliente-a"],
      asesorId: "asesor-a", propertyId: "proyecto-a", fechaISO: "2026-06-22", hora: "09:00",
      slotKey: "09_00", capacidadMaxima: 1, reservedCount: 1,
    });
    await setDoc(doc(db, `cliente_citas_slots/${oldLockId}`), {
      citaId, clienteId: "cliente-a", asesorId: "asesor-a", propertyId: "proyecto-a",
      fechaISO: "2026-06-22", slotKey: "09_00",
    });
  });

  const db = env.authenticatedContext("asesor-a").firestore();
  const batch = writeBatch(db);
  batch.set(doc(db, `citas/${citaId}`), {
    fechaISO: "2026-06-23", fechaTexto: "23 Jun 2026", hora: "10:00", slotKey: "10_00",
    slotId: newSlotId, estado: "Reprogramada", rescheduleReason: "Solicitud del cliente", updatedAt: 2,
  }, { merge: true });
  batch.delete(doc(db, `citas_slots/${oldSlotId}`));
  batch.delete(doc(db, `cliente_citas_slots/${oldLockId}`));
  batch.set(doc(db, `citas_slots/${newSlotId}`), {
    id: newSlotId, citaId, citaIds: [citaId], clienteId: "cliente-a", clientIds: ["cliente-a"],
    asesorId: "asesor-a", propertyId: "proyecto-a", fechaISO: "2026-06-23", hora: "10:00",
    slotKey: "10_00", capacidadMaxima: 1, reservedCount: 1,
  });
  batch.set(doc(db, `cliente_citas_slots/${newLockId}`), {
    citaId, clienteId: "cliente-a", asesorId: "asesor-a", propertyId: "proyecto-a",
    fechaISO: "2026-06-23", slotKey: "10_00",
  });
  batch.set(doc(db, "eventos_cita/evt-reprogramada"), {
    citaId, clienteId: "cliente-a", asesorId: "asesor-a", tipo: "REPROGRAMADA",
  });
  await assertSucceeds(batch.commit());
});

test("la cancelacion del asesor exige liberar el slot y el bloqueo del cliente", async () => {
  const citaId = "cita-para-cancelar";
  const slotId = "asesor-a_2026-06-24_09_00";
  const lockId = "client_cliente-a_2026-06-24_09_00";
  await env.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await setDoc(doc(db, `citas/${citaId}`), {
      id: citaId, clienteId: "cliente-a", asesorId: "asesor-a", propertyId: "proyecto-a",
      fechaISO: "2026-06-24", hora: "09:00", slotKey: "09_00", slotId,
      participantUids: ["cliente-a", "asesor-a"], estado: "Confirmada", createdAt: 1,
    });
    await setDoc(doc(db, `citas_slots/${slotId}`), {
      id: slotId, citaId, citaIds: [citaId], clienteId: "cliente-a", clientIds: ["cliente-a"],
      asesorId: "asesor-a", propertyId: "proyecto-a", fechaISO: "2026-06-24", hora: "09:00",
      slotKey: "09_00", capacidadMaxima: 1, reservedCount: 1,
    });
    await setDoc(doc(db, `cliente_citas_slots/${lockId}`), {
      citaId, clienteId: "cliente-a", asesorId: "asesor-a", propertyId: "proyecto-a",
      fechaISO: "2026-06-24", slotKey: "09_00",
    });
  });

  const db = env.authenticatedContext("asesor-a").firestore();
  const incomplete = writeBatch(db);
  incomplete.set(doc(db, `citas/${citaId}`), {
    estado: "Cancelada", cancelReason: "No disponible", cancelledAt: 2, updatedAt: 2,
  }, { merge: true });
  incomplete.delete(doc(db, `cliente_citas_slots/${lockId}`));
  await assertFails(incomplete.commit());

  const complete = writeBatch(db);
  complete.set(doc(db, `citas/${citaId}`), {
    estado: "Cancelada", cancelReason: "No disponible", cancelledAt: 2, updatedAt: 2,
  }, { merge: true });
  complete.delete(doc(db, `citas_slots/${slotId}`));
  complete.delete(doc(db, `cliente_citas_slots/${lockId}`));
  complete.set(doc(db, "eventos_cita/evt-cancelada"), {
    citaId, clienteId: "cliente-a", asesorId: "asesor-a", tipo: "CANCELADA",
  });
  await assertSucceeds(complete.commit());
});

test("el cliente propietario puede cancelar solo su cita y libera el horario", async () => {
  const citaId = "cita-cliente-cancela";
  const slotId = "asesor-a_2026-06-26_09_00";
  const lockId = "client_cliente-a_2026-06-26_09_00";
  await env.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await setDoc(doc(db, `citas/${citaId}`), {
      id: citaId, clienteId: "cliente-a", asesorId: "asesor-a", propertyId: "proyecto-a",
      fechaISO: "2026-06-26", hora: "09:00", slotKey: "09_00", slotId,
      participantUids: ["cliente-a", "asesor-a"], estado: "Confirmada", createdAt: 1,
    });
    await setDoc(doc(db, `citas_slots/${slotId}`), {
      id: slotId, citaId, citaIds: [citaId], clienteId: "cliente-a", clientIds: ["cliente-a"],
      asesorId: "asesor-a", propertyId: "proyecto-a", fechaISO: "2026-06-26", hora: "09:00",
      slotKey: "09_00", capacidadMaxima: 1, reservedCount: 1,
    });
    await setDoc(doc(db, `cliente_citas_slots/${lockId}`), {
      citaId, clienteId: "cliente-a", asesorId: "asesor-a", propertyId: "proyecto-a",
      fechaISO: "2026-06-26", slotKey: "09_00",
    });
  });

  const db = env.authenticatedContext("cliente-a").firestore();
  const batch = writeBatch(db);
  batch.set(doc(db, `citas/${citaId}`), {
    estado: "Cancelada", cancelReason: "No puedo asistir", cancelledAt: 2, updatedAt: 2,
  }, { merge: true });
  batch.delete(doc(db, `citas_slots/${slotId}`));
  batch.delete(doc(db, `cliente_citas_slots/${lockId}`));
  batch.set(doc(db, "eventos_cita/evt-cliente-cancela"), {
    citaId, clienteId: "cliente-a", asesorId: "asesor-a", tipo: "CANCELADA",
  });
  await assertSucceeds(batch.commit());

  const otherClient = env.authenticatedContext("cliente-b").firestore();
  await assertFails(setDoc(doc(otherClient, `citas/${citaId}`), {
    estado: "Cancelada", cancelReason: "Ajena", cancelledAt: 3, updatedAt: 3,
  }, { merge: true }));
});

test("el cliente crea y ambos participantes leen el chat canónico de su proyecto", async () => {
  const conversationId = "project_chat_cliente-a_proyecto-a";
  const conversation = {
    id: conversationId, conversationType: "project", schemaVersion: 2,
    clienteUid: "cliente-a", asesorUid: "asesor-a",
    participantUids: ["cliente-a", "asesor-a"], projectId: "proyecto-a",
    projectName: "Proyecto A", lastMessage: "Sin mensajes aun", lastMessageAt: 1,
    updatedAt: 1, createdAt: 1, unreadForCliente: false, unreadForAsesor: false, active: true,
  };
  const clientDb = env.authenticatedContext("cliente-a").firestore();
  await assertSucceeds(setDoc(doc(clientDb, `conversaciones/${conversationId}`), conversation));
  const advisorDb = env.authenticatedContext("asesor-a").firestore();
  await assertSucceeds(getDoc(doc(advisorDb, `conversaciones/${conversationId}`)));
  const outsiderDb = env.authenticatedContext("cliente-b").firestore();
  await assertFails(getDoc(doc(outsiderDb, `conversaciones/${conversationId}`)));
});

test("un chat de cita rechaza IDs de cliente, asesor o cita alterados", async () => {
  const citaId = "cita-chat-integridad";
  await env.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), `citas/${citaId}`), {
      id: citaId, clienteId: "cliente-a", asesorId: "asesor-a", propertyId: "proyecto-a",
      participantUids: ["cliente-a", "asesor-a"], estado: "Confirmada",
    });
  });
  const clientDb = env.authenticatedContext("cliente-a").firestore();
  const base = {
    clienteUid: "cliente-a", asesorUid: "asesor-a", participantUids: ["cliente-a", "asesor-a"],
    projectId: "proyecto-a", lastMessage: "", lastMessageAt: 1, updatedAt: 1,
    unreadForCliente: false, unreadForAsesor: false, active: true,
  };
  await assertFails(setDoc(doc(clientDb, "conversaciones/chat-cita-asesor-ajeno"), {
    ...base, citaId, asesorUid: "asesor-ajeno", participantUids: ["cliente-a", "asesor-ajeno"],
  }));
  await assertFails(setDoc(doc(clientDb, "conversaciones/chat-cita-inexistente"), {
    ...base, citaId: "cita-inexistente",
  }));
  const otherClientDb = env.authenticatedContext("cliente-b").firestore();
  await assertFails(setDoc(doc(otherClientDb, "conversaciones/chat-cita-cliente-ajeno"), {
    ...base, citaId,
  }));
});

test("un chat de cita cancelada conserva lectura pero bloquea mensajes y actualizaciones", async () => {
  const citaId = "cita-chat-cancelada";
  const conversationId = "appointment-chat-cancelada";
  await env.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await setDoc(doc(db, `citas/${citaId}`), {
      id: citaId, clienteId: "cliente-a", asesorId: "asesor-a", propertyId: "proyecto-a",
      participantUids: ["cliente-a", "asesor-a"], estado: "Cancelada",
    });
    await setDoc(doc(db, `conversaciones/${conversationId}`), {
      id: conversationId, citaId, clienteUid: "cliente-a", asesorUid: "asesor-a",
      participantUids: ["cliente-a", "asesor-a"], projectId: "proyecto-a",
      lastMessage: "Historial", lastMessageAt: 1, updatedAt: 1,
      unreadForCliente: false, unreadForAsesor: false, active: true,
    });
  });
  const clientDb = env.authenticatedContext("cliente-a").firestore();
  await assertSucceeds(getDoc(doc(clientDb, `conversaciones/${conversationId}`)));
  await assertFails(setDoc(doc(clientDb, "mensajes/mensaje-chat-cancelada"), {
    id: "mensaje-chat-cancelada", conversationId, senderUid: "cliente-a", receiverUid: "asesor-a",
    participantUids: ["cliente-a", "asesor-a"], text: "Hola", createdAt: 2,
  }));
  await assertFails(setDoc(doc(clientDb, `conversaciones/${conversationId}`), {
    lastMessage: "Hola", lastMessageAt: 2, updatedAt: 2,
  }, { merge: true }));
});
