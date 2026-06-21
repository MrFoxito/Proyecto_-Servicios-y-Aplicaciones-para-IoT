package com.example.proyecto_iot.data;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.webkit.MimeTypeMap;

import com.example.proyecto_iot.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseStorageRepository {
    private static volatile boolean firebaseStorageUnavailable;
    private final Context context;
    private final OkHttpClient client = new OkHttpClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final String supabaseUrl;
    private final String publishableKey;
    private final String bucket;

    public interface UploadCallback {
        void onSuccess(UploadResult result);
        void onError(String message);
    }

    public static class UploadResult {
        public final String storagePath;
        public final String publicUrl;
        public final String provider;

        public UploadResult(String storagePath, String publicUrl) {
            this(storagePath, publicUrl, "supabase");
        }

        public UploadResult(String storagePath, String publicUrl, String provider) {
            this.storagePath = storagePath;
            this.publicUrl = publicUrl;
            this.provider = provider;
        }
    }

    public SupabaseStorageRepository(Context context) {
        this.context = context.getApplicationContext();
        this.supabaseUrl = this.context.getString(R.string.supabase_url);
        this.publishableKey = this.context.getString(R.string.supabase_publishable_key);
        this.bucket = this.context.getString(R.string.supabase_storage_bucket);
    }

    public void uploadProjectImage(String projectId, Uri uri, UploadCallback callback) {
        upload("projects/" + sanitize(projectId), uri, callback);
    }

    public void uploadUserAvatar(String uid, Uri uri, UploadCallback callback) {
        upload("avatars/" + sanitize(uid), uri, callback);
    }

    public void uploadCompanyImage(String adminId, Uri uri, UploadCallback callback) {
        upload("companies/" + sanitize(adminId), uri, callback);
    }

    private void upload(String folder, Uri uri, UploadCallback callback) {
        if (!isSupabaseConfigured()) {
            uploadToFirebase(folder, uri, callback);
            return;
        }
        byte[] bytes;
        String mimeType = resolveMimeType(uri);
        try {
            bytes = readBytes(uri);
        } catch (IOException error) {
            postError(callback, "No se pudo leer la imagen seleccionada");
            return;
        }

        String fileName = UUID.randomUUID() + extensionFor(mimeType, uri);
        String storagePath = folder + "/" + fileName;
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + encodePath(storagePath);
        RequestBody body = RequestBody.create(bytes, MediaType.parse(mimeType));

        Request request = new Request.Builder()
                .url(uploadUrl)
                .post(body)
                .addHeader("apikey", publishableKey)
                .addHeader("Authorization", "Bearer " + publishableKey)
                .addHeader("Content-Type", mimeType)
                .addHeader("Cache-Control", "3600")
                .addHeader("x-upsert", "true")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                uploadToFirebase(folder, uri, callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() == null ? "" : response.body().string();
                if (!response.isSuccessful()) {
                    uploadToFirebase(folder, uri, callback);
                    return;
                }
                postSuccess(callback, new UploadResult(storagePath, publicUrl(storagePath), "supabase"));
            }
        });
    }

    private boolean isSupabaseConfigured() {
        return supabaseUrl.startsWith("https://")
                && !supabaseUrl.contains("example.supabase.co")
                && !publishableKey.startsWith("replace-with-")
                && publishableKey.length() > 20;
    }

    private void uploadToFirebase(String folder, Uri uri, UploadCallback callback) {
        if (firebaseStorageUnavailable) {
            new Thread(() -> createInlineFirestoreImage(folder, uri, callback)).start();
            return;
        }
        String mimeType = resolveMimeType(uri);
        String fileName = UUID.randomUUID() + extensionFor(mimeType, uri);
        String storagePath = "project-images/" + folder + "/" + fileName;
        StorageReference reference = FirebaseStorage.getInstance().getReference().child(storagePath);
        reference.putFile(uri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        Exception error = task.getException();
                        throw error == null ? new IOException("Firebase Storage rechazo la imagen") : error;
                    }
                    return reference.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri ->
                        postSuccess(callback, new UploadResult(
                                storagePath,
                                downloadUri.toString(),
                                "firebase"
                        )))
                .addOnFailureListener(error -> {
                    firebaseStorageUnavailable = true;
                    new Thread(() -> createInlineFirestoreImage(folder, uri, callback)).start();
                });
    }

    private void createInlineFirestoreImage(String folder, Uri uri, UploadCallback callback) {
        try {
            byte[] source = readBytes(uri);
            Bitmap original = BitmapFactory.decodeByteArray(source, 0, source.length);
            if (original == null) {
                postError(callback, "La imagen seleccionada no tiene un formato compatible");
                return;
            }
            Bitmap scaled = scaleDown(original, 1280);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            int quality = 82;
            do {
                output.reset();
                scaled.compress(Bitmap.CompressFormat.JPEG, quality, output);
                quality -= 8;
            } while (output.size() > 450_000 && quality >= 42);

            if (output.size() > 650_000) {
                postError(callback, "La imagen es demasiado grande incluso despues de comprimirla");
                return;
            }
            String storagePath = "firestore-inline/" + folder + "/" + UUID.randomUUID() + ".jpg";
            String dataUrl = "data:image/jpeg;base64,"
                    + Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP);
            postSuccess(callback, new UploadResult(storagePath, dataUrl, "firestore"));
            if (scaled != original) {
                scaled.recycle();
            }
            original.recycle();
        } catch (Exception error) {
            postError(callback, "No se pudo preparar la imagen para guardarla: "
                    + (error.getMessage() == null ? "error desconocido" : error.getMessage()));
        }
    }

    private Bitmap scaleDown(Bitmap source, int maxDimension) {
        int width = source.getWidth();
        int height = source.getHeight();
        int largest = Math.max(width, height);
        if (largest <= maxDimension) {
            return source;
        }
        float ratio = maxDimension / (float) largest;
        return Bitmap.createScaledBitmap(
                source,
                Math.max(1, Math.round(width * ratio)),
                Math.max(1, Math.round(height * ratio)),
                true
        );
    }

    private byte[] readBytes(Uri uri) throws IOException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new IOException("InputStream null");
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            return outputStream.toByteArray();
        }
    }

    private String resolveMimeType(Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        return mimeType == null || mimeType.trim().isEmpty() ? "image/jpeg" : mimeType;
    }

    private String extensionFor(String mimeType, Uri uri) {
        String displayName = displayName(uri);
        int dot = displayName.lastIndexOf('.');
        if (dot >= 0 && dot < displayName.length() - 1) {
            return displayName.substring(dot).toLowerCase(Locale.ROOT);
        }
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        return extension == null || extension.trim().isEmpty() ? ".jpg" : "." + extension;
    }

    private String displayName(Uri uri) {
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    return cursor.getString(nameIndex);
                }
            }
        } catch (Exception ignored) {
            // File extension fallback handles this case.
        }
        return "";
    }

    private String publicUrl(String storagePath) {
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + encodePath(storagePath);
    }

    private String encodePath(String value) {
        String[] parts = value.split("/");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                builder.append('/');
            }
            builder.append(URLEncoder.encode(parts[i], StandardCharsets.UTF_8).replace("+", "%20"));
        }
        return builder.toString();
    }

    private String sanitize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "unknown";
        }
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_-]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private String supabaseError(int code, String responseBody) {
        if (code == 404) {
            return "No existe el bucket " + bucket + " en Supabase Storage. Respuesta: " + responseBody;
        }
        if (code == 401 || code == 403) {
            return "Supabase no permite subir imagenes al bucket " + bucket + ": revisa credenciales y politicas. Respuesta: " + responseBody;
        }
        return "Supabase Storage rechazo la imagen (" + code + "): " + responseBody;
    }

    private void postSuccess(UploadCallback callback, UploadResult result) {
        mainHandler.post(() -> callback.onSuccess(result));
    }

    private void postError(UploadCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
