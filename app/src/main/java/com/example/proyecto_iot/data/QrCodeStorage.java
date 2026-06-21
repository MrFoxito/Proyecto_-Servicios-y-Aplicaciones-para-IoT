package com.example.proyecto_iot.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.OutputStream;

public final class QrCodeStorage {
    private QrCodeStorage() {
    }

    public static Uri saveToPictures(Context context, Bitmap bitmap, String projectId) throws Exception {
        String safeId = projectId == null ? "proyecto" : projectId.replaceAll("[^a-zA-Z0-9_-]", "_");
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "qr_" + safeId + ".png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ProyectoIoT");
        values.put(MediaStore.Images.Media.IS_PENDING, 1);

        ContentResolver resolver = context.getContentResolver();
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            throw new IllegalStateException("No se pudo crear el archivo del QR.");
        }
        try (OutputStream stream = resolver.openOutputStream(uri)) {
            if (stream == null || !bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                throw new IllegalStateException("No se pudo escribir la imagen del QR.");
            }
        } catch (Exception error) {
            resolver.delete(uri, null, null);
            throw error;
        }

        values.clear();
        values.put(MediaStore.Images.Media.IS_PENDING, 0);
        resolver.update(uri, values, null, null);
        return uri;
    }
}
