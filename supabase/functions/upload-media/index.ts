import { createClient } from "npm:@supabase/supabase-js@2.49.4";
import { requireFirebaseIdentity } from "../_shared/firebase-auth.ts";

const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, apikey, content-type",
};

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") return new Response("ok", { headers: cors });
  try {
    const identity = await requireFirebaseIdentity(request);
    const form = await request.formData();
    const bucket = String(form.get("bucket") ?? "");
    const folder = cleanPath(String(form.get("folder") ?? ""));
    const file = form.get("file");
    if (bucket !== "app-images") throw new Error("Bucket no permitido.");
    if (!(file instanceof File) || file.size === 0) throw new Error("Archivo vacío.");
    if (!file.type.startsWith("image/")) throw new Error("Solo se permiten imágenes.");
    if (file.size > 8 * 1024 * 1024) throw new Error("La imagen supera 8 MB.");
    authorizeFolder(identity, folder);

    const extension = safeExtension(file.name);
    const storagePath = `${folder}/${crypto.randomUUID()}${extension}`;
    const client = createClient(
      Deno.env.get("SUPABASE_URL")!,
      Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
      { auth: { persistSession: false } },
    );
    const bytes = new Uint8Array(await file.arrayBuffer());
    const { error } = await client.storage.from(bucket).upload(storagePath, bytes, {
      contentType: file.type,
      cacheControl: "3600",
      upsert: false,
    });
    if (error) throw error;
    const { data } = client.storage.from(bucket).getPublicUrl(storagePath);
    return json({ storagePath, publicUrl: data.publicUrl, provider: "supabase" });
  } catch (error) {
    return json({ error: error instanceof Error ? error.message : "Error desconocido" }, 400);
  }
});

function authorizeFolder(identity: { uid: string; role: string }, folder: string) {
  if (folder === `avatars/${identity.uid}` || folder === `companies/${identity.uid}`) return;
  if (folder.startsWith("projects/") && ["admin", "superadmin"].includes(identity.role)) return;
  throw new Error("No tienes permiso para cargar en esta ruta.");
}

function cleanPath(value: string): string {
  return value.split("/").map((part) => part.replace(/[^a-zA-Z0-9_-]/g, "")).filter(Boolean).join("/");
}

function safeExtension(name: string): string {
  const match = name.toLowerCase().match(/\.(jpg|jpeg|png|webp)$/);
  return match ? match[0] : ".jpg";
}

function json(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...cors, "Content-Type": "application/json" },
  });
}
