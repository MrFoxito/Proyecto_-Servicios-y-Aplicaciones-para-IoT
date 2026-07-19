package com.example.proyecto_iot.data;

import android.util.Log;

import com.example.proyecto_iot.entity.Cita;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Single real-time source for an advisor's agenda. Appointment listeners are scoped to
 * currently active project assignments, so an unassignment immediately removes the
 * affected appointment from both agenda and history consumers.
 */
public class AdvisorAgendaRepository {
    private static final String TAG = "AdvisorAgendaRepository";
    private static final int FIRESTORE_IN_LIMIT = 30;

    public interface AgendaCallback {
        void onSuccess(List<Cita> citas);
        void onError(String message);
    }

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final FirebaseAppointmentRepository appointments = new FirebaseAppointmentRepository();
    private final ProjectAssignmentRepository assignments = new ProjectAssignmentRepository();

    public ListenerRegistration listenAgenda(String asesorId, AgendaCallback callback) {
        AgendaRegistration registration = new AgendaRegistration(asesorId, callback);
        registration.start();
        return registration;
    }

    private final class AgendaRegistration implements ListenerRegistration {
        private final String asesorId;
        private final AgendaCallback callback;
        private final List<ListenerRegistration> appointmentListeners = new ArrayList<>();
        private final Map<String, Cita> citasById = new HashMap<>();
        private final Map<String, Map<String, Cita>> citasByListener = new HashMap<>();
        private final Map<String, String> clientNames = new HashMap<>();
        private final Map<String, String> projectNames = new HashMap<>();
        private final Set<String> pendingClients = new HashSet<>();
        private final Set<String> pendingProjects = new HashSet<>();
        private Set<String> activeProjectReferences = Collections.emptySet();
        private ListenerRegistration assignmentListener;
        private int listenerGeneration;
        private boolean removed;

        AgendaRegistration(String asesorId, AgendaCallback callback) {
            this.asesorId = safe(asesorId);
            this.callback = callback;
        }

        void start() {
            if (asesorId.isEmpty()) {
                callback.onError("ID de asesor invalido.");
                return;
            }
            assignmentListener = assignments.listenActiveProjectIdsForAdvisor(asesorId,
                    new ProjectAssignmentRepository.ProjectIdsCallback() {
                @Override
                public void onSuccess(Set<String> projectIds) {
                    if (removed) return;
                    activeProjectReferences = new HashSet<>(projectIds);
                    restartAppointmentListeners();
                }

                @Override
                public void onError(String message) {
                    if (!removed) callback.onError(message);
                }
            });
        }

        private void restartAppointmentListeners() {
            listenerGeneration++;
            for (ListenerRegistration listener : appointmentListeners) listener.remove();
            appointmentListeners.clear();
            citasById.clear();
            citasByListener.clear();

            if (activeProjectReferences.isEmpty()) {
                publish();
                return;
            }
            for (List<String> projectChunk : chunks(activeProjectReferences)) {
                listen("asesorId", "propertyId", projectChunk, listenerGeneration);
                listen("asesorId", "projectId", projectChunk, listenerGeneration);
                listen("asesorId", "proyectoId", projectChunk, listenerGeneration);
                listen("advisorId", "propertyId", projectChunk, listenerGeneration);
                listen("advisorId", "projectId", projectChunk, listenerGeneration);
                listen("advisorId", "proyectoId", projectChunk, listenerGeneration);
            }
        }

        private void listen(String advisorField, String projectField, List<String> ids, int generation) {
            String listenerKey = advisorField + "|" + projectField + "|" + String.join(",", ids);
            ListenerRegistration listener = firestore.collection("citas")
                    .whereEqualTo(advisorField, asesorId)
                    .whereIn(projectField, ids)
                    .addSnapshotListener((snapshot, error) -> {
                        if (removed || generation != listenerGeneration) return;
                        if (error != null) {
                            callback.onError("No se pudieron cargar las citas: " + error.getMessage());
                            return;
                        }
                        Map<String, Cita> listenerCitas = new HashMap<>();
                        if (snapshot != null) {
                            for (DocumentSnapshot document : snapshot.getDocuments()) {
                                Cita cita = appointments.citaFromSnapshot(document);
                                listenerCitas.put(cita.getId(), cita);
                            }
                        }
                        citasByListener.put(listenerKey, listenerCitas);
                        rebuildAppointments();
                        publish();
                    });
            appointmentListeners.add(listener);
        }

        private void rebuildAppointments() {
            citasById.clear();
            for (Map<String, Cita> listenerCitas : citasByListener.values()) {
                citasById.putAll(listenerCitas);
            }
        }

        private void publish() {
            if (removed) return;
            List<Cita> agenda = new ArrayList<>();
            for (Cita cita : citasById.values()) {
                if (!activeProjectReferences.contains(safe(cita.getProyectoId()))) continue;
                if (cita.getFechaISO().isEmpty() || cita.getHora().isEmpty()) {
                    Log.w(TAG, "Cita omitida por fecha/hora invalida: " + cita.getId());
                    continue;
                }
                hydrate(cita);
                if (safe(cita.getClienteNombre()).isEmpty()) cita.setClienteNombre("Cliente");
                if (safe(cita.getProyectoNombre()).isEmpty()) cita.setProyectoNombre("Proyecto");
                agenda.add(cita);
            }
            Collections.sort(agenda, (left, right) -> {
                int date = left.getFechaISO().compareTo(right.getFechaISO());
                return date != 0 ? date : left.getHora().compareTo(right.getHora());
            });
            callback.onSuccess(agenda);
        }

        private void hydrate(Cita cita) {
            String clientId = safe(cita.getClienteId());
            if (isPlaceholder(cita.getClienteNombre(), "Cliente") && !clientId.isEmpty()) {
                String cached = clientNames.get(clientId);
                if (cached != null) {
                    cita.setClienteNombre(cached);
                } else if (pendingClients.add(clientId)) {
                    firestore.collection("usuarios").document(clientId).get().addOnSuccessListener(user -> {
                        pendingClients.remove(clientId);
                        clientNames.put(clientId, displayName(user, "Cliente"));
                        publish();
                    }).addOnFailureListener(error -> pendingClients.remove(clientId));
                }
            }

            String projectId = safe(cita.getProyectoId());
            if (isPlaceholder(cita.getProyectoNombre(), "Proyecto") && !projectId.isEmpty()) {
                String cached = projectNames.get(projectId);
                if (cached != null) {
                    cita.setProyectoNombre(cached);
                } else if (pendingProjects.add(projectId)) {
                    firestore.collection("proyectos").document(projectId).get().addOnSuccessListener(project -> {
                        pendingProjects.remove(projectId);
                        projectNames.put(projectId, first(project, "nombre", "title", "projectName", "Proyecto"));
                        publish();
                    }).addOnFailureListener(error -> pendingProjects.remove(projectId));
                }
            }
        }

        @Override
        public void remove() {
            removed = true;
            if (assignmentListener != null) assignmentListener.remove();
            for (ListenerRegistration listener : appointmentListeners) listener.remove();
            appointmentListeners.clear();
        }
    }

    private static List<List<String>> chunks(Set<String> values) {
        List<String> ids = new ArrayList<>(values);
        List<List<String>> result = new ArrayList<>();
        for (int start = 0; start < ids.size(); start += FIRESTORE_IN_LIMIT) {
            result.add(ids.subList(start, Math.min(start + FIRESTORE_IN_LIMIT, ids.size())));
        }
        return result;
    }

    private static String displayName(DocumentSnapshot document, String fallback) {
        if (!document.exists()) return fallback;
        String fullName = first(document, "nombre", "name", "displayName");
        if (!fullName.isEmpty()) return fullName;
        String composed = (first(document, "nombres") + " " + first(document, "apellidos")).trim();
        return composed.isEmpty() ? fallback : composed;
    }

    private static String first(DocumentSnapshot document, String... fields) {
        for (String field : fields) {
            String value = document.getString(field);
            if (!safe(value).isEmpty()) return value.trim();
        }
        return "";
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isPlaceholder(String value, String placeholder) {
        return safe(value).isEmpty() || placeholder.equalsIgnoreCase(safe(value));
    }
}
