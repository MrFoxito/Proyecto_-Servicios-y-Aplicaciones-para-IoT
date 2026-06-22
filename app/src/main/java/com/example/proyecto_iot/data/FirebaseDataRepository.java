package com.example.proyecto_iot.data;

import android.content.Context;

import androidx.annotation.Nullable;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.model.AdminProjectDraft;
import com.example.proyecto_iot.admin.model.AdminProjectFormAmenityItem;
import com.example.proyecto_iot.admin.model.AdminProjectFormTypologyItem;
import com.example.proyecto_iot.admin.model.AdminProjectItem;
import com.example.proyecto_iot.usuario.UsuarioPropertyListItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

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

    public interface AdminProjectsCallback {
        void onSuccess(List<AdminProjectItem> projects);
        void onError(String message);
    }

    public interface UserPropertyListCallback {
        void onSuccess(List<UsuarioPropertyListItem> projects);
        void onError(String message);
    }

    public interface ProjectDetailCallback {
        void onSuccess(ProjectDetail detail);
        void onError(String message);
    }

    public interface ProjectImagesCallback {
        void onSuccess(List<String> imageUrls);
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

        public UserProfile(String uid, String nombre, String correo, String telefono, String rol) {
            this.uid = uid;
            this.nombre = nombre;
            this.correo = correo;
            this.telefono = telefono;
            this.rol = rol;
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
        SupabaseStorageRepository.UploadResult primaryImage =
                images == null || images.isEmpty() ? null : images.get(0);

        WriteBatch batch = firestore.batch();
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
        project.put("badge", estadoProyectoLabel);
        project.put("lat", draft.getLatitude());
        project.put("lng", draft.getLongitude());
        project.put("ubicacion", locationMap(draft));
        project.put("puntosInteres", nearbyPointsFor(draft));
        project.put("qrValue", qrValue);
        project.put("deepLink", qrValue);
        if (primaryImage != null) {
            project.put("imageUrl", primaryImage.publicUrl);
            project.put("primaryImageUrl", primaryImage.publicUrl);
            project.put("imageStoragePath", primaryImage.storagePath);
            project.put("imageProvider", "supabase");
        } else {
            project.put("imageKey", "sa_profile_admin");
            project.put("userImageKey", "user_featured_house");
        }
        project.put("fechaEntrega", draft.getDeliveryDate());
        project.put("fechaEntregaEstimada", draft.getDeliveryDate());
        project.put("fechaEntregaISO", fechaEntregaISO);
        project.put("fechaEntregaMillis", fechaEntregaMillis);
        project.put("deliveryReminderSent", false);
        project.put("assignmentStatus", "ACTIVO");
        project.put("createdAt", System.currentTimeMillis());
        project.put("updatedAt", System.currentTimeMillis());
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

    public void readUserPropertyListItems(UserPropertyListCallback callback) {
        firestore.collection("proyectos").get()
                .addOnSuccessListener(snapshot -> {
                    List<UsuarioPropertyListItem> items = new ArrayList<>();
                    for (DocumentSnapshot project : snapshot.getDocuments()) {
                        String imageKey = firstNonEmpty(project.getString("userImageKey"), project.getString("imageKey"), "user_featured_house");
                        items.add(new UsuarioPropertyListItem(
                                firstNonEmpty(project.getString("propertyId"), project.getString("projectId"), project.getId()),
                                firstNonEmpty(project.getString("badge"), displayStatus(project), "PROYECTO"),
                                firstNonEmpty(project.getString("nombre"), "Proyecto sin nombre"),
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
                                firstNonEmpty(project.getString("qrValue"), ProjectBusinessRules.qrValue(project.getId()))
                        ));
                    }
                    callback.onSuccess(items);
                })
                .addOnFailureListener(error ->
                        callback.onError("No se pudo leer inmuebles desde Firestore: " + safeMessage(error)));
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

    public void readProjectImages(String projectId, ProjectImagesCallback callback) {
        if (projectId == null || projectId.trim().isEmpty()) {
            callback.onError("ID de proyecto invalido.");
            return;
        }
        firestore.collection("proyectos_imagenes")
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<DocumentSnapshot> docs = new ArrayList<>(snapshot.getDocuments());
                    // Sort locally to avoid needing a composite index in Firestore
                    docs.sort((d1, d2) -> {
                        Long t1 = d1.getLong("createdAt");
                        Long t2 = d2.getLong("createdAt");
                        if (t1 == null) t1 = 0L;
                        if (t2 == null) t2 = 0L;
                        return t1.compareTo(t2);
                    });

                    List<String> images = new ArrayList<>();
                    for (DocumentSnapshot doc : docs) {
                        String url = doc.getString("imageUrl");
                        if (url != null && !url.isEmpty()) {
                            images.add(url);
                        }
                    }
                    callback.onSuccess(images);
                })
                .addOnFailureListener(error ->
                        callback.onError("Error al obtener imagenes: " + safeMessage(error)));
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
            map.put("estadoProyecto", ProjectBusinessRules.normalizeStatus(String.valueOf(valueOr(map.get("estadoProyecto"), map.get("estado")))));
            map.put("estadoProyectoLabel", ProjectBusinessRules.displayStatus(String.valueOf(valueOr(map.get("estadoProyecto"), map.get("estado")))));
            map.put("fechaEntrega", valueOr(map.get("fechaEntrega"), ""));
            map.put("fechaEntregaEstimada", valueOr(map.get("fechaEntregaEstimada"), map.get("fechaEntrega")));
            map.put("fechaEntregaISO", ProjectBusinessRules.deliveryIsoFromDisplay(String.valueOf(valueOr(map.get("fechaEntrega"), ""))));
            map.put("fechaEntregaMillis", ProjectBusinessRules.deliveryMillisFromDisplay(String.valueOf(valueOr(map.get("fechaEntrega"), ""))));
            map.put("qrValue", ProjectBusinessRules.qrValue(String.valueOf(valueOr(map.get("projectId"), docId))));
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

    private ProjectDetail projectDetailFromSnapshot(DocumentSnapshot project) {
        String projectId = firstNonEmpty(project.getString("propertyId"), project.getString("projectId"), project.getId());
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
                firstNonEmpty(project.getString("qrValue"), ProjectBusinessRules.qrValue(projectId)),
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

    private List<Map<String, Object>> nearbyPointsFor(AdminProjectDraft draft) {
        List<Map<String, Object>> points = new ArrayList<>();
        points.add(pointOfInterest("Parque cercano", "parque", draft.getLatitude() + 0.002, draft.getLongitude() + 0.001));
        points.add(pointOfInterest("Centro comercial", "comercio", draft.getLatitude() - 0.001, draft.getLongitude() + 0.002));
        points.add(pointOfInterest("Estacion de transporte", "transporte", draft.getLatitude() + 0.001, draft.getLongitude() - 0.002));
        return points;
    }

    private Map<String, Object> pointOfInterest(String name, String type, double lat, double lng) {
        Map<String, Object> point = new HashMap<>();
        point.put("nombre", name);
        point.put("tipo", type);
        point.put("lat", lat);
        point.put("lng", lng);
        return point;
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
