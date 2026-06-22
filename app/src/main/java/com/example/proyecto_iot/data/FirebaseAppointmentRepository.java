package com.example.proyecto_iot.data;

import androidx.annotation.Nullable;

import com.example.proyecto_iot.entity.Cita;
import com.example.proyecto_iot.entity.EventoCita;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class FirebaseAppointmentRepository {

    public static final String[] SLOT_KEYS = {"09:00", "10:00", "11:00", "12:00", "15:00", "16:00", "17:00"};
    public static final int DEFAULT_DURATION_MINUTES = 60;
    public static final int DEFAULT_SLOT_CAPACITY = 1;

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public interface AdvisorCallback {
        void onSuccess(Advisor advisor);
        void onError(String message);
    }

    public interface SlotsCallback {
        void onSuccess(Set<String> occupiedSlotKeys);
        void onError(String message);
    }

    public interface AvailableSlotsCallback {
        void onSuccess(List<String> availableSlotKeys, Availability availability);
        void onError(String message);
    }

    public interface AppointmentsCallback {
        void onSuccess(List<Cita> citas);
        void onError(String message);
    }

    public interface AppointmentCallback {
        void onSuccess(String citaId);
        void onError(String message);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String message);
    }

    private interface AvailabilityCallback {
        void onSuccess(Availability availability);
        void onError(String message);
    }

    public static class Advisor {
        public final String uid;
        public final String name;

        public Advisor(String uid, String name) {
            this.uid = uid;
            this.name = name;
        }
    }

    public static class Availability {
        public final List<String> slotKeys;
        public final Set<Integer> workingDays;
        public final int durationMinutes;
        public final int capacity;

        public Availability(List<String> slotKeys, Set<Integer> workingDays, int durationMinutes, int capacity) {
            this.slotKeys = slotKeys;
            this.workingDays = workingDays;
            this.durationMinutes = durationMinutes;
            this.capacity = capacity;
        }

        public boolean isWorkingDay(String fechaISO) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(format.parse(fechaISO));
                return workingDays.contains(calendar.get(Calendar.DAY_OF_WEEK));
            } catch (Exception ignored) {
                return false;
            }
        }

        public boolean containsSlot(String slotKey) {
            return slotKeys.contains(slotKey.replace("_", ":")) || slotKeys.contains(slotKey.replace(":", "_"));
        }
    }

    public static class AppointmentDraft {
        public String clienteId;
        public String clienteNombre;
        public String asesorId;
        public String asesorNombre;
        public String propertyId;
        public String inmuebleNombre;
        public String proyectoNombre;
        public String fechaISO;
        public String fechaTexto;
        public String hora;
        public String meetingPoint;
        public String nota;
        public String imageKey;
    }

    public void getFirstActiveAdvisor(AdvisorCallback callback) {
        firestore.collection("usuarios")
                .whereEqualTo("rol", "asesor")
                .whereEqualTo("estado", "activo")
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        callback.onError("No hay asesores activos disponibles.");
                        return;
                    }
                    DocumentSnapshot document = snapshot.getDocuments().get(0);
                    callback.onSuccess(new Advisor(
                            firstNonEmpty(document.getString("uid"), document.getId()),
                            displayName(document)
                    ));
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudo buscar asesor activo: " + safeMessage(error)));
    }

    public void getAdvisorForProject(String propertyId, AdvisorCallback callback) {
        String normalizedPropertyId = firstNonEmpty(propertyId);
        if (normalizedPropertyId.isEmpty()) {
            getFirstActiveAdvisor(callback);
            return;
        }

        firestore.collection("asignaciones")
                .whereEqualTo("propertyId", normalizedPropertyId)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        resolveAdvisorFromAssignment(snapshot.getDocuments().get(0), callback);
                        return;
                    }
                    firestore.collection("asignaciones")
                            .whereEqualTo("proyectoId", normalizedPropertyId)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(projectSnapshot -> {
                                if (projectSnapshot.isEmpty()) {
                                    getFirstActiveAdvisor(callback);
                                } else {
                                    resolveAdvisorFromAssignment(projectSnapshot.getDocuments().get(0), callback);
                                }
                            })
                            .addOnFailureListener(error -> getFirstActiveAdvisor(callback));
                })
                .addOnFailureListener(error -> getFirstActiveAdvisor(callback));
    }

    public void getOccupiedSlots(String asesorId, String fechaISO, SlotsCallback callback) {
        firestore.collection("citas_slots")
                .whereEqualTo("asesorId", asesorId)
                .whereEqualTo("fechaISO", fechaISO)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Set<String> occupied = new HashSet<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        int capacity = intValue(document.get("capacidadMaxima"), DEFAULT_SLOT_CAPACITY);
                        int reserved = intValue(document.get("reservedCount"), document.exists() ? 1 : 0);
                        String status = firstNonEmpty(document.getString("estado"), "ocupado");
                        String slotKey = firstNonEmpty(document.getString("slotKey"));
                        if (!slotKey.isEmpty() && reserved >= capacity && !"libre".equalsIgnoreCase(status)) {
                            occupied.add(slotKey);
                        }
                    }
                    callback.onSuccess(occupied);
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudieron consultar horarios ocupados: " + safeMessage(error)));
    }

    public void getAvailableSlots(String asesorId, String propertyId, String fechaISO, AvailableSlotsCallback callback) {
        loadAvailability(asesorId, propertyId, new AvailabilityCallback() {
            @Override
            public void onSuccess(Availability availability) {
                if (!availability.isWorkingDay(fechaISO)) {
                    callback.onSuccess(new ArrayList<>(), availability);
                    return;
                }
                firestore.collection("citas_slots")
                        .whereEqualTo("asesorId", asesorId)
                        .whereEqualTo("fechaISO", fechaISO)
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            Set<String> fullSlots = new HashSet<>();
                            for (DocumentSnapshot document : snapshot.getDocuments()) {
                                int capacity = intValue(document.get("capacidadMaxima"), availability.capacity);
                                int reserved = intValue(document.get("reservedCount"), document.exists() ? 1 : 0);
                                String status = firstNonEmpty(document.getString("estado"), "ocupado");
                                String slotKey = firstNonEmpty(document.getString("slotKey"));
                                if (!slotKey.isEmpty() && reserved >= capacity && !"libre".equalsIgnoreCase(status)) {
                                    fullSlots.add(slotKey);
                                }
                            }
                            List<String> available = new ArrayList<>();
                            for (String slot : availability.slotKeys) {
                                String slotKey = slotKey(slot);
                                if (!fullSlots.contains(slotKey)) {
                                    available.add(slot);
                                }
                            }
                            callback.onSuccess(available, availability);
                        })
                        .addOnFailureListener(error ->
                                callback.onError("No se pudieron consultar horarios disponibles: " + safeMessage(error)));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void reserveAppointment(AppointmentDraft draft, AppointmentCallback callback) {
        loadAvailability(draft.asesorId, draft.propertyId, new AvailabilityCallback() {
            @Override
            public void onSuccess(Availability availability) {
                String normalizedSlot = slotKey(draft.hora);
                if (!availability.isWorkingDay(draft.fechaISO)) {
                    callback.onError("El asesor no atiende citas en la fecha seleccionada.");
                    return;
                }
                if (!availability.containsSlot(normalizedSlot)) {
                    callback.onError("El horario seleccionado no esta dentro de la disponibilidad configurada.");
                    return;
                }
                reserveAppointmentWithAvailability(draft, availability, callback);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void rescheduleAppointment(String citaId, String nuevaFechaISO, String nuevaFechaTexto,
                                      String nuevaHora, String motivo, OperationCallback callback) {
        long now = System.currentTimeMillis();
        DocumentReference citaRef = firestore.collection("citas").document(citaId);

        firestore.runTransaction(transaction -> {
                    DocumentSnapshot citaSnapshot = transaction.get(citaRef);
                    if (!citaSnapshot.exists()) {
                        throw abort("La cita ya no existe.");
                    }

                    String asesorId = firstNonEmpty(citaSnapshot.getString("asesorId"));
                    String clienteId = firstNonEmpty(citaSnapshot.getString("clienteId"));
                    String propertyId = firstNonEmpty(citaSnapshot.getString("propertyId"));
                    String oldSlotId = firstNonEmpty(
                            citaSnapshot.getString("slotId"),
                            slotId(asesorId, firstNonEmpty(citaSnapshot.getString("fechaISO")), firstNonEmpty(citaSnapshot.getString("slotKey")))
                    );
                    String newSlotKey = slotKey(nuevaHora);
                    String newSlotId = slotId(asesorId, nuevaFechaISO, newSlotKey);

                    DocumentReference oldSlotRef = firestore.collection("citas_slots").document(oldSlotId);
                    DocumentReference newSlotRef = firestore.collection("citas_slots").document(newSlotId);
                    DocumentSnapshot newSlot = transaction.get(newSlotRef);
                    if (newSlot.exists()) {
                        int capacity = intValue(newSlot.get("capacidadMaxima"), DEFAULT_SLOT_CAPACITY);
                        int reserved = intValue(newSlot.get("reservedCount"), 1);
                        if (reserved >= capacity) {
                            throw abort("El nuevo horario ya esta ocupado.");
                        }
                    }

                    if (!oldSlotId.equals(newSlotId)) {
                        DocumentSnapshot oldSlot = transaction.get(oldSlotRef);
                        releaseSlot(transaction, oldSlotRef, oldSlot, citaId, clienteId);
                        reserveSlot(transaction, newSlotRef, newSlot, citaId, clienteId, asesorId, propertyId,
                                nuevaFechaISO, nuevaHora, newSlotKey, DEFAULT_DURATION_MINUTES, DEFAULT_SLOT_CAPACITY, now);
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("fechaISO", nuevaFechaISO);
                    updates.put("fechaTexto", nuevaFechaTexto);
                    updates.put("hora", nuevaHora);
                    updates.put("slotKey", newSlotKey);
                    updates.put("slotId", newSlotId);
                    updates.put("estado", "Reprogramada");
                    updates.put("rescheduleReason", firstNonEmpty(motivo));
                    updates.put("updatedAt", now);
                    transaction.update(citaRef, updates);

                    DocumentReference eventRef = firestore.collection("eventos_cita")
                            .document("evt_" + citaId + "_rescheduled_" + now);
                    transaction.set(eventRef, eventMap("Cita reprogramada",
                            "Nuevo horario: " + nuevaFechaTexto + " " + nuevaHora + ". Motivo: " + firstNonEmpty(motivo, "Sin motivo registrado"),
                            "REPROGRAMADA", citaId, clienteId, asesorId, nuevaFechaISO, newSlotKey, now), SetOptions.merge());
                    return null;
                })
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(error -> callback.onError(safeMessage((Exception) error)));
    }

    public void cancelAppointment(String citaId, String reason, OperationCallback callback) {
        long now = System.currentTimeMillis();
        DocumentReference citaRef = firestore.collection("citas").document(citaId);

        firestore.runTransaction(transaction -> {
                    DocumentSnapshot citaSnapshot = transaction.get(citaRef);
                    if (!citaSnapshot.exists()) {
                        throw abort("La cita ya no existe.");
                    }

                    String asesorId = firstNonEmpty(citaSnapshot.getString("asesorId"));
                    String clienteId = firstNonEmpty(citaSnapshot.getString("clienteId"));
                    String slotId = firstNonEmpty(
                            citaSnapshot.getString("slotId"),
                            slotId(asesorId, firstNonEmpty(citaSnapshot.getString("fechaISO")), firstNonEmpty(citaSnapshot.getString("slotKey")))
                    );
                    DocumentReference slotRef = firestore.collection("citas_slots").document(slotId);
                    DocumentSnapshot slotSnapshot = transaction.get(slotRef);
                    releaseSlot(transaction, slotRef, slotSnapshot, citaId, clienteId);

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("estado", "Cancelada");
                    updates.put("cancelReason", firstNonEmpty(reason));
                    updates.put("cancelledAt", now);
                    updates.put("updatedAt", now);
                    transaction.update(citaRef, updates);

                    DocumentReference eventRef = firestore.collection("eventos_cita")
                            .document("evt_" + citaId + "_cancelled_" + now);
                    transaction.set(eventRef, eventMap("Cita cancelada",
                            firstNonEmpty(reason, "Cancelada desde la agenda del asesor"),
                            "CANCELADA", citaId, clienteId, asesorId,
                            firstNonEmpty(citaSnapshot.getString("fechaISO")), firstNonEmpty(citaSnapshot.getString("slotKey")), now),
                            SetOptions.merge());
                    return null;
                })
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(error -> callback.onError(safeMessage((Exception) error)));
    }

    public void updateAttendance(String citaId, boolean attended, OperationCallback callback) {
        long now = System.currentTimeMillis();
        DocumentReference citaRef = firestore.collection("citas").document(citaId);

        firestore.runTransaction(transaction -> {
                    DocumentSnapshot citaSnapshot = transaction.get(citaRef);
                    if (!citaSnapshot.exists()) {
                        throw abort("La cita ya no existe.");
                    }
                    String asesorId = firstNonEmpty(citaSnapshot.getString("asesorId"));
                    String clienteId = firstNonEmpty(citaSnapshot.getString("clienteId"));
                    String status = attended ? "Atendida" : "No asistio";

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("estado", status);
                    updates.put("attendanceConfirmed", true);
                    updates.put("attended", attended);
                    updates.put("attendanceUpdatedAt", now);
                    updates.put("updatedAt", now);
                    transaction.update(citaRef, updates);

                    DocumentReference eventRef = firestore.collection("eventos_cita")
                            .document("evt_" + citaId + "_attendance_" + now);
                    transaction.set(eventRef, eventMap("Asistencia registrada", status,
                            "ASISTENCIA", citaId, clienteId, asesorId,
                            firstNonEmpty(citaSnapshot.getString("fechaISO")), firstNonEmpty(citaSnapshot.getString("slotKey")), now),
                            SetOptions.merge());
                    return null;
                })
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(error -> callback.onError(safeMessage((Exception) error)));
    }

    public ListenerRegistration listenAdvisorAppointments(String asesorId, AppointmentsCallback callback) {
        return firestore.collection("citas")
                .whereEqualTo("asesorId", asesorId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError("Error al escuchar citas: " + safeMessage(error));
                        return;
                    }
                    List<Cita> list = new ArrayList<>();
                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            list.add(citaFromSnapshot(doc));
                        }
                    }
                    callback.onSuccess(list);
                });
    }

    private void reserveAppointmentWithAvailability(AppointmentDraft draft, Availability availability, AppointmentCallback callback) {
        long now = System.currentTimeMillis();
        String slotKey = slotKey(draft.hora);
        String slotId = slotId(draft.asesorId, draft.fechaISO, slotKey);
        String citaId = "cita_" + slotId + "_" + safeId(draft.clienteId);
        String eventId = "evt_" + citaId + "_" + now;

        DocumentReference slotRef = firestore.collection("citas_slots").document(slotId);
        DocumentReference citaRef = firestore.collection("citas").document(citaId);
        DocumentReference eventRef = firestore.collection("eventos_cita").document(eventId);

        firestore.runTransaction(transaction -> {
                    DocumentSnapshot slot = transaction.get(slotRef);
                    if (slot.exists()) {
                        int capacity = intValue(slot.get("capacidadMaxima"), availability.capacity);
                        int reserved = intValue(slot.get("reservedCount"), 1);
                        List<String> clientIds = stringList(slot.get("clientIds"));
                        if (clientIds.contains(draft.clienteId)) {
                            throw abort("Ya tienes una cita reservada en este horario.");
                        }
                        if (reserved >= capacity) {
                            throw abort("El horario seleccionado ya fue reservado.");
                        }
                    }

                    Map<String, Object> cita = new HashMap<>();
                    cita.put("id", citaId);
                    cita.put("clienteId", draft.clienteId);
                    cita.put("clienteNombre", draft.clienteNombre);
                    cita.put("asesorId", draft.asesorId);
                    cita.put("asesorNombre", draft.asesorNombre);
                    cita.put("inmuebleNombre", draft.inmuebleNombre);
                    cita.put("propertyId", draft.propertyId);
                    cita.put("proyectoNombre", firstNonEmpty(draft.proyectoNombre, draft.inmuebleNombre));
                    cita.put("fechaISO", draft.fechaISO);
                    cita.put("fechaTexto", draft.fechaTexto);
                    cita.put("hora", draft.hora);
                    cita.put("slotKey", slotKey);
                    cita.put("slotId", slotId);
                    cita.put("durationMinutos", availability.durationMinutes);
                    cita.put("capacidadHorario", availability.capacity);
                    cita.put("estado", "Confirmada");
                    cita.put("meetingPoint", draft.meetingPoint);
                    cita.put("nota", draft.nota);
                    cita.put("imageKey", firstNonEmpty(draft.imageKey, "user_featured_house"));
                    cita.put("hasCierre", false);
                    cita.put("createdAt", now);
                    cita.put("updatedAt", now);
                    cita.put("participantUids", Arrays.asList(draft.clienteId, draft.asesorId));

                    reserveSlot(transaction, slotRef, slot, citaId, draft.clienteId, draft.asesorId, draft.propertyId,
                            draft.fechaISO, draft.hora, slotKey, availability.durationMinutes, availability.capacity, now);

                    transaction.set(citaRef, cita, SetOptions.merge());
                    transaction.set(eventRef, eventMap("Cita agendada",
                            "Agendada desde la app por el cliente", "AGENDADA", citaId, draft.clienteId,
                            draft.asesorId, draft.fechaISO, slotKey, now), SetOptions.merge());
                    return citaId;
                })
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(error ->
                        callback.onError(safeMessage((Exception) error)));
    }

    private void reserveSlot(com.google.firebase.firestore.Transaction transaction, DocumentReference slotRef,
                             DocumentSnapshot slot, String citaId, String clienteId, String asesorId, String propertyId,
                             String fechaISO, String hora, String slotKey, int durationMinutes, int capacity, long now) {
        List<String> citaIds = slot.exists() ? stringList(slot.get("citaIds")) : new ArrayList<>();
        List<String> clientIds = slot.exists() ? stringList(slot.get("clientIds")) : new ArrayList<>();
        if (!citaIds.contains(citaId)) {
            citaIds.add(citaId);
        }
        if (!clientIds.contains(clienteId)) {
            clientIds.add(clienteId);
        }
        List<String> participants = new ArrayList<>(clientIds);
        if (!participants.contains(asesorId)) {
            participants.add(asesorId);
        }

        Map<String, Object> slotData = new HashMap<>();
        slotData.put("id", slotRef.getId());
        slotData.put("citaId", citaId);
        slotData.put("citaIds", citaIds);
        slotData.put("clienteId", clienteId);
        slotData.put("clientIds", clientIds);
        slotData.put("asesorId", asesorId);
        slotData.put("propertyId", propertyId);
        slotData.put("fechaISO", fechaISO);
        slotData.put("hora", hora);
        slotData.put("slotKey", slotKey);
        slotData.put("durationMinutos", durationMinutes);
        slotData.put("capacidadMaxima", capacity);
        slotData.put("reservedCount", clientIds.size());
        slotData.put("estado", "ocupado");
        slotData.put("participantUids", participants);
        slotData.put("updatedAt", now);
        if (!slot.exists()) {
            slotData.put("createdAt", now);
        }
        transaction.set(slotRef, slotData, SetOptions.merge());
    }

    private void releaseSlot(com.google.firebase.firestore.Transaction transaction, DocumentReference slotRef,
                             DocumentSnapshot slotSnapshot, String citaId, String clienteId) {
        if (!slotSnapshot.exists()) {
            return;
        }
        List<String> citaIds = stringList(slotSnapshot.get("citaIds"));
        List<String> clientIds = stringList(slotSnapshot.get("clientIds"));
        citaIds.remove(citaId);
        clientIds.remove(clienteId);
        if (clientIds.isEmpty()) {
            transaction.delete(slotRef);
            return;
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("citaIds", citaIds);
        updates.put("clientIds", clientIds);
        updates.put("reservedCount", clientIds.size());
        updates.put("estado", "ocupado");
        transaction.update(slotRef, updates);
    }

    private void loadAvailability(String asesorId, String propertyId, AvailabilityCallback callback) {
        String normalizedPropertyId = firstNonEmpty(propertyId);
        if (!normalizedPropertyId.isEmpty()) {
            firestore.collection("proyectos_disponibilidad").document(normalizedPropertyId)
                    .get()
                    .addOnSuccessListener(projectAvailability -> {
                        if (projectAvailability.exists()) {
                            callback.onSuccess(availabilityFromSnapshot(projectAvailability));
                        } else {
                            loadAdvisorAvailability(asesorId, callback);
                        }
                    })
                    .addOnFailureListener(error -> loadAdvisorAvailability(asesorId, callback));
        } else {
            loadAdvisorAvailability(asesorId, callback);
        }
    }

    private void loadAdvisorAvailability(String asesorId, AvailabilityCallback callback) {
        firestore.collection("asesor_disponibilidad").document(firstNonEmpty(asesorId))
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        callback.onSuccess(availabilityFromSnapshot(document));
                    } else {
                        callback.onSuccess(defaultAvailability());
                    }
                })
                .addOnFailureListener(error -> callback.onSuccess(defaultAvailability()));
    }

    private Availability availabilityFromSnapshot(DocumentSnapshot document) {
        List<String> slots = stringList(firstNonNull(document.get("slotKeys"), document.get("slots")));
        if (slots.isEmpty()) {
            slots = new ArrayList<>(Arrays.asList(SLOT_KEYS));
        }
        Set<Integer> days = intSet(document.get("diasLaborales"));
        if (days.isEmpty()) {
            days = defaultWorkingDays();
        }
        int duration = intValue(document.get("durationMinutos"), DEFAULT_DURATION_MINUTES);
        int capacity = intValue(document.get("capacidadHorario"), DEFAULT_SLOT_CAPACITY);
        return new Availability(slots, days, Math.max(15, duration), Math.max(1, capacity));
    }

    private Availability defaultAvailability() {
        return new Availability(new ArrayList<>(Arrays.asList(SLOT_KEYS)), defaultWorkingDays(),
                DEFAULT_DURATION_MINUTES, DEFAULT_SLOT_CAPACITY);
    }

    private Set<Integer> defaultWorkingDays() {
        return new HashSet<>(Arrays.asList(
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY
        ));
    }

    private void resolveAdvisorFromAssignment(DocumentSnapshot assignment, AdvisorCallback callback) {
        String advisorId = firstNonEmpty(
                assignment.getString("asesorId"),
                assignment.getString("advisorId"),
                assignment.getString("asesorUid"),
                assignment.getString("uidAsesor")
        );
        if (advisorId.isEmpty()) {
            getFirstActiveAdvisor(callback);
            return;
        }
        firestore.collection("usuarios").document(advisorId)
                .get()
                .addOnSuccessListener(user -> {
                    if (user.exists()) {
                        callback.onSuccess(new Advisor(advisorId, displayName(user)));
                    } else {
                        callback.onSuccess(new Advisor(advisorId, firstNonEmpty(assignment.getString("asesorNombre"), "Asesor")));
                    }
                })
                .addOnFailureListener(error ->
                        callback.onSuccess(new Advisor(advisorId, firstNonEmpty(assignment.getString("asesorNombre"), "Asesor"))));
    }

    private Cita citaFromSnapshot(DocumentSnapshot document) {
        Cita cita = new Cita(); // Constructor vacío

        cita.setId(document.getId());
        cita.setClienteNombre(firstNonEmpty(document.getString("clienteNombre"), "Cliente"));
        cita.setProyectoNombre(firstNonEmpty(document.getString("proyectoNombre"), document.getString("inmuebleNombre"), "Inmueble"));
        cita.setHora(firstNonEmpty(document.getString("hora"), "00:00"));
        cita.setFechaISO(firstNonEmpty(document.getString("fechaISO"), document.getString("fechaTexto")));
        cita.setEstado(firstNonEmpty(document.getString("estado"), "Confirmada"));
        cita.setHasCierre(Boolean.TRUE.equals(document.getBoolean("hasCierre")));
        cita.setClienteId(firstNonEmpty(document.getString("clienteId")));
        cita.setAsesorId(firstNonEmpty(document.getString("asesorId")));
        cita.setProyectoId(firstNonEmpty(document.getString("propertyId"), document.getString("projectId")));
        cita.setDuracionMinutos(intValue(document.get("durationMinutos"), 60));
        cita.setCreatedAt(longValue(document.get("createdAt")));

        // 🔥 Cargar historial como lista embebida
        List<EventoCita> historial = new ArrayList<>();
        List<Map<String, Object>> historialData = (List<Map<String, Object>>) document.get("historial");
        if (historialData != null) {
            for (Map<String, Object> item : historialData) {
                EventoCita evento = new EventoCita();
                evento.setId((String) item.get("id"));
                evento.setCitaId((String) item.get("citaId"));
                evento.setTitulo((String) item.get("titulo"));
                evento.setDetalle((String) item.get("detalle"));
                evento.setFechaHora((String) item.get("fechaHora"));
                evento.setTipo((String) item.get("tipo"));
                historial.add(evento);
            }
        }
        cita.setHistorial(historial);

        return cita;
    }

    private Map<String, Object> eventMap(String title, String detail, String type, String citaId,
                                         String clienteId, String asesorId, String fechaISO,
                                         String slotKey, long now) {
        Map<String, Object> event = new HashMap<>();
        event.put("titulo", title);
        event.put("detalle", detail);
        event.put("tipo", type);
        event.put("citaId", citaId);
        event.put("clienteId", clienteId);
        event.put("asesorId", asesorId);
        event.put("fechaHora", firstNonEmpty(fechaISO) + "T" + firstNonEmpty(slotKey).replace("_", ":"));
        event.put("createdAt", now);
        return event;
    }

    private String displayName(DocumentSnapshot document) {
        return firstNonEmpty(
                document.getString("nombre"),
                (firstNonEmpty(document.getString("nombres")) + " " + firstNonEmpty(document.getString("apellidos"))).trim(),
                "Asesor"
        );
    }

    public static String slotKey(String hora) {
        if (hora == null) {
            return "";
        }
        String normalized = hora.trim().toUpperCase(Locale.ROOT);
        if (normalized.endsWith("AM") || normalized.endsWith("PM")) {
            try {
                SimpleDateFormat input = new SimpleDateFormat("hh:mm a", Locale.US);
                SimpleDateFormat output = new SimpleDateFormat("HH_mm", Locale.US);
                return output.format(input.parse(normalized));
            } catch (Exception ignored) {}
        }
        return normalized.replace(":", "_").replaceAll("[^0-9_]", "");
    }

    public static String displayTime(String slotKey) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("HH:mm", Locale.US);
            SimpleDateFormat output = new SimpleDateFormat("hh:mm a", Locale.US);
            return output.format(input.parse(slotKey.replace("_", ":")));
        } catch (Exception ignored) {
            return slotKey;
        }
    }

    private String slotId(String asesorId, String fechaISO, String slotKey) {
        return safeId(asesorId) + "_" + safeId(fechaISO) + "_" + safeId(slotKey);
    }

    private String safeId(@Nullable String value) {
        return value == null ? "" : value.trim().replaceAll("[^A-Za-z0-9_\\-]", "_");
    }

    private String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private Object firstNonNull(Object first, Object second) {
        return first != null ? first : second;
    }

    private int intValue(Object value, int fallback) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (Exception ignored) {}
        }
        return fallback;
    }

    private long longValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (Exception ignored) {}
        }
        return 0L;
    }

    private List<String> stringList(Object value) {
        List<String> result = new ArrayList<>();
        if (value instanceof List<?>) {
            for (Object item : (List<?>) value) {
                if (item != null && !item.toString().trim().isEmpty()) {
                    result.add(item.toString().trim());
                }
            }
        }
        return result;
    }

    private Set<Integer> intSet(Object value) {
        Set<Integer> result = new HashSet<>();
        if (value instanceof List<?>) {
            for (Object item : (List<?>) value) {
                result.add(intValue(item, -1));
            }
        }
        result.remove(-1);
        return result;
    }

    private FirebaseFirestoreException abort(String message) {
        return new FirebaseFirestoreException(message, FirebaseFirestoreException.Code.ABORTED);
    }

    private String safeMessage(Exception error) {
        String message = error.getMessage();
        return message == null || message.trim().isEmpty()
                ? error.getClass().getSimpleName()
                : message;
    }
}
