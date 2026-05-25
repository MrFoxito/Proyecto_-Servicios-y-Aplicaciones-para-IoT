package com.example.proyecto_iot.superadmin.notifications;

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
import com.example.proyecto_iot.superadmin.SuperadminGestionUsuariosActivity;
import com.example.proyecto_iot.superadmin.SuperadminAprobacionAsesoresActivity;

public class SuperadminNotificationHelper {
    private static final String CHANNEL_ID = "superadmin_events_channel";
    private static final String CHANNEL_NAME = "Eventos Superadmin";
    private static final String CHANNEL_DESCRIPTION = "Avisos locales de acciones realizadas por el superadministrador";
    private static final int REQUEST_NOTIFICATIONS_CODE = 5301;

    private SuperadminNotificationHelper() {}

    public static void setup(AppCompatActivity activity) {
        createNotificationChannel(activity);
        requestPermissionIfNeeded(activity);
    }

    public static void showAdminRegisteredNotification(Context context, String adminName) {
        Intent intent = new Intent(context, SuperadminGestionUsuariosActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        showNotification(
                context,
                "Administrador registrado",
                "El administrador " + adminName + " ha sido registrado exitosamente.",
                intent,
                ("admin_reg_" + adminName).hashCode()
        );
    }

    public static void showAdvisorRequestApprovedNotification(Context context, String advisorName) {
        Intent intent = new Intent(context, SuperadminGestionUsuariosActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        showNotification(
                context,
                "Asesor aprobado",
                "La solicitud del asesor " + advisorName + " ha sido aprobada.",
                intent,
                ("adv_appr_" + advisorName).hashCode()
        );
    }

    public static void showAdvisorRequestRejectedNotification(Context context, String advisorName) {
        Intent intent = new Intent(context, SuperadminAprobacionAsesoresActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        showNotification(
                context,
                "Asesor rechazado",
                "La solicitud del asesor " + advisorName + " ha sido rechazada.",
                intent,
                ("adv_rej_" + advisorName).hashCode()
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
