package com.example.proyecto_iot;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * Helper centralizado para lanzar notificaciones del sistema Android.
 *
 * Canales definidos:
 *  - CHANNEL_CITAS    : confirmaciones de visitas agendadas
 *  - CHANNEL_PAGOS    : confirmaciones de separaciones / pagos
 *
 * Uso:
 *   NotificationHelper.init(context);          // llamar una vez al arrancar la app
 *   NotificationHelper.notifyCita(context, "Villa Luminara", "26 Oct 2026", "11:30 AM");
 *   NotificationHelper.notifyPago(context, "Villa Luminara", "USD 1.2M");
 */
public final class NotificationHelper {

    // IDs de canal (requeridos desde Android 8.0 / API 26)
    public static final String CHANNEL_CITAS = "channel_citas_high";
    public static final String CHANNEL_PAGOS = "channel_pagos";

    // IDs únicos de notificación — se incrementan para no sobreescribir
    private static int notifId = 1000;

    private NotificationHelper() {}

    /**
     * Crea los canales de notificación.
     * Debe llamarse una vez al iniciar la app (en MainActivity o Application).
     * En Android < 8.0 no hace nada.
     */
    public static void init(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null) return;

            // Canal de citas
            NotificationChannel citasChannel = new NotificationChannel(
                    CHANNEL_CITAS,
                    "Citas y visitas",
                    NotificationManager.IMPORTANCE_HIGH
            );
            citasChannel.setDescription("Confirmaciones y recordatorios de visitas agendadas");
            manager.createNotificationChannel(citasChannel);

            // Canal de pagos / separaciones
            NotificationChannel pagosChannel = new NotificationChannel(
                    CHANNEL_PAGOS,
                    "Pagos y separaciones",
                    NotificationManager.IMPORTANCE_HIGH
            );
            pagosChannel.setDescription("Confirmaciones de separaciones y pagos procesados");
            manager.createNotificationChannel(pagosChannel);
        }
    }

    /**
     * Lanza una notificación push cuando el usuario confirma una cita.
     *
     * @param context       contexto de la activity
     * @param propertyTitle nombre de la propiedad
     * @param date          fecha seleccionada (ej. "26 Oct 2026")
     * @param time          hora seleccionada (ej. "11:30 AM")
     * @param tapIntent     intent que se abre al tocar la notificación (puede ser null)
     */
    public static void notifyCita(Context context, String propertyTitle,
                                   String date, String time, Intent tapIntent) {
        String title = "Cita registrada";
        String body  = propertyTitle + " · " + date + " a las " + time;
        launch(context, CHANNEL_CITAS, title, body, tapIntent);
    }

    /**
     * Lanza una notificación push cuando el usuario completa una separación/pago.
     *
     * @param context       contexto de la activity
     * @param propertyTitle nombre de la propiedad
     * @param amount        monto pagado (ej. "USD 1.2M")
     * @param tapIntent     intent que se abre al tocar la notificación (puede ser null)
     */
    public static void notifyPago(Context context, String propertyTitle,
                                   String amount, Intent tapIntent) {
        String title = "Separación en proceso";
        String body  = propertyTitle + " · Monto: " + amount + ". En revisión.";
        launch(context, CHANNEL_PAGOS, title, body, tapIntent);
    }

    // -------------------------------------------------------------------------
    // Privado
    // -------------------------------------------------------------------------

    private static void launch(Context context, String channelId,
                                String title, String body, Intent tapIntent) {
        // Verificar permiso en Android 13+ (API 33)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permiso no concedido — no lanzamos la notificación push
                // (la notificación in-app sí se guarda igual en el storage)
                return;
            }
        }

        // PendingIntent para abrir la app al tocar la notificación
        PendingIntent pendingIntent = null;
        if (tapIntent != null) {
            tapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            pendingIntent = PendingIntent.getActivity(context, notifId, tapIntent, flags);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_user_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(CHANNEL_CITAS.equals(channelId)
                        ? NotificationCompat.PRIORITY_HIGH
                        : NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }

        NotificationManagerCompat.from(context).notify(notifId++, builder.build());
    }
}
