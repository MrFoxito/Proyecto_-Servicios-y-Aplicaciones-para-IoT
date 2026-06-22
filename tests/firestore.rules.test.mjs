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
  getDocs,
  query,
  setDoc,
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
      setDoc(doc(db, "proyectos/proyecto-a"), {
        projectId: "proyecto-a", adminId: "admin-a", empresaId: "empresa-a",
      }),
      setDoc(doc(db, "proyectos_tipologias/proyecto-a_tipologia_1"), {
        projectId: "proyecto-a", nombre: "Tipo A",
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

test("un usuario no puede autoconcederse rol admin sin invitacion", async () => {
  const db = env.authenticatedContext("cliente-x", { email: "cliente@example.com" }).firestore();
  await assertFails(setDoc(doc(db, "usuarios/cliente-x"), {
    uid: "cliente-x",
    rol: "admin",
    empresaId: "empresa-a",
  }));
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
