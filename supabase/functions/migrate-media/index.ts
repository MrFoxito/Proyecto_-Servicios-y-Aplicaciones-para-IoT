import { createClient } from "npm:@supabase/supabase-js@2.49.4";
import { requireFirebaseIdentity } from "../_shared/firebase-auth.ts";

Deno.serve(async (request) => {
  try {
    const identity = await requireFirebaseIdentity(request);
    if (identity.role !== "superadmin") throw new Error("Solo superadmin puede migrar imágenes.");
    const body = await request.json();
    const source = String(body.source ?? "");
    const projectId = String(body.projectId ?? "").replace(/[^a-zA-Z0-9_-]/g, "");
    if (!source || !projectId) throw new Error("Solicitud de migración incompleta.");

    const decoded = await readSource(source);
    const client = createClient(
      Deno.env.get("SUPABASE_URL")!,
      Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
      { auth: { persistSession: false } },
    );
    const storagePath = `projects/${projectId}/${crypto.randomUUID()}.${decoded.extension}`;
    const { error } = await client.storage.from("app-images").upload(storagePath, decoded.bytes, {
      contentType: decoded.contentType,
      cacheControl: "3600",
      upsert: false,
    });
    if (error) throw error;
    const { data } = client.storage.from("app-images").getPublicUrl(storagePath);
    return Response.json({ storagePath, publicUrl: data.publicUrl, provider: "supabase" });
  } catch (error) {
    return Response.json({ error: error instanceof Error ? error.message : "Error desconocido" }, { status: 400 });
  }
});

async function readSource(source: string) {
  if (source.startsWith("data:image/")) {
    const match = source.match(/^data:image\/(jpeg|jpg|png|webp);base64,(.+)$/);
    if (!match) throw new Error("Imagen inline inválida.");
    const binary = atob(match[2]);
    return {
      bytes: Uint8Array.from(binary, (char) => char.charCodeAt(0)),
      contentType: `image/${match[1] === "jpg" ? "jpeg" : match[1]}`,
      extension: match[1] === "jpeg" ? "jpg" : match[1],
    };
  }
  const response = await fetch(source);
  if (!response.ok) throw new Error("No se pudo descargar la imagen anterior.");
  const contentType = response.headers.get("content-type") ?? "image/jpeg";
  if (!contentType.startsWith("image/")) throw new Error("La URL anterior no contiene una imagen.");
  return {
    bytes: new Uint8Array(await response.arrayBuffer()),
    contentType,
    extension: contentType.includes("png") ? "png" : contentType.includes("webp") ? "webp" : "jpg",
  };
}
