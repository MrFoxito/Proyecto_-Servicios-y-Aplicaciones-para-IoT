package com.example.proyecto_iot.data;

import com.example.proyecto_iot.admin.model.AdminAdvisorItem;
import com.example.proyecto_iot.entity.Proyecto;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class ProjectAssignmentRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public interface AdvisorsCallback {
        void onSuccess(List<AdminAdvisorItem> advisors);
        void onError(String message);
    }

    public interface ProjectsCallback {
        void onSuccess(List<Proyecto> projects);
        void onError(String message);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface ProjectIdsCallback {
        void onSuccess(Set<String> projectIds);
        void onError(String message);
    }

    public void readAdvisors(String empresaId, AdvisorsCallback callback) {
        String companyId = safe(empresaId);
        if (companyId.isEmpty()) {
            callback.onError("El administrador no está vinculado a una empresa.");
            return;
        }
        firestore.collection("usuarios")
                .whereEqualTo("rol", "asesor")
                .whereEqualTo("empresaId", companyId)
                .get()
                .addOnSuccessListener(primary ->
                        firestore.collection("usuarios")
                                .whereEqualTo("rol", "asesor")
                                .whereEqualTo("inmobiliariaId", companyId)
                                .get()
                                .addOnSuccessListener(legacy ->
                                        callback.onSuccess(advisorItems(primary.getDocuments(),
                                                legacy.getDocuments(), companyId)))
                                .addOnFailureListener(error ->
                                        callback.onSuccess(advisorItems(primary.getDocuments(),
                                                new ArrayList<>(), companyId))))
                .addOnFailureListener(error -> callback.onError(message(error)));
    }

    private List<AdminAdvisorItem> advisorItems(
            List<DocumentSnapshot> primary,
            List<DocumentSnapshot> legacy,
            String companyId
    ) {
        Map<String, DocumentSnapshot> unique = new HashMap<>();
        for (DocumentSnapshot document : primary) unique.put(document.getId(), document);
        for (DocumentSnapshot document : legacy) unique.put(document.getId(), document);
        List<AdminAdvisorItem> advisors = new ArrayList<>();
        for (DocumentSnapshot document : unique.values()) {
            advisors.add(new AdminAdvisorItem(
                    document.getId(),
                    displayName(document),
                    first(document, "rating", "5.0"),
                    first(document, "email", "correo"),
                    !"inactivo".equalsIgnoreCase(first(document, "estado")),
                    com.example.proyecto_iot.R.drawable.sa_profile_asesor_1,
                    stringList(document.get("proyectosAsignados")),
                    companyId
            ));
        }
        return advisors;
    }

    public void assignProject(
            String projectId,
            String projectName,
            String advisorId,
            String advisorName,
            String adminId,
            String empresaId,
            SimpleCallback callback
    ) {
        if (safe(projectId).isEmpty() || safe(advisorId).isEmpty()) {
            callback.onError("Selecciona un proyecto y un asesor válidos.");
            return;
        }
        String id = projectId + "_" + advisorId;
        firestore.runTransaction(transaction -> {
                    DocumentSnapshot current = transaction.get(
                            firestore.collection("asignaciones").document(id)
                    );
                    long now = System.currentTimeMillis();
                    Map<String, Object> assignment = new HashMap<>();
                    assignment.put("id", id);
                    assignment.put("projectId", projectId);
                    assignment.put("propertyId", projectId);
                    assignment.put("proyectoId", projectId);
                    assignment.put("projectName", safe(projectName));
                    assignment.put("proyectoNombre", safe(projectName));
                    assignment.put("asesorId", advisorId);
                    assignment.put("asesorNombre", safe(advisorName));
                    assignment.put("adminId", safe(adminId));
                    assignment.put("empresaId", safe(empresaId));
                    assignment.put("estado", "ACTIVO");
                    assignment.put("createdAt", current.exists() && current.getLong("createdAt") != null
                            ? current.getLong("createdAt") : now);
                    assignment.put("updatedAt", now);
                    assignment.put("deactivatedAt", FieldValue.delete());
                    transaction.set(
                            firestore.collection("asignaciones").document(id),
                            assignment,
                            SetOptions.merge()
                    );
                    return null;
                })
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(error -> callback.onError(message(error)));
    }

    public void unassignProject(String projectId, String advisorId, SimpleCallback callback) {
        if (safe(projectId).isEmpty() || safe(advisorId).isEmpty()) {
            callback.onError("No se recibió una asignación válida.");
            return;
        }
        Map<String, Object> values = new HashMap<>();
        values.put("estado", "INACTIVO");
        values.put("deactivatedAt", System.currentTimeMillis());
        values.put("updatedAt", System.currentTimeMillis());
        firestore.collection("asignaciones").document(projectId + "_" + advisorId)
                .set(values, SetOptions.merge())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(error -> callback.onError(message(error)));
    }

    public void readActiveProjectIdsForAdvisor(String advisorId, ProjectIdsCallback callback) {
        firestore.collection("asignaciones")
                .whereEqualTo("asesorId", advisorId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Set<String> ids = new HashSet<>();
                    for (DocumentSnapshot assignment : snapshot.getDocuments()) {
                        if (!"ACTIVO".equalsIgnoreCase(first(assignment, "estado"))) continue;
                        String projectId = first(assignment, "projectId", "propertyId", "proyectoId");
                        if (!projectId.isEmpty()) ids.add(projectId);
                    }
                    callback.onSuccess(ids);
                })
                .addOnFailureListener(error -> callback.onError(message(error)));
    }

    public void readProjectsForAdvisor(String advisorId, ProjectsCallback callback) {
        firestore.collection("asignaciones")
                .whereEqualTo("asesorId", advisorId)
                .get()
                .addOnSuccessListener(assignments -> {
                    List<String> ids = new ArrayList<>();
                    for (DocumentSnapshot assignment : assignments.getDocuments()) {
                        if (!"ACTIVO".equalsIgnoreCase(first(assignment, "estado"))) continue;
                        String id = first(assignment, "projectId", "propertyId", "proyectoId");
                        if (!id.isEmpty() && !ids.contains(id)) ids.add(id);
                    }
                    if (ids.isEmpty()) {
                        readLegacyAdvisorProjects(advisorId, callback);
                    } else {
                        loadProjects(ids, callback);
                    }
                })
                .addOnFailureListener(error -> readLegacyAdvisorProjects(advisorId, callback));
    }

    private void readLegacyAdvisorProjects(String advisorId, ProjectsCallback callback) {
        firestore.collection("usuarios").document(advisorId).get()
                .addOnSuccessListener(user -> {
                    List<String> ids = stringList(user.get("proyectos_asignados"));
                    if (ids.isEmpty()) ids = stringList(user.get("proyectosAsignados"));
                    if (ids.isEmpty()) callback.onSuccess(new ArrayList<>());
                    else loadProjects(ids, callback);
                })
                .addOnFailureListener(error -> callback.onError(message(error)));
    }

    private void loadProjects(List<String> ids, ProjectsCallback callback) {
        List<Proyecto> projects = new ArrayList<>();
        int[] pending = {ids.size()};
        for (String id : ids) {
            firestore.collection("proyectos").document(id).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            projects.add(project(task.getResult()));
                        }
                        pending[0]--;
                        if (pending[0] == 0) callback.onSuccess(projects);
                    });
        }
    }

    private Proyecto project(DocumentSnapshot doc) {
        Proyecto project = new Proyecto();
        project.setId(doc.getId());
        project.setNombre(first(doc, "nombre", "title"));
        project.setDireccion(first(doc, "direccion"));
        project.setDistrito(first(doc, "distrito"));
        project.setEstado(first(doc, "estadoProyectoLabel", "estadoComercial", "estado"));
        project.setPrecioDesde(first(doc, "precioDesde"));
        project.setImageUrl(first(doc, "primaryImageUrl", "imageUrl"));
        return project;
    }

    private String displayName(DocumentSnapshot document) {
        String name = first(document, "nombre");
        if (!name.isEmpty()) return name;
        return (first(document, "nombres") + " " + first(document, "apellidos")).trim();
    }

    @SuppressWarnings("unchecked")
    private List<String> stringList(Object value) {
        List<String> result = new ArrayList<>();
        if (value instanceof List<?>) {
            for (Object item : (List<Object>) value) {
                if (item != null && !String.valueOf(item).trim().isEmpty()) result.add(String.valueOf(item).trim());
            }
        }
        return result;
    }

    private String first(DocumentSnapshot document, String... keys) {
        for (String key : keys) {
            Object value = document.get(key);
            if (value != null && !String.valueOf(value).trim().isEmpty()) return String.valueOf(value).trim();
        }
        return "";
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String message(Exception error) {
        return error.getMessage() == null ? "Error al procesar asignaciones." : error.getMessage();
    }
}
