package com.example.proyecto_iot.data;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SystemLogger {

    public static void logEvent(String tipo, String nivel, String titulo, String subtitulo, String resumen, String detalle) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES"));
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

        Map<String, Object> log = new HashMap<>();
        log.put("tipo", tipo != null ? tipo : "sistema");
        log.put("nivel", nivel != null ? nivel : "info");
        log.put("titulo", titulo != null ? titulo : "");
        log.put("subtitulo", subtitulo != null ? subtitulo : "");
        log.put("resumen", resumen != null ? resumen : "");
        log.put("detalle", detalle != null ? detalle : "");
        
        log.put("fecha", dateFormat.format(now));
        log.put("tiempo", timeFormat.format(now));
        log.put("dateIso", isoFormat.format(now));
        log.put("timestamp", System.currentTimeMillis());

        firestore.collection("logs_sistema").add(log)
            .addOnSuccessListener(docRef -> android.util.Log.d("SYSTEM_LOGGER", "Log written with ID: " + docRef.getId()))
            .addOnFailureListener(e -> android.util.Log.e("SYSTEM_LOGGER", "Error writing log: ", e));
    }
}
