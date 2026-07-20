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
import com.example.proyecto_iot.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MultipartBody;

import org.json.JSONObject;

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
        this.supabaseUrl = BuildConfig.SUPABASE_URL;
        this.publishableKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY;
        this.bucket = BuildConfig.SUPABASE_BUCKET;
    }

    public void uploadProjectImage(String projectId, Uri uri, UploadCallback callback) {
        upload("projects/" + sanitize(projectId), uri, callback);
    }

    public void uploadUserAvatar(String uid, Uri uri, UploadCallback callback) {
        upload("avatars/" + sanitizePathSegment(activeFirebaseUid(uid), false), uri, callback);
    }

    public void uploadCompanyImage(String adminId, Uri uri, UploadCallback callback) {
        upload("companies/" + sanitizePathSegment(activeFirebaseUid(adminId), false), uri, callback);
    }

    /** Uploads a proof supplied by the authenticated customer for a temporary separation. */
    public void uploadSeparationReceipt(String separationId, Uri uri, UploadCallback callback) {
        upload("separation-receipts/" + sanitizePathSegment(separationId, false), uri, callback);
    }

    private void upload(String folder, Uri uri, UploadCallback callback) {
        if (!isSupabaseConfigured()) {
            postError(callback, "Supabase no está configurado en local.properties");
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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            postError(callback, "Debes iniciar sesión para subir imágenes.");
            return;
        }
        user.getIdToken(false).addOnSuccessListener(tokenResult -> {
            String token = tokenResult.getToken();
            if (token == null || token.isEmpty()) {
                postError(callback, "No se pudo obtener el token de Firebase.");
                return;
            }
            String fileName = UUID.randomUUID() + extensionFor(mimeType, uri);
            RequestBody fileBody = RequestBody.create(bytes, MediaType.parse(mimeType));
            MultipartBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("bucket", bucket)
                    .addFormDataPart("folder", folder)
                    .addFormDataPart("file", fileName, fileBody)
                    .build();
            String endpoint = supabaseUrl + "/functions/v1/" + BuildConfig.SUPABASE_UPLOAD_FUNCTION;
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(body)
                    .addHeader("apikey", publishableKey)
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException error) {
                    postError(callback, "No se pudo conectar con Supabase: " + error.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body() == null ? "" : response.body().string();
                    if (!response.isSuccessful()) {
                        postError(callback, "Supabase rechazó la imagen (" + response.code() + "): " + responseBody);
                        return;
                    }
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        postSuccess(callback, new UploadResult(
                                json.optString("storagePath"),
                                json.optString("publicUrl"),
                                "supabase"
                        ));
                    } catch (Exception error) {
                        postError(callback, "Supabase devolvió una respuesta inválida.");
                    }
                }
            });
        }).addOnFailureListener(error ->
                postError(callback, "No se pudo validar la sesión Firebase: " + error.getMessage()));
    }

    private boolean isSupabaseConfigured() {
        return supabaseUrl.startsWith("https://")
                && !supabaseUrl.contains("example.supabase.co")
                && !publishableKey.startsWith("replace-with-")
                && publishableKey.length() > 20;
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

    private String sanitize(String value) {
        return sanitizePathSegment(value, true);
    }

    private String sanitizePathSegment(String value, boolean lowerCase) {
        if (value == null || value.trim().isEmpty()) {
            return "unknown";
        }
        String cleanValue = lowerCase ? value.toLowerCase(Locale.ROOT) : value;
        return cleanValue
                .replaceAll("[^A-Za-z0-9_-]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private String activeFirebaseUid(String fallbackUid) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getUid() != null && !user.getUid().trim().isEmpty()) {
            return user.getUid();
        }
        return fallbackUid;
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
