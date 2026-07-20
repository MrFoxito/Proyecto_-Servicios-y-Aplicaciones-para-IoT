package com.example.proyecto_iot.data;

import android.content.Context;
import android.net.Uri;

/**
 * Single application entry point for new image uploads.
 *
 * Firebase remains responsible for authentication and metadata while the
 * underlying repository sends image bytes only to the protected Supabase
 * Edge Function.
 */
public final class ProjectMediaRepository {
    private final SupabaseStorageRepository storageRepository;

    public ProjectMediaRepository(Context context) {
        storageRepository = new SupabaseStorageRepository(context);
    }

    public void uploadProjectImage(
            String projectId,
            Uri imageUri,
            SupabaseStorageRepository.UploadCallback callback
    ) {
        storageRepository.uploadProjectImage(projectId, imageUri, callback);
    }

    public void uploadUserAvatar(
            String userId,
            Uri imageUri,
            SupabaseStorageRepository.UploadCallback callback
    ) {
        storageRepository.uploadUserAvatar(userId, imageUri, callback);
    }

    public void uploadCompanyImage(
            String adminId,
            Uri imageUri,
            SupabaseStorageRepository.UploadCallback callback
    ) {
        storageRepository.uploadCompanyImage(adminId, imageUri, callback);
    }

    public void uploadSeparationReceipt(
            String separationId,
            Uri receiptUri,
            SupabaseStorageRepository.UploadCallback callback
    ) {
        storageRepository.uploadSeparationReceipt(separationId, receiptUri, callback);
    }
}
