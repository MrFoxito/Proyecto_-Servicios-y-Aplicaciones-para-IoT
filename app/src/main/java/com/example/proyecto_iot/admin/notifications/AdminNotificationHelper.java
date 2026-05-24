package com.example.proyecto_iot.admin.notifications;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.AdminHistorialAsignacionesActivity;
import com.example.proyecto_iot.admin.AdminProyectosActivity;
import com.example.proyecto_iot.admin.model.AdminAssignmentRecord;

public class AdminNotificationHelper {
    private static final String CHANNEL_ID = "admin_events_channel";
    private static final String CHANNEL_NAME = "Eventos Admin";
    private static final String CHANNEL_DESCRIPTION = "Avisos locales de acciones realizadas por el administrador";
    private static final int REQUEST_NOTIFICATIONS_CODE = 5201;

    private AdminNotificationHelper() {
    }

    public static void setup(AppCompatActivity activity) {
        createNotificationChannel(activity);
        requestPermissionIfNeeded(activity);
    }

    public static void showAssignmentNotification(Context context, AdminAssignmentRecord record) {
        Intent intent = new Intent(context, AdminHistorialAsignacionesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        String title = "Asesor asignado";
        String content = record.getAdvisorName() + " fue asignada a " + record.getProjectTitle();
        showNotification(context, title, content, intent, record.getId().hashCode());
    }

    public static void showProjectPublishedNotification(Context context, String projectName) {
        Intent intent = new Intent(context, AdminProyectosActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        showNotification(
                context,
                "Proyecto publicado",
                projectName.isEmpty() ? "El nuevo proyecto fue publicado correctamente." : projectName + " fue publicado correctamente.",
                intent,
                ("published_" + projectName).hashCode()
        );
    }

    public static void showProjectEditedNotification(Context context, String projectName) {
        Intent intent = new Intent(context, AdminProyectosActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        showNotification(
                context,
                "Cambios guardados",
                projectName.isEmpty() ? "El proyecto fue actualizado correctamente." : projectName + " fue actualizado correctamente.",
                intent,
                ("edited_" + projectName + System.currentTimeMillis()).hashCode()
        );
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(CHANNEL_DESCRIPTION);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    private static void requestPermissionIfNeeded(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATIONS_CODE
            );
        }
    }

    private static void showNotification(Context context, String title, String content, Intent intent, int notificationId) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_bell)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }
}
