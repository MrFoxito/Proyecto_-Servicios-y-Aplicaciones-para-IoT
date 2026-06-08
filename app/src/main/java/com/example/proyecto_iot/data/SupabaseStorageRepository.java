package com.example.proyecto_iot.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import com.example.proyecto_iot.R;

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

        public UploadResult(String storagePath, String publicUrl) {
            this.storagePath = storagePath;
            this.publicUrl = publicUrl;
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
                postError(callback, "No se pudo subir la imagen a Supabase");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() == null ? "" : response.body().string();
                if (!response.isSuccessful()) {
                    postError(callback, supabaseError(response.code(), responseBody));
                    return;
                }
                postSuccess(callback, new UploadResult(storagePath, publicUrl(storagePath)));
            }
        });
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
