package com.example.proyecto_iot.data;

import android.content.Context;

import androidx.annotation.Nullable;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.model.AdminProjectAmenityItem;
import com.example.proyecto_iot.admin.model.AdminProjectDraft;
import com.example.proyecto_iot.admin.model.AdminProjectFormAmenityItem;
import com.example.proyecto_iot.admin.model.AdminProjectFormTypologyItem;
import com.example.proyecto_iot.admin.model.AdminProjectGalleryItem;
import com.example.proyecto_iot.admin.model.AdminProjectItem;
import com.example.proyecto_iot.admin.model.AdminProjectTypologyItem;
import com.example.proyecto_iot.usuario.UsuarioPropertyListItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    public interface AdminProjectsCallback {
        void onSuccess(List<AdminProjectItem> projects);
        void onError(String message);
    }

    public interface AdminProjectDetailCallback {
        void onSuccess(AdminProjectDetail detail);
        void onError(String message);
    }

    public interface UserPropertyListCallback {
        void onSuccess(List<UsuarioPropertyListItem> projects);
        void onError(String message);
    }

    public static class UserProfile {
        public final String uid;
        public final String nombre;
        public final String correo;
        public final String telefono;
        public final String rol;

        public UserProfile(String uid, String nombre, String correo, String telefono, String rol) {
            this.uid = uid;
            this.nombre = nombre;
            this.correo = correo;
            this.telefono = telefono;
            this.rol = rol;
        }
    }

    public static class AdminProjectDetail {
        public final String projectId;
        public final String title;
        public final String description;
        public final String location;
        public final String status;
        public final String deliveryDate;
        public final List<AdminProjectGalleryItem> gallery;
        public final List<AdminProjectTypologyItem> typologies;
        public final List<AdminProjectAmenityItem> amenities;

        public AdminProjectDetail(
                String projectId,
                String title,
                String description,
                String location,
                String status,
                String deliveryDate,
                List<AdminProjectGalleryItem> gallery,
                List<AdminProjectTypologyItem> typologies,
                List<AdminProjectAmenityItem> amenities
        ) {
            this.projectId = projectId;
            this.title = title;
            this.description = description;
            this.location = location;
            this.status = status;
            this.deliveryDate = deliveryDate;
            this.gallery = gallery;
            this.typologies = typologies;
            this.amenities = amenities;
        }
    }

    private static class UserProjectRelations {
        final Map<String, List<DocumentSnapshot>> typologies = new HashMap<>();
        final Map<String, List<DocumentSnapshot>> amenities = new HashMap<>();
        final Map<String, List<DocumentSnapshot>> images = new HashMap<>();
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

    public void seedLocalSnapshotIfNeeded(Context context) {
        firestore.collection(COLLECTION_META).document(META_SCHEMA_DOC).get()
                .addOnSuccessListener(snapshot -> {
                    if (SCHEMA_VERSION.equals(snapshot.getString("version"))) {
                        return;
                    }
                    uploadLocalSnapshot(context);
                });
    }

    public String projectIdForDraft(AdminProjectDraft draft, @Nullable String originalProjectTitle) {
        String projectId = slug(originalProjectTitle == null || originalProjectTitle.trim().isEmpty()
                ? draft.getProjectName()
                : originalProjectTitle);
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
        boolean editingExistingProject = originalProjectTitle != null && !originalProjectTitle.trim().isEmpty();
        SupabaseStorageRepository.UploadResult primaryImage =
                images == null || images.isEmpty() ? null : images.get(0);
        if (primaryImage == null && !editingExistingProject) {
            callback.onError("Selecciona imagenes reales para subir a Supabase antes de publicar");
            return;
        }

        WriteBatch batch = firestore.batch();
        Map<String, Object> project = new HashMap<>();
        project.put("id", projectId);
        project.put("projectId", projectId);
        project.put("propertyId", projectId);
        project.put("adminId", currentUid());
        project.put("nombre", draft.getProjectName());
        project.put("descripcion", draft.getDescription());
        project.put("direccion", draft.getAddress());
        project.put("distrito", draft.getCity());
        project.put("mapa", draft.getMapLabel());
        project.put("estado", draft.getStatus());
        project.put("estadoComercial", draft.getStatus());
        project.put("precioDesde", priceFromDraft(draft));
        project.put("badge", defaultBadge(draft.getStatus()));
        if (primaryImage != null) {
            project.put("imageUrl", primaryImage.publicUrl);
            project.put("primaryImageUrl", primaryImage.publicUrl);
            project.put("imageStoragePath", primaryImage.storagePath);
            project.put("imageProvider", "supabase");
        } else if (!editingExistingProject) {
            project.put("imageKey", "sa_profile_admin");
            project.put("userImageKey", "user_featured_house");
        }
        project.put("fechaEntrega", draft.getDeliveryDate());
        project.put("assignmentStatus", "ACTIVO");
        if (editingExistingProject) {
            project.put("updatedAt", System.currentTimeMillis());
        } else {
            project.put("createdAt", System.currentTimeMillis());
        }
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
            typology.put("montoTotal", item.getTotalAmount());
            typology.put("totalAmount", item.getTotalAmount());
            typology.put("montoSeparacion", item.getSeparationAmount());
            typology.put("separationAmount", item.getSeparationAmount());
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
            amenity.put("icono", String.valueOf(item.getIconRes()));
            amenity.put("selected", item.isSelected());
            batch.set(firestore.collection("proyectos_amenidades").document(id), amenity, SetOptions.merge());
        }

        if (images != null) {
            for (int i = 0; i < images.size(); i++) {
                SupabaseStorageRepository.UploadResult image = images.get(i);
                String id = projectId + "_supabase_" + System.currentTimeMillis() + "_" + i;
                Map<String, Object> data = new HashMap<>();
                data.put("id", id);
                data.put("imageId", id);
                data.put("projectId", projectId);
                data.put("imageUrl", image.publicUrl);
                data.put("storagePath", image.storagePath);
                data.put("provider", "supabase");
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
        firestore.collection("proyectos").get()
                .addOnSuccessListener(snapshot -> {
                    List<AdminProjectItem> items = new ArrayList<>();
                    for (DocumentSnapshot project : snapshot.getDocuments()) {
                        String status = displayStatus(project);
                        items.add(new AdminProjectItem(
                                firstNonEmpty(project.getString("projectId"), project.getString("propertyId"), project.getId()),
                                firstNonEmpty(project.getString("nombre"), "Proyecto sin nombre"),
                                firstNonEmpty(project.getString("direccion"), project.getString("distrito"), "Ubicacion pendiente"),
                                firstNonEmpty(project.getString("precioDesde"), "Precio por definir"),
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

    public void readAdminProjectDetail(String projectId, String fallbackTitle, AdminProjectDetailCallback callback) {
        String normalizedId = firstNonEmpty(projectId);
        if (!normalizedId.isEmpty()) {
            firestore.collection("proyectos").document(normalizedId).get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            readAdminProjectRelations(snapshot, callback);
                        } else {
                            readAdminProjectDetailByTitle(fallbackTitle, callback);
                        }
                    })
                    .addOnFailureListener(error -> readAdminProjectDetailByTitle(fallbackTitle, callback));
            return;
        }
        readAdminProjectDetailByTitle(fallbackTitle, callback);
    }

    private void readAdminProjectDetailByTitle(String title, AdminProjectDetailCallback callback) {
        if (firstNonEmpty(title).isEmpty()) {
            callback.onError("No se recibio proyecto para mostrar");
            return;
        }
        firestore.collection("proyectos")
                .whereEqualTo("nombre", title)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        callback.onError("No se encontro el proyecto en Firestore");
                        return;
                    }
                    readAdminProjectRelations(snapshot.getDocuments().get(0), callback);
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudo leer detalle del proyecto desde Firestore: " + safeMessage(error)));
    }

    private void readAdminProjectRelations(DocumentSnapshot project, AdminProjectDetailCallback callback) {
        String projectId = firstNonEmpty(project.getString("projectId"), project.getString("propertyId"), project.getId());
        firestore.collection("proyectos_imagenes")
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnSuccessListener(images ->
                        firestore.collection("proyectos_tipologias")
                                .whereEqualTo("projectId", projectId)
                                .get()
                                .addOnSuccessListener(typologies ->
                                        firestore.collection("proyectos_amenidades")
                                                .whereEqualTo("projectId", projectId)
                                                .get()
                                                .addOnSuccessListener(amenities ->
                                                        callback.onSuccess(adminProjectDetailFromSnapshot(
                                                                project,
                                                                images.getDocuments(),
                                                                typologies.getDocuments(),
                                                                amenities.getDocuments()
                                                        )))
                                                .addOnFailureListener(error ->
                                                        callback.onError("No se pudieron leer amenidades del proyecto: " + safeMessage(error))))
                                .addOnFailureListener(error ->
                                        callback.onError("No se pudieron leer tipologias del proyecto: " + safeMessage(error))))
                .addOnFailureListener(error ->
                        callback.onError("No se pudieron leer imagenes del proyecto: " + safeMessage(error)));
    }

    private AdminProjectDetail adminProjectDetailFromSnapshot(
            DocumentSnapshot project,
            List<DocumentSnapshot> imageDocs,
            List<DocumentSnapshot> typologyDocs,
            List<DocumentSnapshot> amenityDocs
    ) {
        String projectId = firstNonEmpty(project.getString("projectId"), project.getString("propertyId"), project.getId());
        List<AdminProjectGalleryItem> gallery = new ArrayList<>();
        String primaryUrl = firstNonEmpty(project.getString("primaryImageUrl"), project.getString("imageUrl"));
        if (!primaryUrl.isEmpty()) {
            gallery.add(new AdminProjectGalleryItem(0, primaryUrl));
        }
        for (DocumentSnapshot image : imageDocs) {
            String imageUrl = firstNonEmpty(image.getString("imageUrl"), image.getString("publicUrl"));
            if (!imageUrl.isEmpty() && !containsGalleryUrl(gallery, imageUrl)) {
                gallery.add(new AdminProjectGalleryItem(0, imageUrl));
            } else if (imageUrl.isEmpty()) {
                String imageKey = firstNonEmpty(image.getString("imageKey"));
                int imageRes = imageRes(imageKey);
                if (imageRes != 0) {
                    gallery.add(new AdminProjectGalleryItem(imageRes));
                }
            }
        }
        if (gallery.isEmpty()) {
            int imageRes = imageRes(firstNonEmpty(project.getString("imageKey"), "sa_profile_admin"));
            gallery.add(new AdminProjectGalleryItem(imageRes == 0 ? R.drawable.sa_profile_admin : imageRes));
        }

        List<AdminProjectTypologyItem> typologies = new ArrayList<>();
        for (DocumentSnapshot typology : typologyDocs) {
            boolean available = typology.getBoolean("available") == null || Boolean.TRUE.equals(typology.getBoolean("available"));
            typologies.add(new AdminProjectTypologyItem(
                    firstNonEmpty(typology.getString("nombre"), typology.getString("title"), "Tipologia"),
                    available ? "DISPONIBLE" : "NO DISPONIBLE",
                    available,
                    firstNonEmpty(typology.getString("area"), "Area por definir"),
                    firstNonEmpty(typology.getString("habitaciones"), typology.getString("bedrooms"), "Habitaciones por definir"),
                    firstNonEmpty(typology.getString("banos"), typology.getString("bathrooms"), "Banos por definir"),
                    firstNonEmpty(typology.getString("montoTotal"), typology.getString("totalAmount"), "Precio por definir"),
                    firstNonEmpty(typology.getString("montoSeparacion"), typology.getString("separationAmount"), "Separacion por definir")
            ));
        }

        List<AdminProjectAmenityItem> amenities = new ArrayList<>();
        for (DocumentSnapshot amenity : amenityDocs) {
            String title = firstNonEmpty(amenity.getString("nombre"), amenity.getString("title"), "Amenidad");
            amenities.add(new AdminProjectAmenityItem(title, adminAmenityIcon(firstNonEmpty(amenity.getString("icono"), title))));
        }

        return new AdminProjectDetail(
                projectId,
                firstNonEmpty(project.getString("nombre"), "Proyecto sin nombre"),
                firstNonEmpty(project.getString("descripcion"), "Descripcion pendiente."),
                firstNonEmpty(project.getString("direccion"), project.getString("distrito"), "Ubicacion pendiente"),
                displayStatus(project),
                firstNonEmpty(project.getString("fechaEntrega"), "Fecha por definir"),
                gallery,
                typologies,
                amenities
        );
    }

    private boolean containsGalleryUrl(List<AdminProjectGalleryItem> gallery, String imageUrl) {
        for (AdminProjectGalleryItem item : gallery) {
            if (imageUrl.equals(item.getImageUrl())) {
                return true;
            }
        }
        return false;
    }

    private int adminAmenityIcon(String value) {
        String normalized = firstNonEmpty(value).toLowerCase(Locale.ROOT);
        try {
            if (!normalized.isEmpty() && normalized.matches("\\d+")) {
                return Integer.parseInt(normalized);
            }
        } catch (NumberFormatException ignored) {
            // Falls back to semantic icon mapping.
        }
        if (normalized.contains("cowork")) return R.drawable.ic_admin_laptop;
        if (normalized.contains("pisc")) return R.drawable.ic_admin_pool;
        if (normalized.contains("gim")) return R.drawable.ic_amenity_gym;
        if (normalized.contains("bbq") || normalized.contains("parr")) return R.drawable.ic_amenity_bbq;
        if (normalized.contains("pet")) return R.drawable.ic_amenity_pet;
        if (normalized.contains("seguridad")) return R.drawable.ic_amenity_security;
        if (normalized.contains("estacion")) return R.drawable.ic_amenity_parking;
        if (normalized.contains("bici")) return R.drawable.ic_amenity_bike;
        if (normalized.contains("terraza")) return R.drawable.ic_amenity_terrace;
        if (normalized.contains("juegos")) return R.drawable.ic_amenity_playground;
        if (normalized.contains("lobby") || normalized.contains("lounge")) return R.drawable.ic_amenity_lobby;
        return R.drawable.ic_home;
    }

    public void readUserPropertyListItems(UserPropertyListCallback callback) {
        firestore.collection("proyectos").get()
                .addOnSuccessListener(projectSnapshot ->
                        firestore.collection("proyectos_tipologias").get()
                                .addOnSuccessListener(typologySnapshot ->
                                        firestore.collection("proyectos_amenidades").get()
                                                .addOnSuccessListener(amenitySnapshot ->
                                                        firestore.collection("proyectos_imagenes").get()
                                                                .addOnSuccessListener(imageSnapshot -> {
                                                                    UserProjectRelations relations = new UserProjectRelations();
                                                                    groupByProjectId(relations.typologies, typologySnapshot.getDocuments());
                                                                    groupByProjectId(relations.amenities, amenitySnapshot.getDocuments());
                                                                    groupByProjectId(relations.images, imageSnapshot.getDocuments());

                                                                    List<DocumentSnapshot> projects = new ArrayList<>(projectSnapshot.getDocuments());
                                                                    Collections.sort(projects, projectComparator());

                                                                    List<UsuarioPropertyListItem> items = new ArrayList<>();
                                                                    for (DocumentSnapshot project : projects) {
                                                                        if (!isSellableProject(project)) {
                                                                            continue;
                                                                        }
                                                                        items.add(userProjectFromSnapshot(project, relations));
                                                                    }
                                                                    callback.onSuccess(items);
                                                                })
                                                                .addOnFailureListener(error ->
                                                                        callback.onError("No se pudieron leer imagenes de inmuebles desde Firestore: " + safeMessage(error))))
                                                .addOnFailureListener(error ->
                                                        callback.onError("No se pudieron leer amenidades de inmuebles desde Firestore: " + safeMessage(error))))
                                .addOnFailureListener(error ->
                                        callback.onError("No se pudieron leer tipologias de inmuebles desde Firestore: " + safeMessage(error))))
                .addOnFailureListener(error ->
                        callback.onError("No se pudo leer inmuebles desde Firestore: " + safeMessage(error)));
    }

    private void groupByProjectId(Map<String, List<DocumentSnapshot>> target, List<DocumentSnapshot> documents) {
        for (DocumentSnapshot document : documents) {
            String projectId = firstNonEmpty(document.getString("projectId"), document.getString("propertyId"));
            if (projectId.isEmpty()) {
                continue;
            }
            List<DocumentSnapshot> rows = target.get(projectId);
            if (rows == null) {
                rows = new ArrayList<>();
                target.put(projectId, rows);
            }
            rows.add(document);
        }
    }

    private Comparator<DocumentSnapshot> projectComparator() {
        return (left, right) -> {
            long rightTime = timestampForSort(right);
            long leftTime = timestampForSort(left);
            if (rightTime != leftTime) {
                return Long.compare(rightTime, leftTime);
            }
            return firstNonEmpty(left.getString("nombre"), left.getId())
                    .compareToIgnoreCase(firstNonEmpty(right.getString("nombre"), right.getId()));
        };
    }

    private long timestampForSort(DocumentSnapshot snapshot) {
        Long updatedAt = snapshot.getLong("updatedAt");
        Long createdAt = snapshot.getLong("createdAt");
        if (updatedAt != null) {
            return updatedAt;
        }
        if (createdAt != null) {
            return createdAt;
        }
        return 0L;
    }

    private boolean isSellableProject(DocumentSnapshot project) {
        String assignment = firstNonEmpty(project.getString("assignmentStatus"), "ACTIVO").toUpperCase(Locale.ROOT);
        if (!"ACTIVO".equals(assignment)) {
            return false;
        }
        String status = displayStatus(project);
        return !status.isEmpty()
                && (status.contains("VENTA")
                || status.contains("PREVENTA")
                || status.contains("PLANO")
                || status.contains("ACTIVO")
                || status.contains("PROYECTO"));
    }

    private UsuarioPropertyListItem userProjectFromSnapshot(
            DocumentSnapshot project,
            UserProjectRelations relations
    ) {
        String projectId = firstNonEmpty(project.getString("projectId"), project.getString("propertyId"), project.getId());
        String propertyId = firstNonEmpty(project.getString("propertyId"), projectId, project.getId());
        List<DocumentSnapshot> typologies = relations.typologies.containsKey(projectId)
                ? relations.typologies.get(projectId)
                : new ArrayList<>();
        List<DocumentSnapshot> amenities = relations.amenities.containsKey(projectId)
                ? relations.amenities.get(projectId)
                : new ArrayList<>();
        List<DocumentSnapshot> images = relations.images.containsKey(projectId)
                ? relations.images.get(projectId)
                : new ArrayList<>();

        DocumentSnapshot firstTypology = typologies.isEmpty() ? null : typologies.get(0);
        String status = displayStatus(project);
        String imageKey = firstNonEmpty(project.getString("userImageKey"), project.getString("imageKey"), "user_featured_house");
        String location = firstNonEmpty(project.getString("direccion"), project.getString("distrito"), "Ubicacion pendiente");
        String district = firstNonEmpty(project.getString("distrito"), location);

        return new UsuarioPropertyListItem(
                propertyId,
                firstNonEmpty(project.getString("badge"), status, "PROYECTO"),
                firstNonEmpty(project.getString("nombre"), "Proyecto sin nombre"),
                location,
                firstNonEmpty(project.getString("precioDesde"), "Precio por definir"),
                fallbackImageRes(imageKey),
                firstNonEmpty(project.getString("primaryImageUrl"), project.getString("imageUrl"), firstImageUrl(images)),
                firstNonEmpty(project.getString("descripcion"), "Informacion del inmueble en actualizacion."),
                district,
                firstNonEmpty(project.getString("fechaEntrega"), "Fecha por definir"),
                firstNonEmpty(project.getString("mapa"), project.getString("mapLabel"), ""),
                firstTypology == null ? "" : firstNonEmpty(firstTypology.getString("habitaciones"), firstTypology.getString("bedrooms")),
                firstTypology == null ? "" : firstNonEmpty(firstTypology.getString("banos"), firstTypology.getString("bathrooms")),
                firstTypology == null ? "" : firstNonEmpty(firstTypology.getString("area"), ""),
                typologiesSummary(typologies),
                amenitiesSummary(amenities),
                status
        );
    }

    private String firstImageUrl(List<DocumentSnapshot> images) {
        for (DocumentSnapshot image : images) {
            String url = firstNonEmpty(image.getString("imageUrl"), image.getString("publicUrl"));
            if (!url.isEmpty()) {
                return url;
            }
        }
        return "";
    }

    private String typologiesSummary(List<DocumentSnapshot> typologies) {
        if (typologies == null || typologies.isEmpty()) {
            return "Tipologia por definir";
        }
        DocumentSnapshot first = typologies.get(0);
        String title = firstNonEmpty(first.getString("nombre"), first.getString("title"), "Tipologia");
        String bedrooms = firstNonEmpty(first.getString("habitaciones"), first.getString("bedrooms"));
        String bathrooms = firstNonEmpty(first.getString("banos"), first.getString("bathrooms"));
        String area = firstNonEmpty(first.getString("area"));
        List<String> parts = new ArrayList<>();
        parts.add(title);
        if (!bedrooms.isEmpty()) parts.add(bedrooms);
        if (!bathrooms.isEmpty()) parts.add(bathrooms);
        if (!area.isEmpty()) parts.add(area);
        if (typologies.size() > 1) {
            parts.add("+" + (typologies.size() - 1) + " mas");
        }
        return join(parts, " · ");
    }

    private String amenitiesSummary(List<DocumentSnapshot> amenities) {
        if (amenities == null || amenities.isEmpty()) {
            return "Amenidades por definir";
        }
        List<String> names = new ArrayList<>();
        for (DocumentSnapshot amenity : amenities) {
            String name = firstNonEmpty(amenity.getString("nombre"), amenity.getString("title"));
            if (!name.isEmpty()) {
                names.add(name);
            }
            if (names.size() == 4) {
                break;
            }
        }
        return names.isEmpty() ? "Amenidades por definir" : join(names, " · ");
    }

    private String join(List<String> values, String separator) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(separator);
            }
            builder.append(value.trim());
        }
        return builder.toString();
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
            String id = projectId + "_supabase_" + System.currentTimeMillis() + "_" + i;
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("imageId", id);
            data.put("projectId", projectId);
            data.put("imageUrl", image.publicUrl);
            data.put("storagePath", image.storagePath);
            data.put("provider", "supabase");
            data.put("createdAt", System.currentTimeMillis());
            batch.set(firestore.collection("proyectos_imagenes").document(id), data, SetOptions.merge());
        }
        SupabaseStorageRepository.UploadResult primaryImage = images.get(0);
        Map<String, Object> projectImageData = new HashMap<>();
        projectImageData.put("imageUrl", primaryImage.publicUrl);
        projectImageData.put("primaryImageUrl", primaryImage.publicUrl);
        projectImageData.put("imageStoragePath", primaryImage.storagePath);
        projectImageData.put("imageProvider", "supabase");
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
        data.put("avatarProvider", "supabase");
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
        Map<String, Object> data = new HashMap<>();
        data.put("adminId", uid);
        data.put(slot == 0 ? "companyImageUrl" : "companySecondaryImageUrl", image.publicUrl);
        data.put(slot == 0 ? "companyImageStoragePath" : "companySecondaryImageStoragePath", image.storagePath);
        data.put("provider", "supabase");
        data.put("updatedAt", System.currentTimeMillis());
        firestore.collection("empresas").document(uid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(error ->
                        callback.onError("No se pudo guardar imagen de empresa en Firestore: "
                                + safeMessage(error)));
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
            map.put("projectId", valueOr(map.get("projectId"), docId));
            map.put("estado", valueOr(map.get("estado"), map.get("estadoComercial")));
            map.put("fechaEntrega", valueOr(map.get("fechaEntrega"), ""));
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
        return amount.toUpperCase(Locale.ROOT).contains("USD") ? amount : amount + " USD";
    }

    private String defaultBadge(String status) {
        String normalized = firstNonEmpty(status, "PROYECTO").toUpperCase(Locale.ROOT);
        if (normalized.contains("PREVENTA")) {
            return "EN PREVENTA";
        }
        if (normalized.contains("PLAN")) {
            return "EN PLANOS";
        }
        if (normalized.contains("VENTA")) {
            return "EN VENTA";
        }
        return normalized;
    }

    private String displayStatus(DocumentSnapshot project) {
        String normalized = firstNonEmpty(
                project.getString("estadoComercial"),
                project.getString("estado"),
                "EN PLANOS"
        ).toUpperCase(Locale.ROOT);
        if (normalized.contains("PREVENTA")) {
            return "EN PREVENTA";
        }
        if (normalized.contains("VENTA")) {
            return "EN VENTA";
        }
        if (normalized.contains("PLANO")) {
            return "EN PLANOS";
        }
        return normalized;
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
