package com.example.proyecto_iot.data;

import android.content.Context;

import androidx.annotation.Nullable;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.model.AdminProjectDraft;
import com.example.proyecto_iot.admin.model.AdminProjectFormAmenityItem;
import com.example.proyecto_iot.admin.model.AdminProjectFormTypologyItem;
import com.example.proyecto_iot.admin.model.AdminProjectItem;
import com.example.proyecto_iot.admin.model.AdminRequestItem;
import com.example.proyecto_iot.admin.model.AdminReviewItem;
import com.example.proyecto_iot.usuario.UsuarioPropertyListItem;
import com.example.proyecto_iot.usuario.ExploreProjectPresentationPolicy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FirebaseDataRepository {
    private static final String COLLECTION_META = "app_meta";
    private static final String META_SCHEMA_DOC = "schema_seed";
    private static final String SCHEMA_VERSION = "firebase_schema_v1";
    private static final String ROLE_CLIENTE = "cliente";

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public interface ProfileCallback {
        void onSuccess(UserProfile profile);
        void onError(String message);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface AdminInvitationCallback {
        void onSuccess(String invitationId, String empresaId);
        void onError(String message);
    }

    public interface AdminProjectsCallback {
        void onSuccess(List<AdminProjectItem> projects);
        void onError(String message);
    }

    public interface AdminRequestsCallback {
        void onSuccess(List<AdminRequestItem> requests);
        void onError(String message);
    }

    public interface AdminReviewsCallback {
        void onSuccess(List<AdminReviewItem> reviews);
        void onError(String message);
    }

    public interface UserPropertyListCallback {
        void onSuccess(List<UsuarioPropertyListItem> projects);
        void onError(String message);
    }

    public interface UserProjectSearchCallback {
        void onSuccess(List<UserProjectSearchItem> projects);
        void onError(String message);
    }

    public interface UserExplorePageCallback {
        void onSuccess(UserExplorePage page);
        void onError(String message);
    }

    public interface ProjectDetailCallback {
        void onSuccess(ProjectDetail detail);
        void onError(String message);
    }

    public interface ProjectAssetsCallback {
        void onSuccess(ProjectAssets assets);
        void onError(String message);
    }

    public interface DeliveryReminderCallback {
        void onSuccess(List<ProjectDetail> dueProjects);
        void onError(String message);
    }

    public static class UserProfile {
        public final String uid;
        public final String nombre;
        public final String correo;
        public final String telefono;
        public final String rol;
        public final String nombres;
        public final String apellidos;
        public String documento = "";
        public String nacimiento = "";

        public UserProfile(String uid, String nombre, String correo, String telefono, String rol) {
            this(uid, splitNameParts(nombre)[0], splitNameParts(nombre)[1], correo, telefono, rol);
        }

        public UserProfile(
                String uid,
                String nombres,
                String apellidos,
                String correo,
                String telefono,
                String rol
        ) {
            this.uid = uid;
            this.nombres = nombres == null ? "" : nombres.trim();
            this.apellidos = apellidos == null ? "" : apellidos.trim();
            this.nombre = (this.nombres + " " + this.apellidos).trim();
            this.correo = correo;
            this.telefono = telefono;
            this.rol = rol;
        }

        private static String[] splitNameParts(String fullName) {
            String value = fullName == null ? "" : fullName.trim();
            if (value.isEmpty()) return new String[]{"", ""};
            String[] parts = value.split("\\s+", 2);
            return new String[]{parts[0], parts.length > 1 ? parts[1] : ""};
        }
    }

    public static class ProjectDetail {
        public final String projectId;
        public final String nombre;
        public final String descripcion;
        public final String direccion;
        public final String distrito;
        public final String precioDesde;
        public final String estadoProyecto;
        public final String fechaEntrega;
        public final String fechaEntregaISO;
        public final long fechaEntregaMillis;
        public final String qrValue;
        public final String imageUrl;
        public final double lat;
        public final double lng;

        public ProjectDetail(
                String projectId,
                String nombre,
                String descripcion,
                String direccion,
                String distrito,
                String precioDesde,
                String estadoProyecto,
                String fechaEntrega,
                String fechaEntregaISO,
                long fechaEntregaMillis,
                String qrValue,
                String imageUrl,
                double lat,
                double lng
        ) {
            this.projectId = projectId;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.direccion = direccion;
            this.distrito = distrito;
            this.precioDesde = precioDesde;
            this.estadoProyecto = estadoProyecto;
            this.fechaEntrega = fechaEntrega;
            this.fechaEntregaISO = fechaEntregaISO;
            this.fechaEntregaMillis = fechaEntregaMillis;
            this.qrValue = qrValue;
            this.imageUrl = imageUrl;
            this.lat = lat;
            this.lng = lng;
        }

        public boolean canCreateSeparation() {
            return ProjectBusinessRules.canCreateSeparation(estadoProyecto);
        }
    }

    public static class ProjectAssets {
        public final List<String> imageUrls;
        public final List<AdminProjectFormTypologyItem> typologies;
        public final List<AdminProjectFormAmenityItem> amenities;

        public ProjectAssets(
                List<String> imageUrls,
                List<AdminProjectFormTypologyItem> typologies,
                List<AdminProjectFormAmenityItem> amenities
        ) {
            this.imageUrls = imageUrls;
            this.typologies = typologies;
            this.amenities = amenities;
        }
    }

    /** A public project card together with the normalized data used only for local search. */
    public static class UserProjectSearchItem {
        public final UsuarioPropertyListItem project;
        public final String searchableText;

        public UserProjectSearchItem(UsuarioPropertyListItem project, String searchableText) {
            this.project = project;
            this.searchableText = searchableText == null ? "" : searchableText;
        }
    }

    /** A cursor-based page used exclusively by the client Explore catalogue. */
    public static class UserExplorePage {
        public final List<UsuarioPropertyListItem> projects;
        @Nullable public final DocumentSnapshot nextCursor;
        public final boolean hasMore;

        public UserExplorePage(
                List<UsuarioPropertyListItem> projects,
                @Nullable DocumentSnapshot nextCursor,
                boolean hasMore
        ) {
            this.projects = projects;
            this.nextCursor = nextCursor;
            this.hasMore = hasMore;
        }
    }

    public void signInOrCreateKnownDemoUser(
            Context context,
            String email,
            String password,
            ProfileCallback callback
    ) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        callback.onError("No se pudo leer la sesion de Firebase");
                        return;
                    }
                    seedLocalSnapshotIfNeeded(context);
                    readUserProfile(user.getUid(), email, callback);
                })
                .addOnFailureListener(loginError -> {
                    UserProfile demoProfile = knownDemoProfile(email, password);
                    if (demoProfile == null) {
                        callback.onError("Correo o contrasena incorrectos");
                        return;
                    }
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener(result -> {
                                FirebaseUser user = result.getUser();
                                if (user == null) {
                                    callback.onError("No se pudo crear la sesion de Firebase");
                                    return;
                                }
                                UserProfile profile = new UserProfile(
                                        user.getUid(),
                                        demoProfile.nombre,
                                        demoProfile.correo,
                                        demoProfile.telefono,
                                        demoProfile.rol
                                );
                                saveUserProfile(profile, new SimpleCallback() {
                                    @Override
                                    public void onSuccess() {
                                        seedLocalSnapshotIfNeeded(context);
                                        callback.onSuccess(profile);
                                    }

                                    @Override
                                    public void onError(String message) {
                                        callback.onError(message);
                                    }
                                });
                            })
                            .addOnFailureListener(createError ->
                                    callback.onError("Firebase Auth no permitio iniciar este usuario: "
                                            + safeMessage(createError)));
                });
    }

    public void registerClient(
            String nombre,
            String correo,
            String telefono,
            String password,
            ProfileCallback callback
    ) {
        auth.createUserWithEmailAndPassword(correo, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        callback.onError("No se pudo crear la sesion de Firebase");
                        return;
                    }
                    UserProfile profile = new UserProfile(user.getUid(), nombre, correo, telefono, ROLE_CLIENTE);
                    saveUserProfile(profile, new SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            callback.onSuccess(profile);
                        }

                        @Override
                        public void onError(String message) {
                            callback.onError(message);
                        }
                    });
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudo registrar en Firebase: " + safeMessage(error)));
    }

    public void readUserProfile(String uid, String fallbackEmail, ProfileCallback callback) {
        firestore.collection("usuarios").document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        UserProfile profile = new UserProfile(uid, fallbackEmail, fallbackEmail, "", ROLE_CLIENTE);
                        saveUserProfile(profile, new SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                callback.onSuccess(profile);
                            }

                            @Override
                            public void onError(String message) {
                                callback.onError(message);
                            }
                        });
                        return;
                    }
                    callback.onSuccess(profileFromSnapshot(uid, snapshot, fallbackEmail));
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudo leer usuarios/" + uid + ": " + safeMessage(error)));
    }

    public void saveUserProfile(UserProfile profile, SimpleCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", profile.uid);
        data.put("id", profile.uid);
        data.put("nombre", profile.nombre);
        data.put("nombres", firstName(profile.nombre));
        data.put("apellidos", lastName(profile.nombre));
        data.put("correo", profile.correo);
        data.put("email", profile.correo);
        data.put("telefono", profile.telefono);
        data.put("rol", normalizeRole(profile.rol));
        data.put("estado", "activo");

        firestore.collection("usuarios").document(profile.uid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(error ->
                        callback.onError("No se pudo guardar usuarios/" + profile.uid + ": " + safeMessage(error)));
    }

    public void addInmobiliaria(String name, String description, String photoUrl, String adminEmail, String dominioCorreo, SimpleCallback callback) {
        createAdminInvitation(name, description, photoUrl, adminEmail, dominioCorreo, new AdminInvitationCallback() {
            @Override
            public void onSuccess(String invitationId, String empresaId) {
                callback.onSuccess();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void createAdminInvitation(
            String name,
            String description,
            String photoUrl,
            String adminEmail,
            String dominioCorreo,
            AdminInvitationCallback callback
    ) {
        String companyName = CompanyNamePolicy.clean(name);
        String normalizedEmail = adminEmail == null ? "" : adminEmail.trim().toLowerCase(Locale.ROOT);
        firestore.collection("admin_invitations")
                .whereEqualTo("email", normalizedEmail)
                .get()
                .addOnSuccessListener(existing -> {
                    for (DocumentSnapshot invitation : existing.getDocuments()) {
                        if ("pendiente".equalsIgnoreCase(invitation.getString("estado"))) {
                            String pendingCompanyName = firstNonEmpty(invitation.getString("empresaNombre"));
                            if (CompanyNamePolicy.matches(companyName, pendingCompanyName)) {
                                callback.onSuccess(
                                        invitation.getId(),
                                        firstNonEmpty(invitation.getString("empresaId"))
                                );
                            } else {
                                String label = pendingCompanyName.isEmpty()
                                        ? "otra inmobiliaria"
                                        : "\"" + pendingCompanyName + "\"";
                                callback.onError("El correo ya tiene una invitaciÃ³n pendiente para " + label
                                        + ". Usa otro correo o completa/cancela la invitaciÃ³n existente.");
                            }
                            return;
                        }
                    }
                    createNewAdminInvitation(companyName, description, photoUrl, normalizedEmail, dominioCorreo, callback);
                })
                .addOnFailureListener(error ->
                        createNewAdminInvitation(companyName, description, photoUrl, normalizedEmail, dominioCorreo, callback));
    }

    private void createNewAdminInvitation(
            String name,
            String description,
            String photoUrl,
            String adminEmail,
            String dominioCorreo,
            AdminInvitationCallback callback
    ) {
        String newId = "inmobiliaria_" + System.currentTimeMillis();
        String invitationId = "admin_invitation_" + System.currentTimeMillis();
        Map<String, Object> data = new HashMap<>();
        data.put("id", newId);
        data.put("empresaId", newId);
        data.put("nombre", name);
        data.put("descripcion", description);
        data.put("fotoUrl", photoUrl);
        data.put("adminEmail", adminEmail);
        data.put("estado", "pendiente");
        data.put("createdAt", System.currentTimeMillis());
        if (dominioCorreo != null && !dominioCorreo.trim().isEmpty()) {
            data.put("dominio_correo", dominioCorreo.trim().toLowerCase(Locale.ROOT));
        }

        Map<String, Object> invitation = new HashMap<>();
        invitation.put("id", invitationId);
        invitation.put("empresaId", newId);
        invitation.put("empresaNombre", name);
        invitation.put("email", adminEmail);
        invitation.put("estado", "pendiente");
        invitation.put("createdAt", System.currentTimeMillis());
        invitation.put("expiresAt", System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000));

        WriteBatch batch = firestore.batch();
        batch.set(firestore.collection("empresas").document(newId), data, SetOptions.merge());
        batch.set(firestore.collection("admin_invitations").document(invitationId), invitation, SetOptions.merge());
        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess(invitationId, newId))
                .addOnFailureListener(error -> callback.onError("Error al guardar la invitación: " + safeMessage(error)));
    }

    public void completeAdminInvitation(
            String invitationId,
            UserProfile profile,
            SimpleCallback callback
    ) {
        firestore.collection("admin_invitations").document(invitationId).get()
                .addOnSuccessListener(invitation -> {
                    if (!invitation.exists()) {
                        callback.onError("La invitación no existe.");
                        return;
                    }
                    String invitationEmail = firstNonEmpty(invitation.getString("email"));
                    if (!invitationEmail.equalsIgnoreCase(profile.correo)) {
                        callback.onError("El correo no coincide con la invitación.");
                        return;
                    }
                    if ("aceptada".equalsIgnoreCase(invitation.getString("estado"))) {
                        // Already accepted — try to finish profile directly
                        String empresaId = firstNonEmpty(invitation.getString("empresaId"));
                        completeAdminStep2(invitationId, empresaId, invitation, profile, callback);
                        return;
                    }
                    String empresaId = firstNonEmpty(invitation.getString("empresaId"));

                    // --- Step 1: Mark invitation as accepted ---
                    Map<String, Object> accepted = new HashMap<>();
                    accepted.put("estado", "aceptada");
                    accepted.put("acceptedByUid", profile.uid);
                    accepted.put("acceptedAt", System.currentTimeMillis());
                    // Preserve required fields so rules pass
                    accepted.put("email", invitationEmail);
                    accepted.put("empresaId", empresaId);

                    firestore.collection("admin_invitations").document(invitationId)
                            .set(accepted, SetOptions.merge())
                            .addOnSuccessListener(unused ->
                                    // --- Step 2: Create user + update empresa ---
                                    completeAdminStep2(invitationId, empresaId, invitation, profile, callback))
                            .addOnFailureListener(error ->
                                    callback.onError("No se pudo aceptar la invitación: " + safeMessage(error)));
                })
                .addOnFailureListener(error -> callback.onError("No se pudo validar la invitación: " + safeMessage(error)));
    }

    private void completeAdminStep2(
            String invitationId,
            String empresaId,
            com.google.firebase.firestore.DocumentSnapshot invitation,
            UserProfile profile,
            SimpleCallback callback
    ) {
        firestore.collection("empresas").document(empresaId).get()
                .addOnSuccessListener(company -> completeAdminStep2WithCompany(
                        invitationId, empresaId, invitation, profile, company, callback))
                // The user profile still keeps the invitation name if an old company cannot be read.
                .addOnFailureListener(error -> completeAdminStep2WithCompany(
                        invitationId, empresaId, invitation, profile, null, callback));
    }

    private void completeAdminStep2WithCompany(
            String invitationId,
            String empresaId,
            com.google.firebase.firestore.DocumentSnapshot invitation,
            UserProfile profile,
            @Nullable com.google.firebase.firestore.DocumentSnapshot companySnapshot,
            SimpleCallback callback
    ) {
        String invitationCompanyName = CompanyNamePolicy.clean(
                firstNonEmpty(invitation.getString("empresaNombre")));
        String companyName = CompanyNamePolicy.resolve(
                companySnapshot == null ? "" : companySnapshot.getString("nombre"),
                companySnapshot == null ? "" : firstNonEmpty(
                        companySnapshot.getString("empresaNombre"),
                        companySnapshot.getString("inmobiliariaNombre")),
                "", "", invitationCompanyName);

        Map<String, Object> user = new HashMap<>();
        user.put("uid", profile.uid);
        user.put("id", profile.uid);
        user.put("nombre", profile.nombre);
        user.put("nombres", profile.nombres);
        user.put("apellidos", profile.apellidos);
        user.put("email", profile.correo);
        user.put("correo", profile.correo);
        user.put("telefono", profile.telefono);
        user.put("rol", "admin");
        user.put("estado", "activo");
        if (!profile.documento.isEmpty()) user.put("documento", profile.documento);
        if (!profile.nacimiento.isEmpty()) user.put("nacimiento", profile.nacimiento);
        user.put("empresaId", empresaId);
        user.put("inmobiliariaId", empresaId);
        if (!companyName.isEmpty()) {
            user.put("empresaNombre", companyName);
            user.put("inmobiliariaNombre", companyName);
        }
        user.put("invitationId", invitationId);
        user.put("profileNeedsCompletion", profile.nombres.isEmpty()
                || profile.apellidos.isEmpty()
                || profile.telefono == null
                || profile.telefono.trim().isEmpty());
        user.put("updatedAt", System.currentTimeMillis());

        Map<String, Object> company = new HashMap<>();
        company.put("adminUid", profile.uid);
        company.put("adminEmail", profile.correo);
        company.put("estado", "activo");
        company.put("updatedAt", System.currentTimeMillis());
        String existingCompanyName = companySnapshot == null ? "" : CompanyNamePolicy.resolve(
                companySnapshot.getString("nombre"),
                firstNonEmpty(companySnapshot.getString("empresaNombre"),
                        companySnapshot.getString("inmobiliariaNombre")),
                "", "", "");
        if (CompanyNamePolicy.shouldBackfill(existingCompanyName, companyName)) {
            company.put("nombre", companyName);
        }

        WriteBatch batch = firestore.batch();
        batch.set(firestore.collection("usuarios").document(profile.uid), user, SetOptions.merge());
        batch.set(firestore.collection("empresas").document(empresaId), company, SetOptions.merge());
        batch.commit()
                .addOnSuccessListener(unused ->
                        verifyCompletedAdminProfile(profile.uid, empresaId, callback))
                .addOnFailureListener(error -> callback.onError("No se pudo completar el registro: " + safeMessage(error)));
    }

    private void verifyCompletedAdminProfile(String uid, String empresaId, SimpleCallback callback) {
        firestore.collection("usuarios").document(uid).get()
                .addOnSuccessListener(user -> {
                    boolean complete = user.exists()
                            && "admin".equalsIgnoreCase(firstNonEmpty(user.getString("rol")))
                            && empresaId.equals(firstNonEmpty(user.getString("empresaId")))
                            && !firstNonEmpty(user.getString("nombres")).isEmpty()
                            && !firstNonEmpty(user.getString("apellidos")).isEmpty()
                            && !firstNonEmpty(user.getString("email"), user.getString("correo")).isEmpty()
                            && !firstNonEmpty(user.getString("telefono")).isEmpty();
                    if (complete) callback.onSuccess();
                    else callback.onError("El perfil administrador quedó incompleto. Intenta guardar nuevamente.");
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudo verificar el perfil administrador: " + safeMessage(error)));
    }

    public void seedLocalSnapshotIfNeeded(Context context) {
        firestore.collection(COLLECTION_META).document(META_SCHEMA_DOC).get()
                .addOnSuccessListener(snapshot -> {
                    if (SCHEMA_VERSION.equals(snapshot.getString("version"))) {
                        return;
                    }
                    uploadLocalSnapshot(context);
                });
    }

    public String projectIdForDraft(AdminProjectDraft draft, @Nullable String existingProjectId) {
        String stableId = firstNonEmpty(existingProjectId);
        if (!stableId.isEmpty()) {
            return stableId;
        }
        String projectId = slug(draft.getProjectName());
        return projectId.isEmpty() ? "proy_" + System.currentTimeMillis() : projectId;
    }

    public void saveProject(AdminProjectDraft draft, @Nullable String originalProjectTitle, SimpleCallback callback) {
        saveProjectWithImages(draft, originalProjectTitle, null, callback);
    }

    public void saveProjectWithImages(
            AdminProjectDraft draft,
            @Nullable String originalProjectTitle,
            @Nullable List<SupabaseStorageRepository.UploadResult> images,
            SimpleCallback callback
    ) {
        String projectId = projectIdForDraft(draft, originalProjectTitle);
        Task<QuerySnapshot> typologiesTask = firestore.collection("proyectos_tipologias")
                .whereEqualTo("projectId", projectId)
                .get();
        Task<QuerySnapshot> amenitiesTask = firestore.collection("proyectos_amenidades")
                .whereEqualTo("projectId", projectId)
                .get();
        Task<QuerySnapshot> imagesTask = images == null
                ? Tasks.forResult(null)
                : firestore.collection("proyectos_imagenes")
                        .whereEqualTo("projectId", projectId)
                        .get();
        Task<DocumentSnapshot> projectTask = firestore.collection("proyectos").document(projectId).get();
        Task<DocumentSnapshot> userTask = currentUid().isEmpty()
                ? Tasks.forResult(null)
                : firestore.collection("usuarios").document(currentUid()).get();

        Tasks.whenAll(typologiesTask, amenitiesTask, imagesTask, projectTask, userTask)
                .addOnSuccessListener(unused -> {
                    DocumentSnapshot currentUser = userTask.getResult();
                    String role = currentUser == null ? "" : firstNonEmpty(currentUser.getString("rol"));
                    String empresaId = currentUser == null ? "" : firstNonEmpty(
                            currentUser.getString("empresaId"),
                            currentUser.getString("inmobiliariaId")
                    );
                    if (!"admin".equalsIgnoreCase(role) && !"superadmin".equalsIgnoreCase(role)) {
                        callback.onError("Tu perfil administrador no está completo. Vuelve a iniciar sesión o completa tu perfil.");
                        return;
                    }
                    if ("admin".equalsIgnoreCase(role) && empresaId.isEmpty()) {
                        callback.onError("Tu administrador no está vinculado a una empresa inmobiliaria.");
                        return;
                    }
                    performProjectSave(
                            projectId,
                            draft,
                            images,
                            typologiesTask.getResult(),
                            amenitiesTask.getResult(),
                            imagesTask.getResult(),
                            projectTask.getResult(),
                            currentUser,
                            callback
                    );
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudieron preparar los datos del proyecto en "
                                + failedPreparationSource(
                                        typologiesTask,
                                        amenitiesTask,
                                        imagesTask,
                                        projectTask,
                                        userTask
                                )
                                + ": " + safeMessage(error)));
    }

    private void performProjectSave(
            String projectId,
            AdminProjectDraft draft,
            @Nullable List<SupabaseStorageRepository.UploadResult> images,
            QuerySnapshot previousTypologies,
            QuerySnapshot previousAmenities,
            @Nullable QuerySnapshot previousImages,
            @Nullable DocumentSnapshot previousProject,
            @Nullable DocumentSnapshot currentUser,
            SimpleCallback callback
    ) {
        SupabaseStorageRepository.UploadResult primaryImage =
                images == null || images.isEmpty() ? null : images.get(0);

        WriteBatch batch = firestore.batch();
        for (DocumentSnapshot document : previousTypologies.getDocuments()) {
            batch.delete(document.getReference());
        }
        for (DocumentSnapshot document : previousAmenities.getDocuments()) {
            batch.delete(document.getReference());
        }
        if (previousImages != null) {
            for (DocumentSnapshot document : previousImages.getDocuments()) {
                batch.delete(document.getReference());
            }
        }
        String estadoProyecto = ProjectBusinessRules.normalizeStatus(draft.getStatus());
        String estadoProyectoLabel = ProjectBusinessRules.displayStatus(draft.getStatus());
        String fechaEntregaISO = ProjectBusinessRules.deliveryIsoFromDisplay(draft.getDeliveryDate());
        long fechaEntregaMillis = ProjectBusinessRules.deliveryMillisFromDisplay(draft.getDeliveryDate());
        String qrValue = ProjectBusinessRules.qrValue(projectId);

        Map<String, Object> project = new HashMap<>();
        project.put("id", projectId);
        project.put("projectId", projectId);
        project.put("propertyId", projectId);
        project.put("adminId", currentUid());
        if (currentUser != null) {
            String empresaId = firstNonEmpty(currentUser.getString("empresaId"), currentUser.getString("inmobiliariaId"));
            if (!empresaId.isEmpty()) {
                project.put("empresaId", empresaId);
                project.put("inmobiliariaId", empresaId);
            }
        }
        project.put("nombre", draft.getProjectName());
        project.put("descripcion", draft.getDescription());
        project.put("direccion", draft.getAddress());
        project.put("distrito", draft.getCity());
        project.put("mapa", draft.getMapLabel());
        project.put("estado", draft.getStatus());
        project.put("estadoComercial", draft.getStatus());
        project.put("estadoProyecto", estadoProyecto);
        project.put("estadoProyectoLabel", estadoProyectoLabel);
        project.put("precioDesde", priceFromDraft(draft));
        project.put("currency", "PEN");
        project.put("badge", estadoProyectoLabel);
        project.put("lat", draft.getLatitude());
        project.put("lng", draft.getLongitude());
        project.put("ubicacion", locationMap(draft));
        project.put("puntosInteres", new ArrayList<>());
        project.put("qrValue", qrValue);
        project.put("deepLink", qrValue);
        if (primaryImage != null) {
            project.put("imageUrl", primaryImage.publicUrl);
            project.put("primaryImageUrl", primaryImage.publicUrl);
            project.put("imageStoragePath", primaryImage.storagePath);
            project.put("imageProvider", primaryImage.provider);
        } else if (images != null) {
            project.put("imageUrl", FieldValue.delete());
            project.put("primaryImageUrl", FieldValue.delete());
            project.put("imageStoragePath", FieldValue.delete());
            project.put("imageProvider", FieldValue.delete());
        }
        project.put("fechaEntrega", draft.getDeliveryDate());
        project.put("fechaEntregaEstimada", draft.getDeliveryDate());
        project.put("fechaEntregaISO", fechaEntregaISO);
        project.put("fechaEntregaMillis", fechaEntregaMillis);
        project.put("deliveryReminderSent", false);
        project.put("assignmentStatus", "ACTIVO");
        long now = System.currentTimeMillis();
        Long previousCreatedAt = previousProject == null ? null : previousProject.getLong("createdAt");
        project.put("createdAt", previousCreatedAt == null ? now : previousCreatedAt);
        project.put("updatedAt", now);
        batch.set(firestore.collection("proyectos").document(projectId), project, SetOptions.merge());

        List<AdminProjectFormTypologyItem> typologies = draft.getTypologies();
        for (int i = 0; i < typologies.size(); i++) {
            AdminProjectFormTypologyItem item = typologies.get(i);
            String id = projectId + "_tipologia_" + (i + 1);
            Map<String, Object> typology = new HashMap<>();
            typology.put("id", id);
            typology.put("typologyId", id);
            typology.put("projectId", projectId);
            typology.put("title", item.getTitle());
            typology.put("nombre", item.getTitle());
            typology.put("available", item.isAvailable());
            typology.put("area", item.getArea());
            typology.put("habitaciones", item.getBedrooms());
            typology.put("bedrooms", item.getBedrooms());
            typology.put("banos", item.getBathrooms());
            typology.put("bathrooms", item.getBathrooms());
            typology.put("montoTotal", item.getTotalAmountValue());
            typology.put("totalAmount", item.getTotalAmountValue());
            typology.put("montoTotalLabel", item.getTotalAmount());
            typology.put("totalAmountLabel", item.getTotalAmount());
            typology.put("montoSeparacion", item.getSeparationAmountValue());
            typology.put("separationAmount", item.getSeparationAmountValue());
            typology.put("montoSeparacionLabel", item.getSeparationAmount());
            typology.put("separationAmountLabel", item.getSeparationAmount());
            typology.put("currency", "PEN");
            batch.set(firestore.collection("proyectos_tipologias").document(id), typology, SetOptions.merge());
        }

        List<AdminProjectFormAmenityItem> amenities = draft.getAmenities();
        for (int i = 0; i < amenities.size(); i++) {
            AdminProjectFormAmenityItem item = amenities.get(i);
            String id = projectId + "_amenidad_" + (i + 1);
            Map<String, Object> amenity = new HashMap<>();
            amenity.put("id", id);
            amenity.put("amenityId", id);
            amenity.put("projectId", projectId);
            amenity.put("nombre", item.getTitle());
            amenity.put("title", item.getTitle());
            amenity.put("iconKey", item.getTitle().toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9]+", "_"));
            amenity.put("selected", item.isSelected());
            batch.set(firestore.collection("proyectos_amenidades").document(id), amenity, SetOptions.merge());
        }

        if (images != null) {
            for (int i = 0; i < images.size(); i++) {
                SupabaseStorageRepository.UploadResult image = images.get(i);
                String id = projectId + "_image_" + i;
                Map<String, Object> data = new HashMap<>();
                data.put("id", id);
                data.put("imageId", id);
                data.put("projectId", projectId);
                data.put("imageUrl", image.publicUrl);
                data.put("storagePath", image.storagePath);
                data.put("provider", image.provider);
                data.put("position", i);
                data.put("createdAt", System.currentTimeMillis());
                batch.set(firestore.collection("proyectos_imagenes").document(id), data, SetOptions.merge());
            }
        }

        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(error ->
                        callback.onError("No se pudo guardar el proyecto en Firestore: " + safeMessage(error)));
    }

    public void readAdminProjects(AdminProjectsCallback callback) {
        String uid = currentUid();
        com.google.firebase.firestore.Query query = firestore.collection("proyectos");
        if (!uid.isEmpty()) {
            query = query.whereEqualTo("adminId", uid);
        }
        query.get()
                .addOnSuccessListener(snapshot -> {
                    List<AdminProjectItem> items = new ArrayList<>();
                    for (DocumentSnapshot project : snapshot.getDocuments()) {
                        String status = displayStatus(project);
                        items.add(new AdminProjectItem(
                                project.getId(),
                                firstNonEmpty(project.getString("nombre"), "Proyecto sin nombre"),
                                firstNonEmpty(project.getString("direccion"), project.getString("distrito"), "Ubicacion pendiente"),
                                AdminProjectFormTypologyItem.formatAsPen(
                                        firstNonEmpty(project.getString("precioDesde"), "Precio por definir")),
                                status,
                                imageRes(firstNonEmpty(project.getString("imageKey"), "sa_profile_admin")),
                                firstNonEmpty(project.getString("primaryImageUrl"), project.getString("imageUrl"))
                        ));
                    }
                    callback.onSuccess(items);
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudo leer proyectos desde Firestore: " + safeMessage(error)));
    }

    public void readAdminAdvisorRequests(AdminRequestsCallback callback) {
        firestore.collection("solicitudes_asesor")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<AdminRequestItem> items = new ArrayList<>();
                    for (DocumentSnapshot request : snapshot.getDocuments()) {
                        String status = firstNonEmpty(request.getString("estado"), "pendiente")
                                .toUpperCase(Locale.ROOT);
                        items.add(new AdminRequestItem(
                                firstNonEmpty(request.getString("id"), request.getId()),
                                firstNonEmpty(request.getString("nombre"), request.getString("asesorNombre"), "Asesor sin nombre"),
                                firstNonEmpty(request.getString("email"), request.getString("correo")),
                                firstNonEmpty(request.getString("subtitle"), request.getString("subtitulo"), status),
                                firstNonEmpty(request.getString("descripcion"), request.getString("mensaje"), "Solicitud de asesor"),
                                status,
                                imageRes(firstNonEmpty(request.getString("avatarKey"), "sa_profile_asesor_1")),
                                firstNonEmpty(request.getString("proyectoNombre"), request.getString("projectName"))
                        ));
                    }
                    callback.onSuccess(items);
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudieron leer solicitudes desde Firestore: " + safeMessage(error)));
    }

    public void decideAdvisorRequest(String requestId, String newStatus, SimpleCallback callback) {
        String id = firstNonEmpty(requestId);
        String normalizedStatus = firstNonEmpty(newStatus, "pendiente").toLowerCase(Locale.ROOT);
        if (id.isEmpty() || (!"aceptada".equals(normalizedStatus) && !"rechazada".equals(normalizedStatus))) {
            callback.onError("Solicitud o decision invalida.");
            return;
        }
        firestore.collection("solicitudes_asesor").document(id).get()
                .addOnSuccessListener(request -> {
                    if (!request.exists()) {
                        callback.onError("La solicitud ya no existe en Firestore.");
                        return;
                    }
                    String currentStatus = firstNonEmpty(request.getString("estado"), "pendiente").toLowerCase(Locale.ROOT);
                    if (!"pendiente".equals(currentStatus)) {
                        callback.onError("Esta solicitud ya fue " + currentStatus + " y no se puede revertir.");
                        return;
                    }
                    Map<String, Object> update = new HashMap<>();
                    update.put("estado", normalizedStatus);
                    update.put("subtitle", "aceptada".equals(normalizedStatus)
                            ? "Aceptada hace un momento"
                            : "Rechazada hace un momento");
                    update.put("decisionFinal", true);
                    update.put("decidedAt", System.currentTimeMillis());
                    update.put("decidedBy", currentUid());
                    firestore.collection("solicitudes_asesor").document(id)
                            .set(update, SetOptions.merge())
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(error ->
                                    callback.onError("No se pudo actualizar la solicitud: " + safeMessage(error)));
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudo validar la solicitud: " + safeMessage(error)));
    }

    public void readAdvisorReviews(String advisorId, AdminReviewsCallback callback) {
        com.google.firebase.firestore.Query query = firestore.collection("resenas");
        if (!firstNonEmpty(advisorId).isEmpty()) {
            query = query.whereEqualTo("asesorId", advisorId);
        }
        query.get()
                .addOnSuccessListener(snapshot -> {
                    List<AdminReviewItem> items = new ArrayList<>();
                    for (DocumentSnapshot review : snapshot.getDocuments()) {
                        items.add(new AdminReviewItem(
                                firstNonEmpty(review.getString("clienteNombre"), review.getString("cliente"), "Cliente"),
                                firstNonEmpty(review.getString("fecha"), objectString(review.get("createdAt")), "Fecha no registrada"),
                                firstNonEmpty(review.getString("proyectoNombre"), review.getString("projectName"), "Proyecto"),
                                firstNonEmpty(review.getString("comentario"), review.getString("reviewText"), ""),
                                ratingLabel(review.get("rating")),
                                imageRes(firstNonEmpty(review.getString("avatarKey"), "sa_profile_user_1"))
                        ));
                    }
                    callback.onSuccess(items);
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudieron leer resenas desde Firestore: " + safeMessage(error)));
    }

    public void readUserPropertyListItems(UserPropertyListCallback callback) {
        firestore.collection("proyectos").get()
                .addOnSuccessListener(snapshot -> {
                    List<UsuarioPropertyListItem> items = new ArrayList<>();
                    for (DocumentSnapshot project : snapshot.getDocuments()) {
                        items.add(userPropertyListItemFromSnapshot(project));
                    }
                    callback.onSuccess(items);
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudo leer inmuebles desde Firestore: " + safeMessage(error)));
    }

    /**
     * Reads the public catalogue in stable pages. Projects created by the current admin flow
     * always contain updatedAt, so this preserves a deterministic "most recent" order without
     * downloading the entire catalogue on every Explore screen opening.
     */
    public void readUserExplorePage(
            @Nullable DocumentSnapshot afterCursor,
            int requestedLimit,
            UserExplorePageCallback callback
    ) {
        int pageSize = Math.max(1, Math.min(requestedLimit, 20));
        Query query = firestore.collection("proyectos")
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(pageSize);
        if (afterCursor != null) query = query.startAfter(afterCursor);

        query.get()
                .addOnSuccessListener(projectsSnapshot -> {
                    List<DocumentSnapshot> projects = projectsSnapshot.getDocuments();
                    if (projects.isEmpty()) {
                        callback.onSuccess(new UserExplorePage(new ArrayList<>(), null, false));
                        return;
                    }
                    loadExploreTypologies(projects, typologiesByProject -> {
                        List<UsuarioPropertyListItem> items = new ArrayList<>();
                        for (DocumentSnapshot project : projects) {
                            String storedSummary = firstNonEmpty(project.getString("typologiesSummary"));
                            String summary = storedSummary.isEmpty()
                                    ? buildExploreTypologySummary(typologiesByProject.get(project.getId()))
                                    : storedSummary;
                            items.add(userPropertyListItemFromSnapshot(project, summary));
                        }
                        DocumentSnapshot nextCursor = projects.get(projects.size() - 1);
                        callback.onSuccess(new UserExplorePage(items, nextCursor, projects.size() == pageSize));
                    });
                })
                .addOnFailureListener(error -> callback.onError(
                        "No se pudieron cargar los proyectos: " + safeMessage(error)
                ));
    }

    private interface ExploreTypologiesCallback {
        void onLoaded(Map<String, List<DocumentSnapshot>> typologiesByProject);
    }

    private void loadExploreTypologies(
            List<DocumentSnapshot> projects,
            ExploreTypologiesCallback callback
    ) {
        List<String> projectIds = new ArrayList<>();
        for (DocumentSnapshot project : projects) projectIds.add(project.getId());

        Task<QuerySnapshot> canonical = firestore.collection("proyectos_tipologias")
                .whereIn("projectId", projectIds).get();
        Task<QuerySnapshot> legacyProperty = firestore.collection("proyectos_tipologias")
                .whereIn("propertyId", projectIds).get();
        Task<QuerySnapshot> legacyProject = firestore.collection("proyectos_tipologias")
                .whereIn("proyectoId", projectIds).get();

        Tasks.whenAll(canonical, legacyProperty, legacyProject)
                .addOnSuccessListener(unused -> {
                    Map<String, List<DocumentSnapshot>> result = new HashMap<>();
                    appendExploreTypologies(canonical.getResult(), result);
                    appendExploreTypologies(legacyProperty.getResult(), result);
                    appendExploreTypologies(legacyProject.getResult(), result);
                    callback.onLoaded(result);
                })
                // Typologies enrich a card only; an unavailable legacy child collection must not
                // hide otherwise readable projects from Explore.
                .addOnFailureListener(error -> callback.onLoaded(new HashMap<>()));
    }

    private void appendExploreTypologies(
            QuerySnapshot snapshot,
            Map<String, List<DocumentSnapshot>> target
    ) {
        for (DocumentSnapshot typology : snapshot.getDocuments()) {
            String projectId = firstNonEmpty(
                    typology.getString("projectId"),
                    typology.getString("propertyId"),
                    typology.getString("proyectoId")
            );
            if (projectId.isEmpty()) continue;
            List<DocumentSnapshot> current = target.get(projectId);
            if (current == null) {
                current = new ArrayList<>();
                target.put(projectId, current);
            }
            boolean alreadyAdded = false;
            for (DocumentSnapshot item : current) {
                if (item.getId().equals(typology.getId())) {
                    alreadyAdded = true;
                    break;
                }
            }
            if (!alreadyAdded) current.add(typology);
        }
    }

    private String buildExploreTypologySummary(@Nullable List<DocumentSnapshot> typologies) {
        if (typologies == null || typologies.isEmpty()) return "";
        List<String> bedrooms = new ArrayList<>();
        List<String> areas = new ArrayList<>();
        for (DocumentSnapshot typology : typologies) {
            String bedroom = firstNonEmpty(typology.getString("bedrooms"), typology.getString("habitaciones"));
            String area = firstNonEmpty(typology.getString("area"));
            if (!bedroom.isEmpty() && !bedrooms.contains(bedroom)) bedrooms.add(bedroom);
            if (!area.isEmpty() && !areas.contains(area)) areas.add(area);
        }
        return ExploreProjectPresentationPolicy.typologySummary(bedrooms, areas);
    }

    /**
     * Loads the public project catalogue and its public child data once. Firestore cannot perform
     * arbitrary substring searches across these fields, so the UI filters the returned text locally.
     */
    public void readUserProjectSearchItems(UserProjectSearchCallback callback) {
        Task<QuerySnapshot> projectsTask = firestore.collection("proyectos").get();
        Task<QuerySnapshot> typologiesTask = firestore.collection("proyectos_tipologias").get();
        Task<QuerySnapshot> amenitiesTask = firestore.collection("proyectos_amenidades").get();

        Tasks.whenAll(projectsTask, typologiesTask, amenitiesTask)
                .addOnSuccessListener(unused -> {
                    Map<String, List<String>> childTerms = new HashMap<>();
                    appendChildSearchTerms(typologiesTask.getResult(), childTerms,
                            new String[]{"title", "nombre", "area", "bedrooms", "habitaciones", "bathrooms", "banos"});
                    appendChildSearchTerms(amenitiesTask.getResult(), childTerms,
                            new String[]{"title", "nombre", "descripcion", "iconKey"});

                    List<UserProjectSearchItem> items = new ArrayList<>();
                    for (DocumentSnapshot project : projectsTask.getResult().getDocuments()) {
                        List<String> searchableValues = new ArrayList<>();
                        appendProjectSearchTerms(project, searchableValues);
                        List<String> projectChildTerms = childTerms.get(project.getId());
                        if (projectChildTerms != null) searchableValues.addAll(projectChildTerms);
                        items.add(new UserProjectSearchItem(
                                userPropertyListItemFromSnapshot(project),
                                joinSearchTerms(searchableValues)
                        ));
                    }
                    callback.onSuccess(items);
                })
                .addOnFailureListener(error -> callback.onError(
                        "No se pudieron cargar los proyectos para buscar: " + safeMessage(error)
                ));
    }

    private UsuarioPropertyListItem userPropertyListItemFromSnapshot(DocumentSnapshot project) {
        return userPropertyListItemFromSnapshot(project, firstNonEmpty(project.getString("typologiesSummary"), ""));
    }

    private UsuarioPropertyListItem userPropertyListItemFromSnapshot(
            DocumentSnapshot project,
            String typologiesSummary
    ) {
        String imageKey = firstNonEmpty(project.getString("userImageKey"), project.getString("imageKey"), "user_featured_house");
        return new UsuarioPropertyListItem(
                project.getId(),
                firstNonEmpty(project.getString("badge"), displayStatus(project), "PROYECTO"),
                firstNonEmpty(project.getString("nombre"), project.getString("title"), "Proyecto sin nombre"),
                firstNonEmpty(project.getString("direccion"), project.getString("distrito"), "Ubicacion pendiente"),
                firstNonEmpty(project.getString("precioDesde"), "Precio por definir"),
                fallbackImageRes(imageKey),
                firstNonEmpty(project.getString("primaryImageUrl"), project.getString("imageUrl")),
                ProjectBusinessRules.normalizeStatus(firstNonEmpty(
                        project.getString("estadoProyecto"),
                        project.getString("estadoComercial"),
                        project.getString("estado")
                )),
                firstNonEmpty(project.getString("fechaEntregaEstimada"), project.getString("fechaEntrega")),
                ProjectBusinessRules.qrValue(project.getId()),
                firstNonEmpty(typologiesSummary, ""),
                doubleValue(project.get("lat"), Double.NaN),
                doubleValue(project.get("lng"), Double.NaN),
                firstNonEmpty(project.getString("distrito")),
                firstNonEmpty(project.getString("ciudad"))
        );
    }

    private void appendProjectSearchTerms(DocumentSnapshot project, List<String> target) {
        String[] fields = {
                "nombre", "title", "titulo", "descripcion", "direccion", "distrito", "ciudad",
                "mapa", "ubicacion", "zona", "estado", "estadoComercial", "estadoProyecto",
                "tipo", "categoria", "caracteristicas", "amenidades", "ambientes", "tipologiesSummary"
        };
        for (String field : fields) {
            appendSearchValue(project.get(field), target);
        }
    }

    private void appendChildSearchTerms(
            QuerySnapshot snapshot,
            Map<String, List<String>> childTerms,
            String[] fields
    ) {
        for (DocumentSnapshot child : snapshot.getDocuments()) {
            String projectId = firstNonEmpty(
                    child.getString("projectId"), child.getString("propertyId"), child.getString("proyectoId")
            );
            if (projectId.isEmpty()) continue;
            List<String> terms = childTerms.get(projectId);
            if (terms == null) {
                terms = new ArrayList<>();
                childTerms.put(projectId, terms);
            }
            for (String field : fields) {
                appendSearchValue(child.get(field), terms);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void appendSearchValue(Object value, List<String> target) {
        if (value == null) return;
        if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean) {
            String text = String.valueOf(value).trim();
            if (!text.isEmpty()) target.add(text);
            return;
        }
        if (value instanceof Map) {
            for (Object nested : ((Map<Object, Object>) value).values()) {
                appendSearchValue(nested, target);
            }
            return;
        }
        if (value instanceof Iterable) {
            for (Object nested : (Iterable<Object>) value) {
                appendSearchValue(nested, target);
            }
        }
    }

    private String joinSearchTerms(List<String> terms) {
        StringBuilder result = new StringBuilder();
        for (String term : terms) {
            if (term == null || term.trim().isEmpty()) continue;
            if (result.length() > 0) result.append(' ');
            result.append(term.trim());
        }
        return result.toString();
    }

    public void readProjectDetail(String projectIdOrTitle, ProjectDetailCallback callback) {
        String safeValue = firstNonEmpty(projectIdOrTitle);
        if (safeValue.isEmpty()) {
            callback.onError("No se recibio el proyecto a consultar.");
            return;
        }
        firestore.collection("proyectos").document(safeValue).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        callback.onSuccess(projectDetailFromSnapshot(document));
                        return;
                    }
                    firestore.collection("proyectos")
                            .whereEqualTo("nombre", safeValue)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                if (snapshot.isEmpty()) {
                                    callback.onError("No se encontro el proyecto en Firestore.");
                                } else {
                                    callback.onSuccess(projectDetailFromSnapshot(snapshot.getDocuments().get(0)));
                                }
                            })
                            .addOnFailureListener(error ->
                                    callback.onError("No se pudo leer el proyecto: " + safeMessage(error)));
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudo leer el proyecto: " + safeMessage(error)));
    }

    /**
     * Obtiene un proyecto exclusivamente por el ID de su documento Firestore.
     * Se usa para enlaces y QR, donde buscar por nombre podria abrir un proyecto equivocado.
     */
    public void readProjectDetailById(String projectId, ProjectDetailCallback callback) {
        String safeProjectId = firstNonEmpty(projectId);
        if (safeProjectId.isEmpty()) {
            callback.onError("No se recibio el ID del proyecto.");
            return;
        }
        firestore.collection("proyectos").document(safeProjectId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        callback.onSuccess(projectDetailFromSnapshot(document));
                    } else {
                        callback.onError("No se encontro el proyecto solicitado.");
                    }
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudo leer el proyecto: " + safeMessage(error)));
    }

    /** Resolves a canonical document ID and supports one legacy projectId/propertyId reference. */
    public void readProjectDetailByReference(String reference, ProjectDetailCallback callback) {
        String safeReference = firstNonEmpty(reference);
        if (safeReference.isEmpty()) {
            callback.onError("No se recibio el ID del proyecto.");
            return;
        }
        firestore.collection("proyectos").document(safeReference).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        callback.onSuccess(projectDetailFromSnapshot(document));
                        return;
                    }
                    resolveLegacyProjectReference("propertyId", safeReference, callback, true);
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudo leer el proyecto: " + safeMessage(error)));
    }

    private void resolveLegacyProjectReference(
            String field,
            String reference,
            ProjectDetailCallback callback,
            boolean tryProjectId
    ) {
        firestore.collection("proyectos").whereEqualTo(field, reference).limit(2).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.size() == 1) {
                        callback.onSuccess(projectDetailFromSnapshot(snapshot.getDocuments().get(0)));
                    } else if (snapshot.size() > 1) {
                        callback.onError("La referencia del proyecto es ambigua.");
                    } else if (tryProjectId) {
                        resolveLegacyProjectReference("projectId", reference, callback, false);
                    } else {
                        callback.onError("No se encontro el proyecto solicitado.");
                    }
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudo leer el proyecto: " + safeMessage(error)));
    }

    public void readProjectAssets(String projectId, ProjectAssetsCallback callback) {
        if (firstNonEmpty(projectId).isEmpty()) {
            callback.onError("No se recibio el ID del proyecto.");
            return;
        }
        firestore.collection("proyectos_imagenes")
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnSuccessListener(imagesSnapshot -> {
                    List<String> imageUrls = new ArrayList<>();
                    Map<Integer, DocumentSnapshot> imagesByPosition = new HashMap<>();
                    int legacyPosition = 0;
                    for (DocumentSnapshot image : imagesSnapshot.getDocuments()) {
                        int position = image.contains("position")
                                ? intValue(image.get("position"))
                                : legacyPosition++;
                        DocumentSnapshot current = imagesByPosition.get(position);
                        if (current == null || longValue(image.get("createdAt")) >= longValue(current.get("createdAt"))) {
                            imagesByPosition.put(position, image);
                        }
                    }
                    List<Integer> positions = new ArrayList<>(imagesByPosition.keySet());
                    java.util.Collections.sort(positions);
                    for (Integer position : positions) {
                        DocumentSnapshot image = imagesByPosition.get(position);
                        String url = firstNonEmpty(image.getString("imageUrl"));
                        if (!url.isEmpty() && !imageUrls.contains(url)) {
                            imageUrls.add(url);
                        }
                    }
                    firestore.collection("proyectos_tipologias")
                            .whereEqualTo("projectId", projectId)
                            .get()
                            .addOnSuccessListener(typologiesSnapshot -> {
                                List<AdminProjectFormTypologyItem> typologies = new ArrayList<>();
                                for (DocumentSnapshot item : typologiesSnapshot.getDocuments()) {
                                    typologies.add(new AdminProjectFormTypologyItem(
                                            firstNonEmpty(item.getString("title"), item.getString("nombre")),
                                            !Boolean.FALSE.equals(item.getBoolean("available")),
                                            firstNonEmpty(item.getString("area")),
                                            firstNonEmpty(item.getString("bedrooms"), item.getString("habitaciones")),
                                            firstNonEmpty(item.getString("bathrooms"), item.getString("banos")),
                                            amountString(item.getString("totalAmountLabel"), item.getString("montoTotalLabel"),
                                                    objectString(item.get("totalAmount")), objectString(item.get("montoTotal"))),
                                            amountString(item.getString("separationAmountLabel"), item.getString("montoSeparacionLabel"),
                                                    objectString(item.get("separationAmount")), objectString(item.get("montoSeparacion")))
                                    ));
                                }
                                firestore.collection("proyectos_amenidades")
                                        .whereEqualTo("projectId", projectId)
                                        .get()
                                        .addOnSuccessListener(amenitiesSnapshot -> {
                                            List<AdminProjectFormAmenityItem> amenities = new ArrayList<>();
                                            for (DocumentSnapshot item : amenitiesSnapshot.getDocuments()) {
                                                String title = firstNonEmpty(
                                                        item.getString("title"),
                                                        item.getString("nombre")
                                                );
                                                amenities.add(new AdminProjectFormAmenityItem(
                                                        title,
                                                        AmenityIconResolver.resolve(title),
                                                        !Boolean.FALSE.equals(item.getBoolean("selected"))
                                                ));
                                            }
                                            callback.onSuccess(new ProjectAssets(imageUrls, typologies, amenities));
                                        })
                                        .addOnFailureListener(error ->
                                                callback.onError("No se pudieron leer amenidades: " + safeMessage(error)));
                            })
                            .addOnFailureListener(error ->
                                    callback.onError("No se pudieron leer tipologias: " + safeMessage(error)));
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudieron leer imagenes: " + safeMessage(error)));
    }

    public void checkDeliveryDueProjectNotifications(DeliveryReminderCallback callback) {
        String uid = currentUid();
        if (uid.isEmpty()) {
            callback.onError("No hay administrador autenticado para revisar entregas.");
            return;
        }
        firestore.collection("proyectos")
                .whereEqualTo("adminId", uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<ProjectDetail> dueProjects = new ArrayList<>();
                    WriteBatch batch = firestore.batch();
                    long now = System.currentTimeMillis();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        ProjectDetail detail = projectDetailFromSnapshot(document);
                        boolean reminderSent = Boolean.TRUE.equals(document.getBoolean("deliveryReminderSent"));
                        if (!reminderSent && ProjectBusinessRules.deliveryDateReached(detail.fechaEntregaMillis, detail.estadoProyecto)) {
                            dueProjects.add(detail);
                            Map<String, Object> update = new HashMap<>();
                            update.put("deliveryReminderSent", true);
                            update.put("deliveryReminderSentAt", now);
                            batch.set(document.getReference(), update, SetOptions.merge());

                            String notificationId = "delivery_due_" + detail.projectId;
                            Map<String, Object> notification = new HashMap<>();
                            notification.put("id", notificationId);
                            notification.put("recipientId", uid);
                            notification.put("recipientRole", "admin");
                            notification.put("tipo", "project_delivery_due");
                            notification.put("titulo", "Revisar estado del proyecto");
                            notification.put("body", detail.nombre + " llego a su fecha estimada de entrega. Revisa si debe pasar a En venta.");
                            notification.put("projectId", detail.projectId);
                            notification.put("createdAt", now);
                            notification.put("read", false);
                            batch.set(firestore.collection("notificaciones").document(notificationId), notification, SetOptions.merge());
                        }
                    }
                    if (dueProjects.isEmpty()) {
                        callback.onSuccess(dueProjects);
                        return;
                    }
                    batch.commit()
                            .addOnSuccessListener(unused -> callback.onSuccess(dueProjects))
                            .addOnFailureListener(error ->
                                    callback.onError("No se pudo registrar recordatorio de entrega: " + safeMessage(error)));
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudieron revisar fechas de entrega: " + safeMessage(error)));
    }

    public void saveProjectImages(
            String projectId,
            List<SupabaseStorageRepository.UploadResult> images,
            SimpleCallback callback
    ) {
        if (images == null || images.isEmpty()) {
            callback.onSuccess();
            return;
        }
        WriteBatch batch = firestore.batch();
        for (int i = 0; i < images.size(); i++) {
            SupabaseStorageRepository.UploadResult image = images.get(i);
            String id = projectId + "_image_" + i;
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("imageId", id);
            data.put("projectId", projectId);
            data.put("imageUrl", image.publicUrl);
            data.put("storagePath", image.storagePath);
            data.put("provider", image.provider);
            data.put("position", i);
            data.put("createdAt", System.currentTimeMillis());
            batch.set(firestore.collection("proyectos_imagenes").document(id), data, SetOptions.merge());
        }
        SupabaseStorageRepository.UploadResult primaryImage = images.get(0);
        Map<String, Object> projectImageData = new HashMap<>();
        projectImageData.put("imageUrl", primaryImage.publicUrl);
        projectImageData.put("primaryImageUrl", primaryImage.publicUrl);
        projectImageData.put("imageStoragePath", primaryImage.storagePath);
        projectImageData.put("imageProvider", primaryImage.provider);
        projectImageData.put("updatedAt", System.currentTimeMillis());
        batch.set(firestore.collection("proyectos").document(projectId), projectImageData, SetOptions.merge());
        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(error ->
                        callback.onError("No se pudieron guardar las imagenes del proyecto en Firestore: "
                                + safeMessage(error)));
    }

    public void saveCurrentUserAvatar(SupabaseStorageRepository.UploadResult image, SimpleCallback callback) {
        String uid = currentUid();
        if (uid.isEmpty()) {
            callback.onError("No hay usuario autenticado para guardar avatar");
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("avatarUrl", image.publicUrl);
        data.put("avatarStoragePath", image.storagePath);
        data.put("avatarProvider", image.provider);
        firestore.collection("usuarios").document(uid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(error ->
                        callback.onError("No se pudo guardar avatarUrl en Firestore: " + safeMessage(error)));
    }

    public void saveCompanyImage(
            int slot,
            SupabaseStorageRepository.UploadResult image,
            SimpleCallback callback
    ) {
        String uid = currentUid();
        if (uid.isEmpty()) {
            callback.onError("No hay usuario autenticado para guardar imagen de empresa");
            return;
        }
        firestore.collection("usuarios").document(uid).get()
                .addOnSuccessListener(user -> {
                    String empresaId = firstNonEmpty(user.getString("empresaId"), user.getString("inmobiliariaId"));
                    if (empresaId.isEmpty()) {
                        callback.onError("El administrador no está vinculado a una empresa.");
                        return;
                    }
                    Map<String, Object> data = new HashMap<>();
                    data.put("adminId", uid);
                    data.put(slot == 0 ? "companyImageUrl" : "companySecondaryImageUrl", image.publicUrl);
                    data.put(slot == 0 ? "companyImageStoragePath" : "companySecondaryImageStoragePath", image.storagePath);
                    data.put("provider", image.provider);
                    data.put("updatedAt", System.currentTimeMillis());
                    firestore.collection("empresas").document(empresaId)
                            .set(data, SetOptions.merge())
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(error ->
                                    callback.onError("No se pudo guardar imagen de empresa en Firestore: "
                                            + safeMessage(error)));
                })
                .addOnFailureListener(error -> callback.onError("No se pudo resolver la empresa: " + safeMessage(error)));
    }

    private void uploadLocalSnapshot(Context context) {
        try {
            JSONObject root = new JSONObject(new LocalSchemaStorage(context).exportSchemaSnapshot());
            WriteBatch batch = firestore.batch();
            Iterator<String> collections = root.keys();
            while (collections.hasNext()) {
                String collection = collections.next();
                JSONArray rows = root.optJSONArray(collection);
                if (rows == null) {
                    continue;
                }
                for (int i = 0; i < rows.length(); i++) {
                    JSONObject object = rows.optJSONObject(i);
                    if (object == null) {
                        continue;
                    }
                    String docId = documentIdFor(collection, object, i);
                    Map<String, Object> map = jsonObjectToMap(object);
                    applyFirestoreAliases(collection, docId, map);
                    batch.set(firestore.collection(collection).document(docId), map, SetOptions.merge());
                }
            }
            Map<String, Object> meta = new HashMap<>();
            meta.put("version", SCHEMA_VERSION);
            meta.put("seededAt", System.currentTimeMillis());
            batch.set(firestore.collection(COLLECTION_META).document(META_SCHEMA_DOC), meta, SetOptions.merge());
            batch.commit();
        } catch (JSONException ignored) {
            // El seed local es controlado por la app; si falla, Auth/registro siguen funcionando.
        }
    }

    private void applyFirestoreAliases(String collection, String docId, Map<String, Object> map) {
        map.put("firebaseId", docId);
        if ("usuarios".equals(collection)) {
            map.put("uid", valueOr(map.get("uid"), docId));
            map.put("correo", valueOr(map.get("correo"), map.get("email")));
            String nombre = String.valueOf(valueOr(map.get("nombre"),
                    (valueOr(map.get("nombres"), "") + " " + valueOr(map.get("apellidos"), "")).trim()));
            map.put("nombre", nombre);
        } else if ("proyectos".equals(collection)) {
            // The Firestore document ID is the only canonical project identifier. Keeping aliases
            // synchronized prevents a legacy projectId from producing a QR for another document.
            map.put("id", docId);
            map.put("projectId", docId);
            map.put("propertyId", docId);
            map.put("estado", valueOr(map.get("estado"), map.get("estadoComercial")));
            map.put("estadoProyecto", ProjectBusinessRules.normalizeStatus(String.valueOf(valueOr(map.get("estadoProyecto"), map.get("estado")))));
            map.put("estadoProyectoLabel", ProjectBusinessRules.displayStatus(String.valueOf(valueOr(map.get("estadoProyecto"), map.get("estado")))));
            map.put("fechaEntrega", valueOr(map.get("fechaEntrega"), ""));
            map.put("fechaEntregaEstimada", valueOr(map.get("fechaEntregaEstimada"), map.get("fechaEntrega")));
            map.put("fechaEntregaISO", ProjectBusinessRules.deliveryIsoFromDisplay(String.valueOf(valueOr(map.get("fechaEntrega"), ""))));
            map.put("fechaEntregaMillis", ProjectBusinessRules.deliveryMillisFromDisplay(String.valueOf(valueOr(map.get("fechaEntrega"), ""))));
            String canonicalQrValue = ProjectBusinessRules.qrValue(docId);
            map.put("qrValue", canonicalQrValue);
            map.put("deepLink", canonicalQrValue);
        } else if ("proyectos_tipologias".equals(collection)) {
            map.put("typologyId", valueOr(map.get("typologyId"), docId));
            map.put("habitaciones", valueOr(map.get("habitaciones"), map.get("bedrooms")));
            map.put("banos", valueOr(map.get("banos"), map.get("bathrooms")));
            map.put("montoTotal", valueOr(map.get("montoTotal"), map.get("totalAmount")));
            map.put("montoSeparacion", valueOr(map.get("montoSeparacion"), map.get("separationAmount")));
        } else if ("proyectos_amenidades".equals(collection)) {
            map.put("amenityId", valueOr(map.get("amenityId"), docId));
            map.put("nombre", valueOr(map.get("nombre"), map.get("title")));
            map.put("icono", valueOr(map.get("icono"), map.get("icon")));
        }
    }

    private String documentIdFor(String collection, JSONObject object, int index) {
        String id = object.optString("id", "");
        if (!id.isEmpty()) {
            return slug(id);
        }
        String projectId = object.optString("projectId", "");
        String title = object.optString("title", "");
        String imageKey = object.optString("imageKey", "");
        if (!projectId.isEmpty() && !title.isEmpty()) {
            return slug(projectId + "_" + title);
        }
        if (!projectId.isEmpty() && !imageKey.isEmpty()) {
            return slug(projectId + "_" + imageKey);
        }
        return slug(collection + "_" + index);
    }

    private Map<String, Object> jsonObjectToMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keys = object.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = object.get(key);
            if (value instanceof JSONObject) {
                map.put(key, jsonObjectToMap((JSONObject) value));
            } else if (value instanceof JSONArray) {
                map.put(key, jsonArrayToList((JSONArray) value));
            } else {
                map.put(key, value);
            }
        }
        return map;
    }

    private List<Object> jsonArrayToList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONObject) {
                list.add(jsonObjectToMap((JSONObject) value));
            } else if (value instanceof JSONArray) {
                list.add(jsonArrayToList((JSONArray) value));
            } else {
                list.add(value);
            }
        }
        return list;
    }

    private UserProfile profileFromSnapshot(String uid, DocumentSnapshot snapshot, String fallbackEmail) {
        String nombre = firstNonEmpty(snapshot.getString("nombre"),
                (firstNonEmpty(snapshot.getString("nombres"), "") + " "
                        + firstNonEmpty(snapshot.getString("apellidos"), "")).trim(),
                fallbackEmail);
        String correo = firstNonEmpty(snapshot.getString("correo"), snapshot.getString("email"), fallbackEmail);
        String telefono = firstNonEmpty(snapshot.getString("telefono"), "");
        String rol = normalizeRole(firstNonEmpty(snapshot.getString("rol"), ROLE_CLIENTE));
        return new UserProfile(uid, nombre, correo, telefono, rol);
    }

    @Nullable
    private UserProfile knownDemoProfile(String email, String password) {
        String normalizedEmail = email.toLowerCase(Locale.ROOT);
        if ("superadmin@estate.pe".equals(normalizedEmail) && "super123".equals(password)) {
            return new UserProfile("", "Julian Reed", email, "", "superadmin");
        }
        if ("admin@editorialestate.com".equals(normalizedEmail) && "admin123".equals(password)) {
            return new UserProfile("", "Administrador Editorial", email, "+51 987 654 321", "admin");
        }
        if ("evaldes@editorialestate.com".equals(normalizedEmail) && "asesor123".equals(password)) {
            return new UserProfile("", "Elena Valdes", email, "+51 987 111 222", "asesor");
        }
        if ("alicia.velarde@mail.com".equals(normalizedEmail) && "cliente123".equals(password)) {
            return new UserProfile("", "Alicia Velarde", email, "+51 987 456 210", ROLE_CLIENTE);
        }
        return null;
    }

    private String normalizeRole(String role) {
        if ("user".equals(role)) {
            return ROLE_CLIENTE;
        }
        return firstNonEmpty(role, ROLE_CLIENTE);
    }

    private String currentUid() {
        FirebaseUser currentUser = auth.getCurrentUser();
        return currentUser == null ? "" : currentUser.getUid();
    }

    private Object valueOr(Object value, Object fallback) {
        if (value == null || "null".equals(String.valueOf(value))) {
            return fallback == null ? "" : fallback;
        }
        return value;
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private static String failedPreparationSource(
            Task<QuerySnapshot> typologiesTask,
            Task<QuerySnapshot> amenitiesTask,
            Task<QuerySnapshot> imagesTask,
            Task<DocumentSnapshot> projectTask,
            Task<DocumentSnapshot> userTask
    ) {
        if (!typologiesTask.isSuccessful()) {
            return "proyectos_tipologias";
        }
        if (!amenitiesTask.isSuccessful()) {
            return "proyectos_amenidades";
        }
        if (!imagesTask.isSuccessful()) {
            return "proyectos_imagenes";
        }
        if (!projectTask.isSuccessful()) {
            return "proyectos";
        }
        if (!userTask.isSuccessful()) {
            return "usuarios/" + FirebaseAuth.getInstance().getUid();
        }
        return "Firestore";
    }

    private String firstName(String fullName) {
        String[] parts = firstNonEmpty(fullName).split("\\s+", 2);
        return parts.length == 0 ? "" : parts[0];
    }

    private String lastName(String fullName) {
        String[] parts = firstNonEmpty(fullName).split("\\s+", 2);
        return parts.length < 2 ? "" : parts[1];
    }

    private String priceFromDraft(AdminProjectDraft draft) {
        if (draft == null || draft.getTypologies().isEmpty()) {
            return "Precio por definir";
        }
        String amount = firstNonEmpty(draft.getTypologies().get(0).getTotalAmount(), "Precio por definir");
        if ("Precio por definir".equals(amount)) {
            return amount;
        }
        return amount;
    }

    private String amountString(Object primary, Object secondary, String... fallbacks) {
        Object value = primary != null ? primary : secondary;
        if (value instanceof Number) {
            double number = ((Number) value).doubleValue();
            if (number == Math.rint(number)) {
                return String.valueOf((long) number);
            }
            return String.valueOf(number);
        }
        String direct = value == null ? "" : String.valueOf(value);
        if (!direct.trim().isEmpty()) {
            return direct.trim();
        }
        return firstNonEmpty(fallbacks);
    }

    private String ratingLabel(Object value) {
        String rating = value == null ? "" : String.valueOf(value).trim();
        if (rating.isEmpty()) {
            return "0/5";
        }
        return rating.contains("/") ? rating : rating + "/5";
    }

    private String objectString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private ProjectDetail projectDetailFromSnapshot(DocumentSnapshot project) {
        String projectId = project.getId();
        String status = ProjectBusinessRules.normalizeStatus(firstNonEmpty(
                project.getString("estadoProyecto"),
                project.getString("estadoComercial"),
                project.getString("estado")
        ));
        String deliveryDate = firstNonEmpty(project.getString("fechaEntregaEstimada"), project.getString("fechaEntrega"));
        long deliveryMillis = longValue(project.get("fechaEntregaMillis"));
        if (deliveryMillis == 0L) {
            deliveryMillis = ProjectBusinessRules.deliveryMillisFromDisplay(deliveryDate);
        }
        return new ProjectDetail(
                projectId,
                firstNonEmpty(project.getString("nombre"), "Proyecto sin nombre"),
                firstNonEmpty(project.getString("descripcion"), ""),
                firstNonEmpty(project.getString("direccion"), ""),
                firstNonEmpty(project.getString("distrito"), ""),
                firstNonEmpty(project.getString("precioDesde"), "Precio por definir"),
                status,
                deliveryDate,
                firstNonEmpty(project.getString("fechaEntregaISO"), ProjectBusinessRules.deliveryIsoFromDisplay(deliveryDate)),
                deliveryMillis,
                ProjectBusinessRules.qrValue(projectId),
                firstNonEmpty(project.getString("primaryImageUrl"), project.getString("imageUrl")),
                doubleValue(project.get("lat"), -12.0464),
                doubleValue(project.get("lng"), -77.0428)
        );
    }

    private Map<String, Object> locationMap(AdminProjectDraft draft) {
        Map<String, Object> location = new HashMap<>();
        location.put("direccion", draft.getAddress());
        location.put("distrito", draft.getCity());
        location.put("lat", draft.getLatitude());
        location.put("lng", draft.getLongitude());
        return location;
    }

    private String defaultBadge(String status) {
        return ProjectBusinessRules.displayStatus(status);
    }

    private String displayStatus(DocumentSnapshot project) {
        return ProjectBusinessRules.displayStatus(firstNonEmpty(
                project.getString("estadoProyecto"),
                project.getString("estadoComercial"),
                project.getString("estado"),
                "EN PLANOS"
        ));
    }

    private int fallbackImageRes(String key) {
        int res = imageRes(key);
        return res == 0 ? R.drawable.user_featured_house : res;
    }

    private int imageRes(String key) {
        if ("sa_profile_admin".equals(key)) return R.drawable.sa_profile_admin;
        if ("user_popular_1".equals(key)) return R.drawable.user_popular_1;
        if ("user_popular_2".equals(key)) return R.drawable.user_popular_2;
        if ("user_property_hero_real".equals(key)) return R.drawable.user_property_hero_real;
        if ("user_featured_house".equals(key)) return R.drawable.user_featured_house;
        return 0;
    }

    private String slug(String value) {
        return firstNonEmpty(value)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
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

    private double doubleValue(Object value, double fallback) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (Exception ignored) {}
        }
        return fallback;
    }

    private int intValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (Exception ignored) {
                return 0;
            }
        }
        return 0;
    }

    private static String safeMessage(Exception error) {
        if (error instanceof FirebaseAuthException) {
            String code = ((FirebaseAuthException) error).getErrorCode();
            if ("ERROR_WEAK_PASSWORD".equals(code)) {
                return "la contrasena debe tener minimo 6 caracteres";
            }
            if ("ERROR_EMAIL_ALREADY_IN_USE".equals(code)) {
                return "ese correo ya esta registrado, inicia sesion";
            }
            if ("ERROR_INVALID_EMAIL".equals(code)) {
                return "el correo ingresado no es valido";
            }
            if ("ERROR_OPERATION_NOT_ALLOWED".equals(code)) {
                return "activa Email/Password en Firebase Authentication";
            }
            if ("ERROR_INTERNAL_ERROR".equals(code)) {
                return "error interno de Firebase; revisa que google-services.json pertenezca al paquete com.example.proyecto_iot y que Email/Password este activado";
            }
        }
        String message = error.getMessage();
        return message == null || message.trim().isEmpty() ? "error sin detalle" : message;
    }
}
