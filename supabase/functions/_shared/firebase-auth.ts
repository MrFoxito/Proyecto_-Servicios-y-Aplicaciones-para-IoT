import { createRemoteJWKSet, jwtVerify } from "npm:jose@5.9.6";

const firebaseProjectId = Deno.env.get("FIREBASE_PROJECT_ID") ?? "iot-g3-c3fa2";
const jwks = createRemoteJWKSet(
  new URL("https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com"),
);

export type FirebaseIdentity = {
  uid: string;
  email: string;
  role: string;
  empresaId: string;
};

export async function requireFirebaseIdentity(request: Request): Promise<FirebaseIdentity> {
  const authorization = request.headers.get("Authorization") ?? "";
  const token = authorization.startsWith("Bearer ") ? authorization.slice(7) : "";
  if (!token) throw new Error("Falta el Firebase ID token.");

  const { payload } = await jwtVerify(token, jwks, {
    audience: firebaseProjectId,
    issuer: `https://securetoken.google.com/${firebaseProjectId}`,
  });
  const uid = String(payload.sub ?? "");
  if (!uid) throw new Error("Token Firebase sin UID.");

  const profileResponse = await fetch(
    `https://firestore.googleapis.com/v1/projects/${firebaseProjectId}/databases/(default)/documents/usuarios/${uid}`,
    { headers: { Authorization: `Bearer ${token}` } },
  );
  if (!profileResponse.ok) throw new Error("No se pudo validar el perfil Firestore.");
  const profile = await profileResponse.json();
  const fields = profile.fields ?? {};
  return {
    uid,
    email: String(payload.email ?? ""),
    role: firestoreString(fields.rol),
    empresaId: firestoreString(fields.empresaId) || firestoreString(fields.inmobiliariaId),
  };
}

function firestoreString(value: Record<string, unknown> | undefined): string {
  return typeof value?.stringValue === "string" ? value.stringValue : "";
}
