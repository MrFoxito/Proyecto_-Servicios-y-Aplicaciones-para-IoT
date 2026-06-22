package com.example.proyecto_iot.data;

import com.example.proyecto_iot.BuildConfig;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DataMigrationRepository {
    public static final String VERSION = "admin_projects_assignments_v3";
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final OkHttpClient client = new OkHttpClient();

    public interface MigrationCallback {
        void onSuccess(MigrationResult result);
        void onError(String message);
    }

    public static class MigrationResult {
        public int usersUpdated;
        public int companiesUpdated;
        public int projectsUpdated;
        public int assignmentsCreated;
        public int assignmentsUpdated;
        public int mediaMigrated;
        public int mediaPending;
    }

    public void run(MigrationCallback callback) {
        firestore.collection("app_meta").document(VERSION).get()
                .addOnSuccessListener(meta -> {
                    if ("complete".equals(meta.getString("status"))) {
                        callback.onSuccess(new MigrationResult());
                    } else {
                        loadAndRepair(callback);
                    }
                })
                .addOnFailureListener(error -> loadAndRepair(callback));
    }

    private void loadAndRepair(MigrationCallback callback) {
        Task<com.google.firebase.firestore.QuerySnapshot> usersTask = firestore.collection("usuarios").get();
        Task<com.google.firebase.firestore.QuerySnapshot> companiesTask = firestore.collection("empresas").get();
        Task<com.google.firebase.firestore.QuerySnapshot> projectsTask = firestore.collection("proyectos").get();
        Task<com.google.firebase.firestore.QuerySnapshot> imagesTask = firestore.collection("proyectos_imagenes").get();
        Task<com.google.firebase.firestore.QuerySnapshot> invitationsTask = firestore.collection("admin_invitations").get();
        Task<com.google.firebase.firestore.QuerySnapshot> assignmentsTask = firestore.collection("asignaciones").get();
        Tasks.whenAllSuccess(usersTask, companiesTask, projectsTask, imagesTask, invitationsTask, assignmentsTask)
                .addOnSuccessListener(results -> {
                    @SuppressWarnings("unchecked")
                    List<DocumentSnapshot> users = ((com.google.firebase.firestore.QuerySnapshot) results.get(0)).getDocuments();
                    List<DocumentSnapshot> companies = ((com.google.firebase.firestore.QuerySnapshot) results.get(1)).getDocuments();
                    List<DocumentSnapshot> projects = ((com.google.firebase.firestore.QuerySnapshot) results.get(2)).getDocuments();
                    List<DocumentSnapshot> images = ((com.google.firebase.firestore.QuerySnapshot) results.get(3)).getDocuments();
                    List<DocumentSnapshot> invitations = ((com.google.firebase.firestore.QuerySnapshot) results.get(4)).getDocuments();
                    List<DocumentSnapshot> assignments = ((com.google.firebase.firestore.QuerySnapshot) results.get(5)).getDocuments();
                    repairMetadata(users, companies, projects, images, invitations, assignments, callback);
                })
                .addOnFailureListener(error -> callback.onError(message(error)));
    }

    private void repairMetadata(
            List<DocumentSnapshot> users,
            List<DocumentSnapshot> companies,
            List<DocumentSnapshot> projects,
            List<DocumentSnapshot> images,
            List<DocumentSnapshot> invitations,
            List<DocumentSnapshot> assignments,
            MigrationCallback callback
    ) {
        MigrationResult result = new MigrationResult();
        List<Task<Void>> writes = new ArrayList<>();
        Map<String, String> companyByEmail = new HashMap<>();
        Map<String, String> projectByTitle = new HashMap<>();
        Map<String, DocumentSnapshot> invitationByEmail = new HashMap<>();
        Map<String, DocumentSnapshot> projectById = new HashMap<>();
        Map<String, String> adminByCompany = new HashMap<>();
        Map<String, String> companyByAdmin = new HashMap<>();

        for (DocumentSnapshot invitation : invitations) {
            String email = first(invitation, "email").toLowerCase(Locale.ROOT);
            if (!email.isEmpty()) invitationByEmail.put(email, invitation);
        }

        for (DocumentSnapshot company : companies) {
            String email = first(company, "adminEmail").toLowerCase(Locale.ROOT);
            if (!email.isEmpty()) companyByEmail.put(email, company.getId());
            Map<String, Object> values = new HashMap<>();
            values.put("id", company.getId());
            values.put("empresaId", company.getId());
            values.put("updatedAt", System.currentTimeMillis());
            writes.add(company.getReference().set(values, SetOptions.merge()));
            result.companiesUpdated++;
        }
        for (DocumentSnapshot user : users) {
            String email = first(user, "email", "correo").toLowerCase(Locale.ROOT);
            String companyId = first(user, "empresaId", "inmobiliariaId");
            if (companyId.isEmpty()) companyId = companyByEmail.getOrDefault(email, "");
            boolean admin = "admin".equalsIgnoreCase(first(user, "rol"))
                    || companyByEmail.containsKey(email)
                    || invitationByEmail.containsKey(email);
            if (admin && !companyId.isEmpty()) {
                adminByCompany.put(companyId, user.getId());
                companyByAdmin.put(user.getId(), companyId);
            }
        }
        for (DocumentSnapshot project : projects) {
            String projectId = first(project, "projectId", "propertyId");
            if (projectId.isEmpty()) projectId = project.getId();
            String title = first(project, "nombre", "title");
            projectById.put(projectId, project);
            if (!title.isEmpty()) projectByTitle.put(title.toLowerCase(Locale.ROOT), projectId);
            String image = first(project, "primaryImageUrl", "imageUrl");
            String adminId = first(project, "adminId");
            String companyId = first(project, "empresaId", "inmobiliariaId");
            if (companyId.isEmpty() && !adminId.isEmpty()) {
                companyId = companyByAdmin.getOrDefault(adminId, "");
            }
            if (adminId.isEmpty() && !companyId.isEmpty()) {
                adminId = adminByCompany.getOrDefault(companyId, "");
            }
            if (adminId.isEmpty() && companyId.isEmpty() && companies.size() == 1) {
                companyId = companies.get(0).getId();
                adminId = adminByCompany.getOrDefault(companyId, "");
            }
            Map<String, Object> values = new HashMap<>();
            values.put("id", projectId);
            values.put("projectId", projectId);
            values.put("propertyId", projectId);
            if (!adminId.isEmpty()) values.put("adminId", adminId);
            if (!companyId.isEmpty()) {
                values.put("empresaId", companyId);
                values.put("inmobiliariaId", companyId);
            }
            if (!image.isEmpty()) {
                values.put("imageUrl", image);
                values.put("primaryImageUrl", image);
            }
            values.put("updatedAt", System.currentTimeMillis());
            writes.add(project.getReference().set(values, SetOptions.merge()));
            result.projectsUpdated++;
        }
        for (DocumentSnapshot user : users) {
            String email = first(user, "email", "correo");
            DocumentSnapshot invitation = invitationByEmail.get(email.toLowerCase(Locale.ROOT));
            String companyId = first(user, "empresaId", "inmobiliariaId");
            if (companyId.isEmpty() && invitation != null) companyId = first(invitation, "empresaId");
            if (companyId.isEmpty()) companyId = companyByEmail.getOrDefault(email.toLowerCase(Locale.ROOT), "");
            String name = first(user, "nombre");
            String names = first(user, "nombres");
            String surnames = first(user, "apellidos");
            if (name.isEmpty()) name = (names + " " + surnames).trim();
            if ((names.isEmpty() || surnames.isEmpty()) && !name.isEmpty()) {
                String[] parts = name.split("\\s+", 2);
                if (names.isEmpty()) names = parts[0];
                if (surnames.isEmpty()) surnames = parts.length > 1 ? parts[1] : "";
            }
            boolean isInvitedAdmin = invitation != null
                    || companyByEmail.containsKey(email.toLowerCase(Locale.ROOT))
                    || "admin".equalsIgnoreCase(first(user, "rol"));
            Map<String, Object> values = new HashMap<>();
            values.put("uid", user.getId());
            values.put("id", user.getId());
            values.put("nombre", name);
            values.put("email", email);
            values.put("correo", email);
            values.put("nombres", names);
            values.put("apellidos", surnames);
            if (isInvitedAdmin) {
                values.put("rol", "admin");
                values.put("estado", "activo");
                values.put("profileNeedsCompletion", names.isEmpty()
                        || surnames.isEmpty()
                        || first(user, "telefono").isEmpty()
                        || companyId.isEmpty());
                if (invitation != null) values.put("invitationId", invitation.getId());
            }
            if (!companyId.isEmpty()) {
                values.put("empresaId", companyId);
                values.put("inmobiliariaId", companyId);
            }
            values.put("updatedAt", System.currentTimeMillis());
            writes.add(user.getReference().set(values, SetOptions.merge()));
            result.usersUpdated++;
            if (isInvitedAdmin && !companyId.isEmpty()) {
                Map<String, Object> companyAdmin = new HashMap<>();
                companyAdmin.put("adminUid", user.getId());
                companyAdmin.put("adminEmail", email);
                companyAdmin.put("estado", "activo");
                companyAdmin.put("updatedAt", System.currentTimeMillis());
                writes.add(firestore.collection("empresas").document(companyId)
                        .set(companyAdmin, SetOptions.merge()));
            }

            if ("asesor".equalsIgnoreCase(first(user, "rol"))) {
                List<String> legacyProjects = stringList(user.get("proyectos_asignados"));
                if (legacyProjects.isEmpty()) legacyProjects = stringList(user.get("proyectosAsignados"));
                for (String legacy : legacyProjects) {
                    String projectId = projectByTitle.getOrDefault(legacy.toLowerCase(Locale.ROOT), legacy);
                    String assignmentId = projectId + "_" + user.getId();
                    Map<String, Object> assignment = new HashMap<>();
                    assignment.put("id", assignmentId);
                    assignment.put("projectId", projectId);
                    assignment.put("propertyId", projectId);
                    assignment.put("proyectoId", projectId);
                    assignment.put("asesorId", user.getId());
                    assignment.put("asesorNombre", name);
                    assignment.put("empresaId", companyId);
                    assignment.put("estado", "ACTIVO");
                    assignment.put("updatedAt", System.currentTimeMillis());
                    writes.add(firestore.collection("asignaciones").document(assignmentId).set(assignment, SetOptions.merge()));
                    result.assignmentsCreated++;
                }
            }
        }

        for (DocumentSnapshot existing : assignments) {
            String projectId = first(existing, "projectId", "propertyId", "proyectoId");
            String advisorId = first(existing, "asesorId", "advisorId", "asesorUid", "uidAsesor");
            if (projectId.isEmpty() || advisorId.isEmpty()) continue;
            DocumentSnapshot project = projectById.get(projectId);
            Map<String, Object> values = new HashMap<>();
            values.put("id", projectId + "_" + advisorId);
            values.put("projectId", projectId);
            values.put("propertyId", projectId);
            values.put("proyectoId", projectId);
            values.put("asesorId", advisorId);
            values.put("estado", first(existing, "estado").isEmpty()
                    ? "ACTIVO" : first(existing, "estado").toUpperCase(Locale.ROOT));
            if (project != null) {
                values.put("adminId", first(project, "adminId"));
                values.put("empresaId", first(project, "empresaId", "inmobiliariaId"));
                values.put("projectName", first(project, "nombre", "title"));
            }
            values.put("updatedAt", System.currentTimeMillis());
            writes.add(firestore.collection("asignaciones")
                    .document(projectId + "_" + advisorId)
                    .set(values, SetOptions.merge()));
            result.assignmentsUpdated++;
        }

        Tasks.whenAll(writes).addOnSuccessListener(unused ->
                migrateMedia(projects, images, result, callback)
        ).addOnFailureListener(error -> callback.onError(message(error)));
    }

    private void migrateMedia(
            List<DocumentSnapshot> projects,
            List<DocumentSnapshot> images,
            MigrationResult result,
            MigrationCallback callback
    ) {
        List<MediaRecord> records = new ArrayList<>();
        for (DocumentSnapshot image : images) {
            String source = first(image, "imageUrl");
            String projectId = first(image, "projectId");
            if (needsMigration(source) && !projectId.isEmpty()) {
                records.add(new MediaRecord(projectId, source, image));
            }
        }
        for (DocumentSnapshot project : projects) {
            String source = first(project, "primaryImageUrl", "imageUrl");
            if (needsMigration(source)) records.add(new MediaRecord(project.getId(), source, project));
        }
        migrateNext(records, 0, result, callback);
    }

    private void migrateNext(
            List<MediaRecord> records,
            int index,
            MigrationResult result,
            MigrationCallback callback
    ) {
        if (index >= records.size()) {
            finish(result, callback);
            return;
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            result.mediaPending += records.size() - index;
            finish(result, callback);
            return;
        }
        user.getIdToken(false).addOnSuccessListener(tokenResult -> {
            try {
                MediaRecord record = records.get(index);
                JSONObject json = new JSONObject();
                json.put("projectId", record.projectId);
                json.put("source", record.source);
                Request request = new Request.Builder()
                        .url(BuildConfig.SUPABASE_URL + "/functions/v1/migrate-media")
                        .post(RequestBody.create(json.toString(), MediaType.parse("application/json")))
                        .addHeader("apikey", BuildConfig.SUPABASE_PUBLISHABLE_KEY)
                        .addHeader("Authorization", "Bearer " + tokenResult.getToken())
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException error) {
                        result.mediaPending++;
                        migrateNext(records, index + 1, result, callback);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String body = response.body() == null ? "" : response.body().string();
                        if (!response.isSuccessful()) {
                            result.mediaPending++;
                            migrateNext(records, index + 1, result, callback);
                            return;
                        }
                        try {
                            JSONObject migrated = new JSONObject(body);
                            Map<String, Object> values = new HashMap<>();
                            values.put("imageUrl", migrated.optString("publicUrl"));
                            values.put("primaryImageUrl", migrated.optString("publicUrl"));
                            values.put("storagePath", migrated.optString("storagePath"));
                            values.put("provider", "supabase");
                            values.put("updatedAt", System.currentTimeMillis());
                            record.document.getReference().set(values, SetOptions.merge())
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) result.mediaMigrated++;
                                        else result.mediaPending++;
                                        migrateNext(records, index + 1, result, callback);
                                    });
                        } catch (Exception error) {
                            result.mediaPending++;
                            migrateNext(records, index + 1, result, callback);
                        }
                    }
                });
            } catch (Exception error) {
                result.mediaPending++;
                migrateNext(records, index + 1, result, callback);
            }
        }).addOnFailureListener(error -> {
            result.mediaPending++;
            migrateNext(records, index + 1, result, callback);
        });
    }

    private void finish(MigrationResult result, MigrationCallback callback) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("version", VERSION);
        meta.put("status", result.mediaPending == 0 ? "complete" : "partial");
        meta.put("usersUpdated", result.usersUpdated);
        meta.put("companiesUpdated", result.companiesUpdated);
        meta.put("projectsUpdated", result.projectsUpdated);
        meta.put("assignmentsCreated", result.assignmentsCreated);
        meta.put("assignmentsUpdated", result.assignmentsUpdated);
        meta.put("mediaMigrated", result.mediaMigrated);
        meta.put("mediaPending", result.mediaPending);
        meta.put("updatedAt", System.currentTimeMillis());
        firestore.collection("app_meta").document(VERSION).set(meta, SetOptions.merge())
                .addOnSuccessListener(unused -> callback.onSuccess(result))
                .addOnFailureListener(error -> callback.onError(message(error)));
    }

    private boolean needsMigration(String source) {
        return !source.isEmpty()
                && !source.contains("bcsjisoqnbvyznaiynmd.supabase.co/storage/v1/object/public/app-images/");
    }

    private String first(DocumentSnapshot document, String... keys) {
        for (String key : keys) {
            Object value = document.get(key);
            if (value != null && !String.valueOf(value).trim().isEmpty()) return String.valueOf(value).trim();
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private List<String> stringList(Object value) {
        List<String> list = new ArrayList<>();
        if (value instanceof List<?>) {
            for (Object item : (List<Object>) value) {
                if (item != null && !String.valueOf(item).trim().isEmpty()) list.add(String.valueOf(item).trim());
            }
        }
        return list;
    }

    private String message(Exception error) {
        return error.getMessage() == null ? "No se pudo completar la migración." : error.getMessage();
    }

    private static class MediaRecord {
        final String projectId;
        final String source;
        final DocumentSnapshot document;

        MediaRecord(String projectId, String source, DocumentSnapshot document) {
            this.projectId = projectId;
            this.source = source;
            this.document = document;
        }
    }
}
