package com.example.proyecto_iot.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.core.content.ContextCompat;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.model.AdminAdvisorItem;
import com.example.proyecto_iot.admin.model.AdminAssignableProjectItem;
import com.example.proyecto_iot.admin.model.AdminAssignedProjectItem;
import com.example.proyecto_iot.admin.model.AdminNotificationItem;
import com.example.proyecto_iot.admin.model.AdminProjectAmenityItem;
import com.example.proyecto_iot.admin.model.AdminProjectDraft;
import com.example.proyecto_iot.admin.model.AdminProjectFormAmenityItem;
import com.example.proyecto_iot.admin.model.AdminProjectFormTypologyItem;
import com.example.proyecto_iot.admin.model.AdminProjectGalleryItem;
import com.example.proyecto_iot.admin.model.AdminProjectItem;
import com.example.proyecto_iot.admin.model.AdminProjectTypologyItem;
import com.example.proyecto_iot.admin.model.AdminProjectVisualItem;
import com.example.proyecto_iot.admin.model.AdminRequestItem;
import com.example.proyecto_iot.admin.model.AdminReviewItem;
import com.example.proyecto_iot.entity.Chat;
import com.example.proyecto_iot.entity.Cita;
import com.example.proyecto_iot.entity.EventoCita;
import com.example.proyecto_iot.entity.MensajeChat;
import com.example.proyecto_iot.entity.Separacion;
import com.example.proyecto_iot.superadmin.SuperadminControlAccesoItem;
import com.example.proyecto_iot.superadmin.SuperadminGestionUsuarioItem;
import com.example.proyecto_iot.superadmin.SuperadminLogEntryItem;
import com.example.proyecto_iot.superadmin.SuperadminResumenLogItem;
import com.example.proyecto_iot.superadmin.SuperadminSolicitudAsesorItem;
import com.example.proyecto_iot.usuario.UsuarioAppointmentItem;
import com.example.proyecto_iot.usuario.UsuarioChatListItem;
import com.example.proyecto_iot.usuario.UsuarioHistoryItem;
import com.example.proyecto_iot.usuario.UsuarioNotificationItem;
import com.example.proyecto_iot.usuario.UsuarioPropertyCatalog;
import com.example.proyecto_iot.usuario.UsuarioPropertyListItem;
import com.example.proyecto_iot.usuario.UsuarioTramiteItem;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LocalSchemaStorage {
    private static final String PREFS_NAME = "iot_local_schema_storage";
    //private static final String KEY_INITIALIZED = "initialized_v5"; // Incrementado para resetear datos con nueva estructura
    private static final String KEY_INITIALIZED = "initialized_v4";
    private static final long FIRESTORE_TIMEOUT_SECONDS = 3;

    private static final String COLLECTION_USUARIOS = "usuarios";
    private static final String COLLECTION_PROYECTOS = "proyectos";
    private static final String COLLECTION_TIPOLOGIAS = "proyectos_tipologias";
    private static final String COLLECTION_AMENIDADES = "proyectos_amenidades";
    private static final String COLLECTION_IMAGENES = "proyectos_imagenes";
    private static final String COLLECTION_SOLICITUDES = "solicitudes_asesor";
    private static final String COLLECTION_CITAS = "citas";
    private static final String COLLECTION_EVENTOS_CITA = "eventos_cita";
    private static final String COLLECTION_SEPARACIONES = "separaciones";
    private static final String COLLECTION_CONVERSACIONES = "conversaciones";
    private static final String COLLECTION_MENSAJES = "mensajes";
    private static final String COLLECTION_NOTIFICACIONES = "notificaciones";
    private static final String COLLECTION_RESENAS = "resenas";
    private static final String COLLECTION_LOGS = "logs_sistema";
    private static final String COLLECTION_TRAMITES = "tramites";
    private static final String COLLECTION_HISTORIAL = "historial_usuario";

    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final FirebaseFirestore firestore;

    public LocalSchemaStorage(Context context) {
        this.context = context.getApplicationContext();
        this.sharedPreferences = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.firestore = FirebaseFirestore.getInstance();
        ensureSeedData();
    }

    public void resetSeedData() {
        sharedPreferences.edit().clear().apply();
        ensureSeedData();
    }

    public String exportSchemaSnapshot() {
        JSONObject root = new JSONObject();
        try {
            root.put(COLLECTION_USUARIOS, readArray(COLLECTION_USUARIOS));
            root.put(COLLECTION_PROYECTOS, readArray(COLLECTION_PROYECTOS));
            root.put(COLLECTION_TIPOLOGIAS, readArray(COLLECTION_TIPOLOGIAS));
            root.put(COLLECTION_AMENIDADES, readArray(COLLECTION_AMENIDADES));
            root.put(COLLECTION_IMAGENES, readArray(COLLECTION_IMAGENES));
            root.put(COLLECTION_SOLICITUDES, readArray(COLLECTION_SOLICITUDES));
            root.put(COLLECTION_CITAS, readArray(COLLECTION_CITAS));
            root.put(COLLECTION_EVENTOS_CITA, readArray(COLLECTION_EVENTOS_CITA));
            root.put(COLLECTION_SEPARACIONES, readArray(COLLECTION_SEPARACIONES));
            root.put(COLLECTION_CONVERSACIONES, readArray(COLLECTION_CONVERSACIONES));
            root.put(COLLECTION_MENSAJES, readArray(COLLECTION_MENSAJES));
            root.put(COLLECTION_NOTIFICACIONES, readArray(COLLECTION_NOTIFICACIONES));
            root.put(COLLECTION_RESENAS, readArray(COLLECTION_RESENAS));
            root.put(COLLECTION_LOGS, readArray(COLLECTION_LOGS));
            root.put(COLLECTION_TRAMITES, readArray(COLLECTION_TRAMITES));
            root.put(COLLECTION_HISTORIAL, readArray(COLLECTION_HISTORIAL));
        } catch (JSONException ignored) {
            return "{}";
        }
        return root.toString();
    }

    public List<SuperadminGestionUsuarioItem> getSuperadminUsers() {
        List<SuperadminGestionUsuarioItem> items = new ArrayList<>();
        JSONArray usuarios = readArray(COLLECTION_USUARIOS);
        for (int i = 0; i < usuarios.length(); i++) {
            JSONObject user = usuarios.optJSONObject(i);
            if (user == null || "superadmin".equals(user.optString("rol"))) {
                continue;
            }
            items.add(new SuperadminGestionUsuarioItem(
                    user.optString("nombres") + " " + user.optString("apellidos"),
                    user.optString("email"),
                    "AGENCIA: " + user.optString("inmobiliariaNombre", "SIN AGENCIA").toUpperCase(Locale.ROOT),
                    imageRes(user.optString("avatarKey")),
                    "activo".equalsIgnoreCase(user.optString("estado"))
            ));
        }
        return items;
    }

    public List<SuperadminSolicitudAsesorItem> getSuperadminAdvisorRequests() {
        List<SuperadminSolicitudAsesorItem> items = new ArrayList<>();
        JSONArray requests = readArray(COLLECTION_SOLICITUDES);
        for (int i = 0; i < requests.length(); i++) {
            JSONObject request = requests.optJSONObject(i);
            if (request == null) {
                continue;
            }
            items.add(new SuperadminSolicitudAsesorItem(
                    request.optString("nombre"),
                    request.optString("email"),
                    "Agencia: " + request.optString("inmobiliariaNombre"),
                    imageRes(request.optString("avatarKey")),
                    request.optString("estado").toUpperCase(Locale.ROOT)
            ));
        }
        return items;
    }

    public List<SuperadminControlAccesoItem> getSuperadminAccessItems() {
        List<SuperadminControlAccesoItem> items = new ArrayList<>();
        JSONArray usuarios = readArray(COLLECTION_USUARIOS);
        for (int i = 0; i < usuarios.length() && items.size() < 3; i++) {
            JSONObject user = usuarios.optJSONObject(i);
            if (user == null || !"activo".equalsIgnoreCase(user.optString("estado"))) {
                continue;
            }
            items.add(new SuperadminControlAccesoItem(
                    user.optString("nombres") + " " + user.optString("apellidos"),
                    roleLabel(user.optString("rol")),
                    imageRes(user.optString("avatarKey"))
            ));
        }
        return items;
    }

    public List<SuperadminResumenLogItem> getSuperadminSummaryLogs() {
        List<SuperadminResumenLogItem> items = new ArrayList<>();
        JSONArray logs = readArray(COLLECTION_LOGS);
        for (int i = 0; i < logs.length() && items.size() < 4; i++) {
            JSONObject log = logs.optJSONObject(i);
            if (log == null) {
                continue;
            }
            int color = logColor(log.optString("nivel"));
            items.add(new SuperadminResumenLogItem(
                    log.optString("tipo").toUpperCase(Locale.ROOT) + "      " + log.optString("tiempo"),
                    log.optString("resumen"),
                    color,
                    color
            ));
        }
        return items;
    }

    public List<SuperadminLogEntryItem> getSuperadminLogs() {
        List<SuperadminLogEntryItem> items = new ArrayList<>();
        JSONArray logs = readArray(COLLECTION_LOGS);
        for (int i = 0; i < logs.length(); i++) {
            JSONObject log = logs.optJSONObject(i);
            if (log == null) {
                continue;
            }
            int color = logColor(log.optString("nivel"));
            items.add(new SuperadminLogEntryItem(
                    color,
                    logIcon(log.optString("tipo")),
                    color,
                    log.optString("titulo"),
                    log.optString("subtitulo"),
                    log.optString("tiempo"),
                    log.optString("detalle"),
                    log.optString("nivel").toUpperCase(Locale.ROOT),
                    color
            ));
        }
        return items;
    }

    public List<AdminProjectItem> getAdminProjects() {
        List<AdminProjectItem> items = new ArrayList<>();
        JSONArray projects = readArray(COLLECTION_PROYECTOS);
        for (int i = 0; i < projects.length(); i++) {
            JSONObject project = projects.optJSONObject(i);
            if (project == null) {
                continue;
            }
            items.add(new AdminProjectItem(
                    project.optString("nombre"),
                    project.optString("direccion"),
                    project.optString("precioDesde"),
                    project.optString("estadoComercial"),
                    imageRes(project.optString("imageKey"))
            ));
        }
        return items;
    }

    public List<AdminAssignableProjectItem> getAdminAssignableProjects() {
        List<AdminAssignableProjectItem> items = new ArrayList<>();
        JSONArray projects = readArray(COLLECTION_PROYECTOS);
        for (int i = 0; i < projects.length(); i++) {
            JSONObject project = projects.optJSONObject(i);
            if (project == null) {
                continue;
            }
            items.add(new AdminAssignableProjectItem(
                    project.optString("nombre"),
                    project.optString("direccion"),
                    project.optString("distrito"),
                    project.optString("estadoComercial"),
                    imageRes(project.optString("imageKey"))
            ));
        }
        return items;
    }

    public List<AdminAdvisorItem> getAdminAdvisors() {
        List<AdminAdvisorItem> items = new ArrayList<>();
        JSONArray usuarios = readArray(COLLECTION_USUARIOS);
        for (int i = 0; i < usuarios.length(); i++) {
            JSONObject user = usuarios.optJSONObject(i);
            if (user == null || !"asesor".equals(user.optString("rol"))) {
                continue;
            }
            items.add(new AdminAdvisorItem(
                    user.optString("nombres") + " " + user.optString("apellidos"),
                    user.optString("rating", "4.8"),
                    user.optString("email"),
                    "activo".equalsIgnoreCase(user.optString("estado")),
                    imageRes(user.optString("avatarKey")),
                    stringList(user.optJSONArray("proyectosAsignados"))
            ));
        }
        return items;
    }

    public List<AdminAssignedProjectItem> getAdminAssignedProjects() {
        List<AdminAssignedProjectItem> items = new ArrayList<>();
        JSONArray projects = readArray(COLLECTION_PROYECTOS);
        for (int i = 0; i < projects.length() && items.size() < 6; i++) {
            JSONObject project = projects.optJSONObject(i);
            if (project == null) {
                continue;
            }
            items.add(new AdminAssignedProjectItem(
                    project.optString("nombre"),
                    project.optString("distrito") + ", Lima",
                    project.optString("assignmentStatus", "ACTIVO"),
                    imageRes(project.optString("imageKey"))
            ));
        }
        return items;
    }

    public List<AdminRequestItem> getAdminRequests() {
        List<AdminRequestItem> items = new ArrayList<>();
        JSONArray requests = readArray(COLLECTION_SOLICITUDES);
        for (int i = 0; i < requests.length(); i++) {
            JSONObject request = requests.optJSONObject(i);
            if (request == null) {
                continue;
            }
            items.add(new AdminRequestItem(
                    request.optString("id"),
                    request.optString("nombre"),
                    request.optString("email"),
                    request.optString("subtitle"),
                    request.optString("descripcion"),
                    request.optString("estado").toUpperCase(Locale.ROOT),
                    imageRes(request.optString("avatarKey")),
                    request.optString("proyectoNombre")
            ));
        }
        return items;
    }

    public String addAdminProject(AdminProjectDraft draft) {
        JSONArray projects = readArray(COLLECTION_PROYECTOS);
        String projectId = "proy_local_" + System.currentTimeMillis();
        try {
            projects.put(projectToJson(projectId, draft));
            persistArray(COLLECTION_PROYECTOS, projects);
            saveDraftProjectCollections(projectId, draft);
            addAdminNotification(
                    "project_published_" + projectId,
                    "action",
                    "Proyecto publicado",
                    "Hace un momento",
                    safeProjectName(draft),
                    "Guardado en storage local",
                    "VER PROYECTOS"
            );
            return projectId;
        } catch (Exception ignored) {
            return "";
        }
    }

    public boolean updateAdminProject(String originalProjectName, AdminProjectDraft draft) {
        JSONArray projects = readArray(COLLECTION_PROYECTOS);
        String fallbackName = safeProjectName(draft);
        int indexToUpdate = -1;

        for (int i = 0; i < projects.length(); i++) {
            JSONObject project = projects.optJSONObject(i);
            if (project == null) {
                continue;
            }
            String currentName = project.optString("nombre");
            if ((!isEmpty(originalProjectName) && originalProjectName.equalsIgnoreCase(currentName))
                    || fallbackName.equalsIgnoreCase(currentName)) {
                indexToUpdate = i;
                break;
            }
        }

        if (indexToUpdate == -1 && projects.length() > 0) {
            indexToUpdate = 0;
        }

        if (indexToUpdate == -1) {
            return !addAdminProject(draft).isEmpty();
        }

        JSONObject current = projects.optJSONObject(indexToUpdate);
        String projectId = current != null ? current.optString("id", "proy_local_" + System.currentTimeMillis()) : "proy_local_" + System.currentTimeMillis();
        try {
            projects.put(indexToUpdate, projectToJson(projectId, draft));
            persistArray(COLLECTION_PROYECTOS, projects);
            saveDraftProjectCollections(projectId, draft);
            addAdminNotification(
                    "project_edited_" + projectId + "_" + System.currentTimeMillis(),
                    "action",
                    "Proyecto actualizado",
                    "Hace un momento",
                    fallbackName,
                    "Cambios guardados en storage local",
                    "VER PROYECTOS"
            );
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public AdminProjectDraft getAdminProjectDraftForEdit(String projectName) {
        JSONArray projects = readArray(COLLECTION_PROYECTOS);
        JSONObject selected = null;
        for (int i = 0; i < projects.length(); i++) {
            JSONObject project = projects.optJSONObject(i);
            if (project == null) {
                continue;
            }
            if (!isEmpty(projectName) && projectName.equalsIgnoreCase(project.optString("nombre"))) {
                selected = project;
                break;
            }
            if (selected == null) {
                selected = project;
            }
        }

        if (selected == null) {
            return null;
        }

        return new AdminProjectDraft(
                selected.optString("nombre"),
                selected.optString("descripcion", "Proyecto inmobiliario gestionado localmente."),
                selected.optString("direccion"),
                selected.optString("distrito"),
                selected.optString("mapLabel", "Mapa: " + selected.optString("direccion")),
                fromStorageStatus(selected.optString("estadoComercial")),
                selected.optString("fechaEntrega", "10/06/2025"),
                getAdminProjectFormTypologies(),
                getAdminProjectFormAmenities()
        );
    }

    public boolean addAdvisorProjectJoinRequest(String advisorName, String advisorEmail, String projectName) {
        JSONArray requests = readArray(COLLECTION_SOLICITUDES);
        String normalizedEmail = advisorEmail == null ? "" : advisorEmail.trim().toLowerCase(Locale.ROOT);
        String normalizedProject = projectName == null ? "" : projectName.trim();

        for (int i = 0; i < requests.length(); i++) {
            JSONObject request = requests.optJSONObject(i);
            if (request == null) {
                continue;
            }
            boolean sameAdvisor = normalizedEmail.equals(request.optString("email").toLowerCase(Locale.ROOT));
            boolean sameProject = normalizedProject.equalsIgnoreCase(request.optString("proyectoNombre"));
            boolean pending = "pendiente".equalsIgnoreCase(request.optString("estado"));
            if (sameAdvisor && sameProject && pending) {
                return false;
            }
        }

        String safeName = advisorName == null || advisorName.trim().isEmpty()
                ? "Asesor sin nombre"
                : advisorName.trim();
        String safeProject = normalizedProject.isEmpty() ? "Proyecto sin nombre" : normalizedProject;
        String requestId = "sol_local_" + System.currentTimeMillis();

        try {
            requests.put(obj(
                    "id", requestId,
                    "nombre", safeName,
                    "email", normalizedEmail,
                    "subtitle", "Solicitud enviada ahora",
                    "descripcion", safeName + " quiere unirse al proyecto " + safeProject + ".",
                    "estado", "pendiente",
                    "avatarKey", "sa_profile_asesor_1",
                    "inmobiliariaNombre", "The Editorial Estate",
                    "proyectoNombre", safeProject
            ));
            persistArray(COLLECTION_SOLICITUDES, requests);
            addAdminNotification(
                    "admin_action_" + requestId,
                    "action",
                    "Solicitud de asesor",
                    "Hace un momento",
                    safeName,
                    "Quiere unirse a " + safeProject,
                    "VER SOLICITUD"
            );
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public boolean updateAdvisorRequestStatus(String requestId, String newStatus) {
        JSONArray requests = readArray(COLLECTION_SOLICITUDES);
        String normalizedStatus = newStatus == null ? "pendiente" : newStatus.toLowerCase(Locale.ROOT);

        for (int i = 0; i < requests.length(); i++) {
            JSONObject request = requests.optJSONObject(i);
            if (request == null || !requestId.equals(request.optString("id"))) {
                continue;
            }

            try {
                request.put("estado", normalizedStatus);
                request.put("subtitle", "aceptada".equals(normalizedStatus)
                        ? "Aceptada hace un momento"
                        : "Rechazada hace un momento");
                requests.put(i, request);
                persistArray(COLLECTION_SOLICITUDES, requests);

                if ("aceptada".equals(normalizedStatus)) {
                    upsertAdvisorFromRequest(request);
                }

                return true;
            } catch (Exception ignored) {
                return false;
            }
        }
        return false;
    }

    public List<AdminReviewItem> getAdminReviews() {
        List<AdminReviewItem> items = new ArrayList<>();
        JSONArray reviews = readArray(COLLECTION_RESENAS);
        for (int i = 0; i < reviews.length(); i++) {
            JSONObject review = reviews.optJSONObject(i);
            if (review == null) {
                continue;
            }
            items.add(new AdminReviewItem(
                    review.optString("clienteNombre"),
                    review.optString("fecha"),
                    review.optString("proyectoNombre"),
                    review.optString("comentario"),
                    review.optString("rating") + "/5",
                    imageRes(review.optString("avatarKey"))
            ));
        }
        return items;
    }

    public List<AdminNotificationItem> getAdminNotifications() {
        List<AdminNotificationItem> items = new ArrayList<>();
        JSONArray notifications = readArray(COLLECTION_NOTIFICACIONES);
        for (int i = 0; i < notifications.length(); i++) {
            JSONObject notification = notifications.optJSONObject(i);
            if (notification == null || !"admin".equals(notification.optString("recipientRole"))) {
                continue;
            }
            items.add(new AdminNotificationItem(
                    notification.optString("id"),
                    "yesterday".equals(notification.optString("section"))
                            ? AdminNotificationItem.Section.YESTERDAY
                            : AdminNotificationItem.Section.TODAY,
                    adminNotificationType(notification.optString("tipo")),
                    notification.optString("titulo"),
                    notification.optString("badge"),
                    notification.optString("line1"),
                    notification.optString("line2"),
                    notification.optString("actionText", "REVISAR DETALLE")
            ));
        }
        return items;
    }

    public List<AdminProjectGalleryItem> getAdminProjectGallery() {
        List<AdminProjectGalleryItem> items = new ArrayList<>();
        JSONArray images = readArray(COLLECTION_IMAGENES);
        for (int i = 0; i < images.length(); i++) {
            JSONObject image = images.optJSONObject(i);
            if (image != null) {
                items.add(new AdminProjectGalleryItem(
                        imageRes(image.optString("imageKey")),
                        image.optString("imageUrl")
                ));
            }
        }
        return items;
    }

    public List<AdminProjectTypologyItem> getAdminProjectTypologies() {
        List<AdminProjectTypologyItem> items = new ArrayList<>();
        JSONArray typologies = readArray(COLLECTION_TIPOLOGIAS);
        for (int i = 0; i < typologies.length(); i++) {
            JSONObject typology = typologies.optJSONObject(i);
            if (typology == null) {
                continue;
            }
            boolean available = typology.optBoolean("available", true);
            items.add(new AdminProjectTypologyItem(
                    typology.optString("title"),
                    available ? "DISPONIBLE" : "NO DISPONIBLE",
                    available,
                    typology.optString("area"),
                    typology.optString("bedrooms"),
                    typology.optString("bathrooms", "2 banos"),
                    typology.optString("totalAmount"),
                    typology.optString("separationAmount")
            ));
        }
        return items;
    }

    public List<AdminProjectFormTypologyItem> getAdminProjectFormTypologies() {
        List<AdminProjectFormTypologyItem> items = new ArrayList<>();
        JSONArray typologies = readArray(COLLECTION_TIPOLOGIAS);
        for (int i = 0; i < typologies.length(); i++) {
            JSONObject typology = typologies.optJSONObject(i);
            if (typology == null) {
                continue;
            }
            items.add(new AdminProjectFormTypologyItem(
                    typology.optString("title"),
                    typology.optBoolean("available", true),
                    typology.optString("area"),
                    typology.optString("bedrooms"),
                    typology.optString("bathrooms", "2 banos"),
                    typology.optString("totalAmount"),
                    typology.optString("separationAmount")
            ));
        }
        return items;
    }

    public List<AdminProjectAmenityItem> getAdminProjectAmenities() {
        List<AdminProjectAmenityItem> items = new ArrayList<>();
        JSONArray amenities = readArray(COLLECTION_AMENIDADES);
        for (int i = 0; i < amenities.length(); i++) {
            JSONObject amenity = amenities.optJSONObject(i);
            if (amenity != null) {
                items.add(new AdminProjectAmenityItem(
                        amenity.optString("title"),
                        amenityIcon(amenity.optString("icon"))
                ));
            }
        }
        return items;
    }

    public List<AdminProjectFormAmenityItem> getAdminProjectFormAmenities() {
        List<AdminProjectFormAmenityItem> items = new ArrayList<>();
        JSONArray amenities = readArray(COLLECTION_AMENIDADES);
        for (int i = 0; i < amenities.length(); i++) {
            JSONObject amenity = amenities.optJSONObject(i);
            if (amenity != null) {
                items.add(new AdminProjectFormAmenityItem(
                        amenity.optString("title"),
                        amenityIcon(amenity.optString("icon")),
                        amenity.optBoolean("selected", true)
                ));
            }
        }
        return items;
    }

    public List<AdminProjectVisualItem> getAdminProjectCreateVisualSlots() {
        List<AdminProjectVisualItem> items = new ArrayList<>();
        String[] titles = {"Portada principal", "Fachada", "Lobby", "Amenidades", "Rooftop"};
        for (String title : titles) {
            items.add(new AdminProjectVisualItem(title, "Agregar foto", 0, false));
        }
        return items;
    }

    public List<AdminProjectVisualItem> getAdminProjectEditVisuals() {
        List<AdminProjectVisualItem> items = new ArrayList<>();
        String[] titles = {"Portada principal", "Fachada", "Lobby", "Amenidades", "Rooftop"};
        JSONArray images = readArray(COLLECTION_IMAGENES);
        for (int i = 0; i < titles.length; i++) {
            JSONObject image = i < images.length() ? images.optJSONObject(i) : null;
            int imageRes = image != null ? imageRes(image.optString("imageKey")) : 0;
            items.add(new AdminProjectVisualItem(titles[i], imageRes == 0 ? "Agregar foto" : "Cambiar foto", imageRes, imageRes != 0));
        }
        return items;
    }

    public List<Cita> getAdvisorCitas() {
        List<Cita> items = new ArrayList<>();
        JSONArray citas = readArray(COLLECTION_CITAS);
        for (int i = 0; i < citas.length(); i++) {
            JSONObject cita = citas.optJSONObject(i);
            if (cita == null) {
                continue;
            }
            Cita item = new Cita(
                    cita.optString("id"),
                    cita.optString("clienteNombre"),
                    cita.optString("inmuebleNombre"),
                    cita.optString("hora"),
                    relativeDate(cita.optInt("dateOffset", 0)),
                    cita.optString("estado"),
                    cita.optString("proyectoNombre"),
                    cita.optBoolean("hasCierre")
            );
            List<EventoCita> events = getCitaEventos(item.getId());
            for (EventoCita event : events) {
                item.addEvento(event);
            }
            items.add(item);
        }
        return items;
    }

    public Cita getAdvisorPrimaryCita() {
        List<Cita> citas = getAdvisorCitas();
        return citas.isEmpty() ? null : citas.get(0);
    }

    public List<EventoCita> getCitaEventos(String citaId) {
        List<EventoCita> items = new ArrayList<>();
        JSONArray events = readArray(COLLECTION_EVENTOS_CITA);
        for (int i = 0; i < events.length(); i++) {
            JSONObject event = events.optJSONObject(i);
            if (event == null || !citaId.equals(event.optString("citaId"))) {
                continue;
            }
            items.add(new EventoCita(
                    event.optString("id"),
                    event.optString("citaId"),
                    event.optString("titulo"),
                    event.optString("detalle"),
                    event.optString("fechaHora"),
                    event.optString("tipo")
            ));
        }
        return items;
    }

    public List<Separacion> getAdvisorSeparaciones() {
        List<Separacion> items = new ArrayList<>();
        JSONArray separaciones = readArray(COLLECTION_SEPARACIONES);
        for (int i = 0; i < separaciones.length(); i++) {
            JSONObject sepObj = separaciones.optJSONObject(i);
            if (sepObj == null) continue;
            items.add(parseSeparacion(sepObj));
        }
        return items;
    }

    public List<Separacion> getSeparacionesByClient(String clientId) {
        List<Separacion> items = new ArrayList<>();
        JSONArray separaciones = readArray(COLLECTION_SEPARACIONES);
        for (int i = 0; i < separaciones.length(); i++) {
            JSONObject sepObj = separaciones.optJSONObject(i);
            if (sepObj != null && clientId.equals(sepObj.optString("clientId"))) {
                items.add(parseSeparacion(sepObj));
            }
        }
        return items;
    }

    public int getActiveSeparationsCount(String clientId) {
        int count = 0;
        JSONArray separaciones = readArray(COLLECTION_SEPARACIONES);
        for (int i = 0; i < separaciones.length(); i++) {
            JSONObject sepObj = separaciones.optJSONObject(i);
            if (sepObj != null && clientId.equals(sepObj.optString("clientId"))) {
                String status = sepObj.optString("status");
                if ("borrador".equalsIgnoreCase(status) || "pendiente".equalsIgnoreCase(status)) {
                    count++;
                }
            }
        }
        return count;
    }

    private Separacion parseSeparacion(JSONObject obj) {
        Separacion sep = new Separacion(
                obj.optString("id"),
                obj.optString("clientId"),
                obj.optString("asesorId"),
                obj.optString("projectId"),
                obj.optString("tipologiaId"),
                obj.optString("clienteNombre"),
                obj.optString("inmuebleNombre"),
                obj.optString("montoTexto"),
                obj.optString("fechaTexto"),
                imageRes(obj.optString("imageKey")),
                obj.optString("status")
        );
        sep.setVerificableUrl(obj.optString("verificableUrl", ""));
        return sep;
    }

    public List<Chat> getAdvisorChats() {
        List<Chat> items = new ArrayList<>();
        JSONArray conversations = readArray(COLLECTION_CONVERSACIONES);
        for (int i = 0; i < conversations.length(); i++) {
            JSONObject chat = conversations.optJSONObject(i);
            if (chat == null || !"asesor".equals(chat.optString("viewFor"))) {
                continue;
            }
            items.add(new Chat(
                    chat.optString("id"),
                    chat.optString("nombre"),
                    chat.optString("lastMessage"),
                    chat.optString("time"),
                    imageRes(chat.optString("avatarKey")),
                    chat.optString("initials", null),
                    chat.optBoolean("unread")
            ));
        }
        return items;
    }

    public List<MensajeChat> getAdvisorMessages() {
        List<MensajeChat> items = new ArrayList<>();
        JSONArray messages = readArray(COLLECTION_MENSAJES);
        for (int i = 0; i < messages.length(); i++) {
            JSONObject message = messages.optJSONObject(i);
            if (message == null) {
                continue;
            }
            if (message.optBoolean("dateHeader")) {
                items.add(new MensajeChat(message.optString("text"), true));
            } else {
                items.add(new MensajeChat(
                        message.optString("id"),
                        message.optString("text"),
                        message.optString("time"),
                        message.optBoolean("sentByMe")
                ));
            }
        }
        return items;
    }

    public List<UsuarioPropertyListItem> getUserPropertyListItems() {
        List<UsuarioPropertyListItem> items = new ArrayList<>();
        JSONArray projects = readArray(COLLECTION_PROYECTOS);
        for (int i = 0; i < projects.length(); i++) {
            JSONObject project = projects.optJSONObject(i);
            if (project == null) {
                continue;
            }
            String imageKey = project.optString("userImageKey", project.optString("imageKey", "user_featured_house"));
            items.add(new UsuarioPropertyListItem(
                    firstNonEmpty(project.optString("propertyId"), project.optString("projectId"), project.optString("id"), "fallback_property"),
                    firstNonEmpty(project.optString("badge"), project.optString("estadoComercial"), project.optString("estado"), "PROYECTO"),
                    project.optString("nombre"),
                    project.optString("direccion"),
                    firstNonEmpty(project.optString("precioDesde"), "Precio por definir"),
                    fallbackImageRes(isEmpty(imageKey) ? "user_featured_house" : imageKey)
            ));
        }
        return items;
    }

    public List<UsuarioAppointmentItem> getUserAppointments(String clienteId) {
        List<UsuarioAppointmentItem> items = new ArrayList<>();
        JSONArray citas = readArray(COLLECTION_CITAS);
        for (int i = citas.length() - 1; i >= 0 && items.size() < 15; i--) {
            JSONObject cita = citas.optJSONObject(i);
            if (cita == null) continue;
            if (clienteId != null && !clienteId.isEmpty()
                    && !clienteId.equals(cita.optString("clienteId"))) continue;
            items.add(new UsuarioAppointmentItem(
                    cita.optString("inmuebleNombre"),
                    cita.optString("status").toUpperCase(Locale.ROOT),
                    cita.optString("fechaTexto") + ", " + cita.optString("hora"),
                    cita.optString("asesorNombre"),
                    imageRes(cita.optString("imageKey")),
                    cita.optString("meetingPoint"),
                    cita.optString("nota"),
                    "Confirmada".equalsIgnoreCase(cita.optString("status"))
            ));
        }
        return items;
    }

    public List<UsuarioTramiteItem> getUserTramites(String clienteId) {
        List<UsuarioTramiteItem> items = new ArrayList<>();
        JSONArray tramites = readArray(COLLECTION_TRAMITES);
        for (int i = 0; i < tramites.length(); i++) {
            JSONObject tramite = tramites.optJSONObject(i);
            if (tramite == null) continue;
            if (clienteId != null && !clienteId.isEmpty()
                    && !clienteId.equals(tramite.optString("clienteId"))) continue;
            items.add(new UsuarioTramiteItem(
                    tramite.optString("title"),
                    tramite.optString("code"),
                    tramite.optString("status"),
                    tramite.optString("note"),
                    tramite.optString("due"),
                    tramite.optBoolean("canPay")
            ));
        }
        return items;
    }

    public List<UsuarioHistoryItem> getUserHistory(String clienteId) {
        List<UsuarioHistoryItem> items = new ArrayList<>();
        JSONArray history = readArray(COLLECTION_HISTORIAL);
        for (int i = 0; i < history.length(); i++) {
            JSONObject item = history.optJSONObject(i);
            if (item == null) continue;
            if (clienteId != null && !clienteId.isEmpty()
                    && !clienteId.equals(item.optString("clienteId"))) continue;
            items.add(new UsuarioHistoryItem(
                    item.optString("badge"),
                    item.optString("title"),
                    item.optString("date"),
                    item.optString("summary"),
                    item.optString("status"),
                    item.optString("code"),
                    item.optString("amount")
            ));
        }
        return items;
    }

    public List<UsuarioChatListItem> getUserChats() {
        List<UsuarioChatListItem> items = new ArrayList<>();
        JSONArray conversations = readArray(COLLECTION_CONVERSACIONES);
        for (int i = 0; i < conversations.length(); i++) {
            JSONObject chat = conversations.optJSONObject(i);
            if (chat == null || !"cliente".equals(chat.optString("viewFor"))) {
                continue;
            }
            items.add(new UsuarioChatListItem(
                    chat.optString("nombre"),
                    chat.optString("lastMessage"),
                    chat.optString("time"),
                    imageRes(chat.optString("avatarKey")),
                    chat.optString("initials"),
                    chat.optBoolean("usesInitials"),
                    chat.optBoolean("unread"),
                    chat.optBoolean("favorite")
            ));
        }
        return items;
    }

    public List<UsuarioNotificationItem> getUserNotifications() {
        List<UsuarioNotificationItem> items = new ArrayList<>();
        JSONArray notifications = readArray(COLLECTION_NOTIFICACIONES);
        for (int i = 0; i < notifications.length(); i++) {
            JSONObject notification = notifications.optJSONObject(i);
            if (notification == null || !"cliente".equals(notification.optString("recipientRole"))) {
                continue;
            }
            items.add(new UsuarioNotificationItem(
                    "visit".equals(notification.optString("tipo"))
                            ? UsuarioNotificationItem.TYPE_VISIT
                            : UsuarioNotificationItem.TYPE_APPROVAL,
                    notification.optString("badge"),
                    notification.optString("titulo"),
                    notification.optString("body"),
                    notification.optString("actionText"),
                    "payment".equals(notification.optString("action"))
                            ? UsuarioNotificationItem.ACTION_PAYMENT
                            : UsuarioNotificationItem.ACTION_APPOINTMENT
            ));
        }
        return items;
    }

    public List<UsuarioPropertyCatalog.PropertyDetail> getUserExploreProperties() {
        List<UsuarioPropertyCatalog.PropertyDetail> items = new ArrayList<>();
        for (UsuarioPropertyListItem item : getUserPropertyListItems()) {
            UsuarioPropertyCatalog.PropertyDetail detail = UsuarioPropertyCatalog.getById(item.getPropertyId());
            if (detail != null) {
                items.add(detail);
            }
        }
        return items;
    }

    private void ensureSeedData() {
        if (sharedPreferences.getBoolean(KEY_INITIALIZED, false)) {
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(COLLECTION_USUARIOS, seedUsuarios().toString());
        editor.putString(COLLECTION_PROYECTOS, seedProyectos().toString());
        editor.putString(COLLECTION_TIPOLOGIAS, seedTipologias().toString());
        editor.putString(COLLECTION_AMENIDADES, seedAmenidades().toString());
        editor.putString(COLLECTION_IMAGENES, seedImagenes().toString());
        editor.putString(COLLECTION_SOLICITUDES, seedSolicitudes().toString());
        editor.putString(COLLECTION_CITAS, seedCitas().toString());
        editor.putString(COLLECTION_EVENTOS_CITA, seedEventosCita().toString());
        editor.putString(COLLECTION_SEPARACIONES, seedSeparaciones().toString());
        editor.putString(COLLECTION_CONVERSACIONES, seedConversaciones().toString());
        editor.putString(COLLECTION_MENSAJES, seedMensajes().toString());
        editor.putString(COLLECTION_NOTIFICACIONES, seedNotificaciones().toString());
        editor.putString(COLLECTION_RESENAS, seedResenas().toString());
        editor.putString(COLLECTION_LOGS, seedLogs().toString());
        editor.putString(COLLECTION_TRAMITES, seedTramites().toString());
        editor.putString(COLLECTION_HISTORIAL, seedHistorial().toString());
        editor.putBoolean(KEY_INITIALIZED, true);
        editor.apply();
    }

    private JSONArray seedUsuarios() {
        return array(
                obj("id", "usr_super_001", "rol", "superadmin", "nombres", "Julian", "apellidos", "Reed", "email", "superadmin@estate.pe", "password", "super123", "estado", "activo", "avatarKey", "sa_avatar_07", "inmobiliariaNombre", "Sistema"),
                obj("id", "usr_admin_001", "rol", "admin", "nombres", "Administrador", "apellidos", "Editorial", "email", "admin@editorialestate.com", "password", "admin123", "telefono", "+51 987 654 321", "estado", "activo", "avatarKey", "sa_profile_admin", "inmobiliariaId", "inmo_editorial", "inmobiliariaNombre", "The Editorial Estate"),
                obj("id", "usr_asesor_001", "rol", "asesor", "nombres", "Elena", "apellidos", "Valdes", "email", "evaldes@editorialestate.com", "password", "asesor123", "telefono", "+51 987 111 222", "estado", "activo", "avatarKey", "sa_profile_asesor_1", "rating", "5.0", "inmobiliariaId", "inmo_editorial", "inmobiliariaNombre", "The Editorial Estate", "proyectosAsignados", arrayStrings("Catalina Sky View", "Villa Luminara")),
                obj("id", "usr_asesor_002", "rol", "asesor", "nombres", "Julian", "apellidos", "Costa", "email", "jcosta@editorialestate.com", "password", "asesor123", "telefono", "+51 987 222 333", "estado", "inactivo", "avatarKey", "sa_profile_asesor_3", "rating", "4.8", "inmobiliariaId", "inmo_editorial", "inmobiliariaNombre", "The Editorial Estate", "proyectosAsignados", arrayStrings("The Iron Works")),
                obj("id", "usr_asesor_003", "rol", "asesor", "nombres", "Sofia", "apellidos", "Mendez", "email", "smendez@editorialestate.com", "password", "asesor123", "telefono", "+51 987 333 444", "estado", "activo", "avatarKey", "sa_profile_asesor_2", "rating", "4.9", "inmobiliariaId", "inmo_editorial", "inmobiliariaNombre", "The Editorial Estate", "proyectosAsignados", arrayStrings("Refugio Celeste", "Casa Meridian")),
                obj("id", "usr_cliente_001", "rol", "cliente", "nombres", "Alicia", "apellidos", "Velarde", "email", "alicia.velarde@mail.com", "password", "cliente123", "telefono", "+51 987 456 210", "estado", "activo", "avatarKey", "sa_profile_user_1", "inmobiliariaNombre", "The Editorial Estate"),
                obj("id", "usr_cliente_002", "rol", "cliente", "nombres", "Julian", "apellidos", "Mendoza", "email", "julian.mendoza@mail.com", "password", "cliente123", "telefono", "+51 987 456 211", "estado", "activo", "avatarKey", "sa_profile_user_2", "inmobiliariaNombre", "The Editorial Estate")
        );
    }

    private JSONArray seedProyectos() {
        return array(
                obj("id", "proy_001", "propertyId", UsuarioPropertyCatalog.ID_VILLA_LUMINARA, "nombre", "Villa Luminara", "direccion", "Av. Javier Prado 450, San Isidro", "distrito", "Polanco", "precioDesde", "USD 1.2M", "estadoComercial", "EN PREVENTA", "badge", "CURADURIA DESTACADA", "imageKey", "sa_profile_admin", "userImageKey", "user_featured_house", "assignmentStatus", "ACTIVO"),
                obj("id", "proy_002", "propertyId", UsuarioPropertyCatalog.ID_IRON_WORKS, "nombre", "The Iron Works", "direccion", "Calle Monte Real 210, Miraflores", "distrito", "Santa Fe", "precioDesde", "USD 1.8M", "estadoComercial", "EN VENTA", "badge", "LOFT INDUSTRIAL", "imageKey", "sa_profile_admin", "userImageKey", "user_popular_1", "assignmentStatus", "EN CURSO"),
                obj("id", "proy_003", "propertyId", UsuarioPropertyCatalog.ID_REFUGIO_CELESTE, "nombre", "Refugio Celeste", "direccion", "Malecon Cisneros 780, Barranco", "distrito", "Roma Norte", "precioDesde", "USD 980K", "estadoComercial", "EN PLANOS", "badge", "COSTA AZUL", "imageKey", "sa_profile_admin", "userImageKey", "user_popular_2", "assignmentStatus", "ACTIVO"),
                obj("id", "proy_004", "propertyId", UsuarioPropertyCatalog.ID_CASA_MERIDIAN, "nombre", "Casa Meridian", "direccion", "Av. El Golf 145, San Isidro", "distrito", "Polanco", "precioDesde", "USD 1.6M", "estadoComercial", "EN VENTA", "badge", "NUEVA COLECCION", "imageKey", "sa_profile_admin", "userImageKey", "user_featured_house", "assignmentStatus", "EN CURSO"),
                obj("id", "proy_005", "propertyId", UsuarioPropertyCatalog.ID_ATICO_DEL_PARQUE, "nombre", "Atico del Parque", "direccion", "Av. La Encalada 900, Surco", "distrito", "Santa Fe", "precioDesde", "USD 1.05M", "estadoComercial", "EN PREVENTA", "badge", "EN VENTA", "imageKey", "sa_profile_admin", "userImageKey", "user_property_hero_real", "assignmentStatus", "ACTIVO")
        );
    }

    private JSONArray seedTipologias() {
        return array(
                obj("projectId", "proy_001", "title", "Tipo A", "available", true, "area", "70 m2", "bedrooms", "2 habs", "bathrooms", "2 banos", "totalAmount", "350,000 USD", "separationAmount", "1,500 USD"),
                obj("projectId", "proy_001", "title", "Tipo B", "available", false, "area", "80 m2", "bedrooms", "3 habs", "bathrooms", "2 banos", "totalAmount", "400,000 USD", "separationAmount", "1,700 USD"),
                obj("projectId", "proy_001", "title", "Tipo C", "available", true, "area", "60 m2", "bedrooms", "1 hab", "bathrooms", "1 bano", "totalAmount", "310,000 USD", "separationAmount", "1,400 USD"),
                obj("projectId", "proy_001", "title", "Tipo D", "available", true, "area", "95 m2", "bedrooms", "3 habs", "bathrooms", "3 banos", "totalAmount", "410,000 USD", "separationAmount", "1,800 USD")
        );
    }

    private JSONArray seedAmenidades() {
        return array(
                obj("projectId", "proy_001", "title", "Coworking", "icon", "laptop", "selected", true),
                obj("projectId", "proy_001", "title", "Piscina", "icon", "pool", "selected", true),
                obj("projectId", "proy_001", "title", "Terraza", "icon", "terrace", "selected", false),
                obj("projectId", "proy_001", "title", "Sala lounge", "icon", "lobby", "selected", true),
                obj("projectId", "proy_001", "title", "Gimnasio", "icon", "gym", "selected", false),
                obj("projectId", "proy_001", "title", "Zona BBQ", "icon", "bbq", "selected", true)
        );
    }

    private JSONArray seedImagenes() {
        return array(
                obj("projectId", "proy_001", "imageKey", "sa_profile_admin"),
                obj("projectId", "proy_001", "imageKey", "user_featured_house"),
                obj("projectId", "proy_001", "imageKey", "user_popular_1"),
                obj("projectId", "proy_001", "imageKey", "user_popular_2"),
                obj("projectId", "proy_001", "imageKey", "user_property_hero_real")
        );
    }

    private JSONArray seedSolicitudes() {
        return array(
                obj("id", "sol_001", "nombre", "Elena Valdes", "email", "evaldes@editorialestate.com", "subtitle", "Registro enviado hoy", "descripcion", "Solicita unirse a The Editorial Estate como asesora inmobiliaria.", "estado", "pendiente", "avatarKey", "sa_profile_asesor_1", "inmobiliariaNombre", "The Editorial Estate"),
                obj("id", "sol_002", "nombre", "Sofia Mendez", "email", "smendez@editorialestate.com", "subtitle", "Aceptada hace 2 dias", "descripcion", "Perfil aprobado para integrarse al equipo comercial.", "estado", "aceptada", "avatarKey", "sa_profile_asesor_2", "inmobiliariaNombre", "The Editorial Estate"),
                obj("id", "sol_003", "nombre", "Julian Costa", "email", "jcosta@editorialestate.com", "subtitle", "Registro enviado ayer", "descripcion", "Solicita habilitar acceso para gestionar proyectos activos.", "estado", "pendiente", "avatarKey", "sa_profile_asesor_3", "inmobiliariaNombre", "The Editorial Estate")
        );
    }

    private JSONArray seedCitas() {
        return new JSONArray(); // vacío — el cliente llena esto al agendar citas
    }

    private JSONArray seedEventosCita() {
        return array(
                obj("id", "evt_001", "citaId", "cita_001", "titulo", "Cita agendada", "detalle", "Agendada desde la app por el cliente", "fechaHora", "2026-05-23T14:32", "tipo", "AGENDADA"),
                obj("id", "evt_002", "citaId", "cita_001", "titulo", "Cita confirmada por el asesor", "detalle", "Confirmada via panel del asesor", "fechaHora", "2026-05-24T08:00", "tipo", "CONFIRMADA"),
                obj("id", "evt_003", "citaId", "cita_004", "titulo", "Separacion registrada", "detalle", "La cita derivo en una separacion pendiente de pago", "fechaHora", "2026-05-22T10:35", "tipo", "SEPARACION")
        );
    }

    private JSONArray seedSeparaciones() {
        return array(
                obj("id", "ASP-294", "clientId", "usr_cliente_001", "asesorId", "usr_asesor_001", "projectId", "proy_001", "tipologiaId", "Tipo A", "clienteNombre", "Alicia Velarde", "inmuebleNombre", "Villa Luminara", "montoTexto", "$950,000", "fechaTexto", "12 Oct 2026", "status", "pendiente", "imageKey", "as_property_04", "verificableUrl", "http://example.com/doc1"),
                obj("id", "ASP-288", "clientId", "usr_cliente_001", "asesorId", "usr_asesor_001", "projectId", "proy_002", "tipologiaId", "Tipo B", "clienteNombre", "Alicia Velarde", "inmuebleNombre", "The Iron Works", "montoTexto", "$1,200,000", "fechaTexto", "10 Oct 2026", "status", "borrador", "imageKey", "as_property_05", "verificableUrl", "http://example.com/doc2")
        );
    }

    private JSONArray seedConversaciones() {
        return array(
                obj("id", "chat_001", "clientId", "usr_cliente_001", "viewFor", "asesor", "nombre", "Alicia Velarde", "lastMessage", "El piso del entrepiso se ve...", "time", "14:02 PM", "avatarKey", "sa_profile_user_1", "initials", "", "unread", true),
                obj("id", "chat_002", "clientId", "usr_cliente_002", "viewFor", "asesor", "nombre", "Julian Mendoza", "lastMessage", "Le envio los planos para el...", "time", "AYER", "avatarKey", "sa_profile_user_2", "initials", "", "unread", false)
        );
    }

    private JSONArray seedMensajes() {
        return array(
                obj("dateHeader", true, "text", "LUNES, 24 DE OCT"),
                obj("id", "msg_001", "text", "Hola Julian, vi la propiedad Villa Luminara. El plano se ve muy bien.", "time", "10:14 AM", "sentByMe", false),
                obj("id", "msg_002", "text", "Buen dia. El proyecto tiene disponibilidad para visita este sabado.", "time", "10:16 AM", "sentByMe", true),
                obj("id", "msg_003", "text", "Me gustaria agendar una visita presencial si es posible.", "time", "10:20 AM", "sentByMe", false),
                obj("id", "msg_004", "text", "Claro, tengo un espacio a las 10:30 AM. Te lo separo.", "time", "10:28 AM", "sentByMe", true)
        );
    }

    private JSONArray seedNotificaciones() {
        return array(
                obj("id", "payment_1", "recipientRole", "admin", "tipo", "payment", "section", "today", "titulo", "Pago Recibido", "badge", "Hace menos de 10 min", "line1", "Unidad 402 - Torre B", "line2", "$4,500.00 USD", "actionText", "REVISAR DETALLE"),
                obj("id", "separation_1", "recipientRole", "admin", "tipo", "separation", "section", "today", "titulo", "Nueva Separacion", "badge", "Hace 2 horas", "line1", "Cliente: Carlos Mendoza", "line2", "Deposito: $1,000.00 USD", "actionText", "REVISAR DETALLE"),
                obj("id", "action_1", "recipientRole", "admin", "tipo", "action", "section", "yesterday", "titulo", "Accion Requerida", "badge", "Ayer, 14:30", "line1", "Pago expirado para Separacion #8492", "line2", "", "actionText", "REVISAR DETALLE"),
                obj("id", "user_notif_1", "recipientRole", "cliente", "tipo", "approval", "titulo", "Solicitud de separacion aprobada", "badge", "10:24 AM", "body", "Felicidades. Tu solicitud para Villa Luminara ha sido validada satisfactoriamente.", "actionText", "PROCEDER AL PAGO", "action", "payment"),
                obj("id", "user_notif_2", "recipientRole", "cliente", "tipo", "visit", "titulo", "Cita confirmada", "badge", "08:15 AM", "body", "Tu visita guiada a Villa Luminara esta programada para manana a las 11:00 AM.", "actionText", "", "action", "appointment")
        );
    }

    private JSONArray seedResenas() {
        return array(
                obj("id", "res_001", "clienteNombre", "Alicia Velarde", "fecha", "24 Oct 2026", "proyectoNombre", "Villa Luminara", "comentario", "La asesora resolvio dudas tecnicas y financieras con claridad.", "rating", "5.0", "avatarKey", "sa_profile_user_1"),
                obj("id", "res_002", "clienteNombre", "Julian Mendoza", "fecha", "21 Oct 2026", "proyectoNombre", "The Iron Works", "comentario", "Buen seguimiento durante la visita y envio oportuno de planos.", "rating", "4.8", "avatarKey", "sa_profile_user_2"),
                obj("id", "res_003", "clienteNombre", "Maria Garcia", "fecha", "18 Oct 2026", "proyectoNombre", "Casa Meridian", "comentario", "La separacion fue fluida y el asesor explico los plazos.", "rating", "4.9", "avatarKey", "sa_profile_user_1")
        );
    }

    private JSONArray seedLogs() {
        return array(
                obj("id", "log_001", "tipo", "alerta", "nivel", "critico", "titulo", "Error de Sistema", "subtitulo", "Kernel-Level Exception", "tiempo", "14:20", "detalle", "- ADMIN_042", "resumen", "Error critico detectado en validacion de pago"),
                obj("id", "log_002", "tipo", "pago", "nivel", "alerta", "titulo", "Pago Fallido", "subtitulo", "Ref: TXN-9921-BA", "tiempo", "Hace 5 min", "detalle", "- USER_ID: 8821", "resumen", "Pago fallido para separacion activa"),
                obj("id", "log_003", "tipo", "acceso", "nivel", "exito", "titulo", "Login Exitoso", "subtitulo", "Acceso desde IP: 192.168.1.1", "tiempo", "13:45", "detalle", "- PRINCIPAL ARCHITECT", "resumen", "M. Valdes inicio sesion desde Lima, PE"),
                obj("id", "log_004", "tipo", "actualizacion", "nivel", "info", "titulo", "Nueva Agencia", "subtitulo", "Inmobiliaria del Este", "tiempo", "12:10", "detalle", "- ADMIN_SYSTEM", "resumen", "Cambio de politica en The Editorial Estate"),
                obj("id", "log_005", "tipo", "usuario", "nivel", "exito", "titulo", "Registro de Usuario", "subtitulo", "Validacion de Correo Completada", "tiempo", "11:55", "detalle", "- USER_ID: 8824", "resumen", "E. Ramos registro un nuevo auditor regional")
        );
    }

    private JSONArray seedTramites() {
        return new JSONArray(); // vacío — se llena cuando el cliente hace una separación
    }

    private JSONArray seedHistorial() {
        return new JSONArray(); // vacío — se llena cuando el cliente completa operaciones
    }

    private JSONArray readArray(String key) {
        try {
            QuerySnapshot snapshot = Tasks.await(
                    firestore.collection(key).get(),
                    FIRESTORE_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS
            );
            if (snapshot != null && !snapshot.isEmpty()) {
                JSONArray array = new JSONArray();
                for (DocumentSnapshot document : snapshot.getDocuments()) {
                    Map<String, Object> data = document.getData();
                    if (data != null) {
                        array.put(new JSONObject(data));
                    }
                }
                sharedPreferences.edit().putString(key, array.toString()).apply();
                return array;
            }
        } catch (Exception ignored) {
            // Firestore queda como fuente principal; SharedPreferences solo cubre demo/offline.
        }

        String raw = sharedPreferences.getString(key, "[]");
        try {
            return new JSONArray(raw);
        } catch (JSONException ignored) {
            sharedPreferences.edit().putString(key, "[]").apply();
            return new JSONArray();
        }
    }

    private void persistArray(String key, JSONArray array) {
        sharedPreferences.edit().putString(key, array.toString()).apply();
        try {
            WriteBatch batch = firestore.batch();
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                if (object == null) {
                    continue;
                }
                String docId = firestoreDocumentId(key, object, i);
                batch.set(
                        firestore.collection(key).document(docId),
                        jsonObjectToMap(object),
                        SetOptions.merge()
                );
            }
            batch.commit();
        } catch (Exception ignored) {
            // La cache visual ya quedo actualizada; la siguiente sincronizacion puede reintentar.
        }
    }

    private String firestoreDocumentId(String collection, JSONObject object, int index) {
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
        java.util.HashMap<String, Object> map = new java.util.HashMap<>();
        java.util.Iterator<String> keys = object.keys();
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

    private String slug(String value) {
        if (value == null) {
            return "doc_" + System.currentTimeMillis();
        }
        String slug = value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        return slug.isEmpty() ? "doc_" + System.currentTimeMillis() : slug;
    }

    private JSONObject obj(Object... values) {
        JSONObject object = new JSONObject();
        for (int i = 0; i + 1 < values.length; i += 2) {
            try {
                object.put(String.valueOf(values[i]), values[i + 1]);
            } catch (JSONException ignored) {
                // Seed data is controlled by the app.
            }
        }
        return object;
    }

    private JSONArray array(JSONObject... objects) {
        JSONArray array = new JSONArray();
        for (JSONObject object : objects) {
            array.put(object);
        }
        return array;
    }

    private JSONArray arrayStrings(String... values) {
        JSONArray array = new JSONArray();
        for (String value : values) {
            array.put(value);
        }
        return array;
    }

    private List<String> stringList(JSONArray array) {
        if (array == null) {
            return new ArrayList<>();
        }
        List<String> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            list.add(array.optString(i));
        }
        return list;
    }

    private String relativeDate(int dayOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, dayOffset);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
    }

    private String roleLabel(String role) {
        if ("admin".equals(role)) {
            return "Admin Inmobiliaria";
        }
        if ("asesor".equals(role)) {
            return "Asesor de Ventas";
        }
        if ("cliente".equals(role)) {
            return "Cliente";
        }
        return "Superadmin";
    }

    private AdminNotificationItem.Type adminNotificationType(String type) {
        if ("payment".equals(type)) {
            return AdminNotificationItem.Type.PAYMENT;
        }
        if ("separation".equals(type)) {
            return AdminNotificationItem.Type.SEPARATION;
        }
        return AdminNotificationItem.Type.ACTION;
    }

    private int logColor(String level) {
        if ("critico".equals(level)) {
            return ContextCompat.getColor(context, R.color.sa_danger);
        }
        if ("alerta".equals(level)) {
            return ContextCompat.getColor(context, R.color.sa_gold);
        }
        return ContextCompat.getColor(context, R.color.sa_dark);
    }

    private int logIcon(String type) {
        if ("alerta".equals(type)) {
            return android.R.drawable.stat_notify_error;
        }
        if ("pago".equals(type)) {
            return android.R.drawable.ic_menu_save;
        }
        if ("actualizacion".equals(type)) {
            return android.R.drawable.ic_menu_manage;
        }
        if ("usuario".equals(type)) {
            return android.R.drawable.ic_menu_add;
        }
        return android.R.drawable.checkbox_on_background;
    }

    private int amenityIcon(String icon) {
        if ("pool".equals(icon)) {
            return R.drawable.ic_admin_pool;
        }
        if ("laptop".equals(icon)) {
            return R.drawable.ic_admin_laptop;
        }
        if ("gym".equals(icon)) {
            return R.drawable.ic_amenity_gym;
        }
        if ("bbq".equals(icon)) {
            return R.drawable.ic_amenity_bbq;
        }
        if ("terrace".equals(icon)) {
            return R.drawable.ic_amenity_terrace;
        }
        if ("lobby".equals(icon)) {
            return R.drawable.ic_amenity_lobby;
        }
        if ("pet".equals(icon)) {
            return R.drawable.ic_amenity_pet;
        }
        if ("security".equals(icon)) {
            return R.drawable.ic_amenity_security;
        }
        if ("parking".equals(icon)) {
            return R.drawable.ic_amenity_parking;
        }
        if ("bike".equals(icon)) {
            return R.drawable.ic_amenity_bike;
        }
        if ("playground".equals(icon)) {
            return R.drawable.ic_amenity_playground;
        }
        if ("email".equals(icon)) {
            return R.drawable.ic_email;
        }
        return R.drawable.ic_home;
    }

    public void addCita(String clienteId, String clienteNombre, String inmuebleNombre, String fechaTexto, String hora, String meetingPoint, String nota, String asesorNombre, String imageKey) {
        JSONArray citas = readArray(COLLECTION_CITAS);
        try {
            JSONObject newCita = obj(
                    "id", "cita_" + System.currentTimeMillis(),
                    "clienteId", clienteId != null ? clienteId : "",
                    "clienteNombre", clienteNombre != null ? clienteNombre : "",
                    "asesorNombre", asesorNombre != null ? asesorNombre : "Elena Valdes",
                    "inmuebleNombre", inmuebleNombre,
                    "proyectoNombre", "The Editorial Estate",
                    "hora", hora,
                    "fechaTexto", fechaTexto,
                    "dateOffset", 0,
                    "status", "Confirmada",
                    "meetingPoint", meetingPoint != null && !meetingPoint.trim().isEmpty() ? meetingPoint : "Lobby principal",
                    "nota", nota,
                    "imageKey", imageKey != null ? imageKey : "user_featured_house",
                    "hasCierre", false
            );
            citas.put(newCita);
            persistArray(COLLECTION_CITAS, citas);
        } catch (Exception ignored) {}
    }

    public void addChatMessage(String text, boolean sentByMe) {
        JSONArray messages = readArray(COLLECTION_MENSAJES);
        try {
            JSONObject newMsg = obj(
                    "id", "msg_" + System.currentTimeMillis(),
                    "text", text,
                    "time", new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new java.util.Date()),
                    "sentByMe", sentByMe
            );
            messages.put(newMsg);
            persistArray(COLLECTION_MENSAJES, messages);
        } catch (Exception ignored) {}
    }

    public String addTramite(String clienteId, String propertyTitle, String amount) {
        JSONArray tramites = readArray(COLLECTION_TRAMITES);
        try {
            String tramiteId = "#TE-" + (10000 + (int)(Math.random() * 89999));
            String fecha = new SimpleDateFormat("dd MMM yyyy", new java.util.Locale("es", "PE"))
                    .format(new java.util.Date());
            JSONObject newTramite = obj(
                    "id", "tram_" + System.currentTimeMillis(),
                    "clienteId", clienteId != null ? clienteId : "",
                    "title", propertyTitle,
                    "code", "ID de tramite: " + tramiteId,
                    "status", "EN REVISION",
                    "note", "Verificacion de documentos",
                    "due", "Revision en curso, sin vencimiento inmediato.",
                    "canPay", false,
                    "amount", amount,
                    "fecha", fecha
            );
            tramites.put(newTramite);
            persistArray(COLLECTION_TRAMITES, tramites);
            return tramiteId;
        } catch (Exception ignored) {
            return "";
        }
    }

    public void addHistorial(String clienteId, String propertyTitle, String amount, String tramiteId) {
        JSONArray historial = readArray(COLLECTION_HISTORIAL);
        try {
            String fecha = new SimpleDateFormat("dd MMM yyyy", new java.util.Locale("es", "PE"))
                    .format(new java.util.Date()) + " - Confirmacion emitida";
            String opCode = "OP-" + new SimpleDateFormat("yyyy", Locale.getDefault())
                    .format(new java.util.Date()) + "-" + (10000 + (int)(Math.random() * 89999));
            JSONObject newItem = obj(
                    "id", "hist_" + System.currentTimeMillis(),
                    "clienteId", clienteId != null ? clienteId : "",
                    "badge", "RESERVA COMPLETADA",
                    "title", propertyTitle,
                    "date", fecha,
                    "summary", "Separacion registrada. Monto abonado y documentos en proceso de liberacion.",
                    "status", "COMPLETADO",
                    "code", opCode,
                    "amount", amount
            );
            historial.put(newItem);
            persistArray(COLLECTION_HISTORIAL, historial);
        } catch (Exception ignored) {}
    }

    private JSONObject projectToJson(String projectId, AdminProjectDraft draft) {
        String projectName = safeProjectName(draft);
        String address = isEmpty(draft.getAddress()) ? "Direccion pendiente" : draft.getAddress();
        String district = isEmpty(draft.getCity()) ? "Polanco" : draft.getCity();
        return obj(
                "id", projectId,
                "propertyId", projectId,
                "nombre", projectName,
                "descripcion", isEmpty(draft.getDescription()) ? "Proyecto inmobiliario gestionado localmente." : draft.getDescription(),
                "direccion", address,
                "distrito", district,
                "precioDesde", priceFromDraft(draft),
                "estadoComercial", toStorageStatus(draft.getStatus()),
                "badge", "CREADO LOCALMENTE",
                "imageKey", "sa_profile_admin",
                "userImageKey", "user_featured_house",
                "assignmentStatus", "ACTIVO",
                "fechaEntrega", isEmpty(draft.getDeliveryDate()) ? "Pendiente" : draft.getDeliveryDate(),
                "mapLabel", isEmpty(draft.getMapLabel()) ? "Mapa: " + address : draft.getMapLabel()
        );
    }

    private void saveDraftProjectCollections(String projectId, AdminProjectDraft draft) {
        JSONArray typologies = new JSONArray();
        for (AdminProjectFormTypologyItem item : draft.getTypologies()) {
            typologies.put(obj(
                    "projectId", projectId,
                    "title", item.getTitle(),
                    "available", item.isAvailable(),
                    "area", item.getArea(),
                    "bedrooms", item.getBedrooms(),
                    "bathrooms", item.getBathrooms(),
                    "totalAmount", normalizeUsdAmount(item.getTotalAmount()),
                    "separationAmount", normalizeUsdAmount(item.getSeparationAmount())
            ));
        }

        JSONArray amenities = new JSONArray();
        for (AdminProjectFormAmenityItem item : draft.getAmenities()) {
            amenities.put(obj(
                    "projectId", projectId,
                    "title", item.getTitle(),
                    "icon", amenityIconKey(item.getTitle()),
                    "selected", item.isSelected()
            ));
        }

        persistArray(COLLECTION_TIPOLOGIAS, typologies);
        persistArray(COLLECTION_AMENIDADES, amenities);
    }

    private String safeProjectName(AdminProjectDraft draft) {
        return draft == null || isEmpty(draft.getProjectName()) ? "Proyecto sin nombre" : draft.getProjectName();
    }

    private String priceFromDraft(AdminProjectDraft draft) {
        if (draft == null || draft.getTypologies().isEmpty()) {
            return "USD 0";
        }
        String totalAmount = draft.getTypologies().get(0).getTotalAmount();
        if (isEmpty(totalAmount)) {
            return "USD 0";
        }
        return normalizeUsdAmount(totalAmount);
    }

    private String normalizeUsdAmount(String rawAmount) {
        String value = rawAmount == null ? "" : rawAmount.trim();
        if (value.isEmpty()) {
            return "0 USD";
        }
        return value.toUpperCase(Locale.ROOT).contains("USD") ? value : value + " USD";
    }

    private String amenityIconKey(String title) {
        String normalized = title == null ? "" : title.toLowerCase(Locale.ROOT);
        if (normalized.contains("cowork")) return "laptop";
        if (normalized.contains("pisc")) return "pool";
        if (normalized.contains("gim")) return "gym";
        if (normalized.contains("bbq") || normalized.contains("parr")) return "bbq";
        if (normalized.contains("pet")) return "pet";
        if (normalized.contains("seguridad")) return "security";
        if (normalized.contains("estacion")) return "parking";
        if (normalized.contains("bici")) return "bike";
        if (normalized.contains("terraza")) return "terrace";
        if (normalized.contains("juegos")) return "playground";
        if (normalized.contains("lobby") || normalized.contains("lounge")) return "lobby";
        return "home";
    }

    private String toStorageStatus(String status) {
        if ("En preventa".equalsIgnoreCase(status)) {
            return "EN PREVENTA";
        }
        if ("En venta".equalsIgnoreCase(status)) {
            return "EN VENTA";
        }
        return "EN PLANOS";
    }

    private String fromStorageStatus(String status) {
        if ("EN PREVENTA".equalsIgnoreCase(status)) {
            return "En preventa";
        }
        if ("EN VENTA".equalsIgnoreCase(status)) {
            return "En venta";
        }
        return "En planos";
    }

    private void upsertAdvisorFromRequest(JSONObject request) {
        JSONArray usuarios = readArray(COLLECTION_USUARIOS);
        String email = request.optString("email").toLowerCase(Locale.ROOT);
        for (int i = 0; i < usuarios.length(); i++) {
            JSONObject user = usuarios.optJSONObject(i);
            if (user == null || !email.equals(user.optString("email").toLowerCase(Locale.ROOT))) {
                continue;
            }
            try {
                user.put("rol", "asesor");
                user.put("estado", "activo");
                usuarios.put(i, user);
                persistArray(COLLECTION_USUARIOS, usuarios);
            } catch (JSONException ignored) {}
            return;
        }

        String fullName = request.optString("nombre", "Asesor Local");
        String nombres = fullName;
        String apellidos = "";
        int spaceIndex = fullName.indexOf(' ');
        if (spaceIndex != -1) {
            nombres = fullName.substring(0, spaceIndex);
            apellidos = fullName.substring(spaceIndex + 1);
        }

        usuarios.put(obj(
                "id", "usr_asesor_local_" + System.currentTimeMillis(),
                "rol", "asesor",
                "nombres", nombres,
                "apellidos", apellidos,
                "email", email,
                "password", "asesor123",
                "telefono", "+51 000 000 000",
                "estado", "activo",
                "avatarKey", "sa_profile_asesor_1",
                "rating", "4.8",
                "inmobiliariaId", "inmo_editorial",
                "inmobiliariaNombre", request.optString("inmobiliariaNombre", "The Editorial Estate"),
                "proyectosAsignados", arrayStrings(request.optString("proyectoNombre", "Proyecto local"))
        ));
        persistArray(COLLECTION_USUARIOS, usuarios);
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (!isEmpty(value)) {
                return value.trim();
            }
        }
        return "";
    }

    public void addAdminNotification(String id, String type, String title, String badge, String line1, String line2, String actionText) {
        JSONArray notifications = readArray(COLLECTION_NOTIFICACIONES);
        try {
            notifications.put(obj(
                    "id", id,
                    "recipientRole", "admin",
                    "tipo", isEmpty(type) ? "action" : type,
                    "section", "today",
                    "titulo", title,
                    "badge", badge,
                    "line1", line1,
                    "line2", line2,
                    "actionText", actionText
            ));
            persistArray(COLLECTION_NOTIFICACIONES, notifications);
        } catch (Exception ignored) {}
    }

    public void addUsuario(String fullName, String email, String phone) {
        addUsuarioAndGetId(fullName, email, phone, "");
    }

    public String addUsuarioAndGetId(String fullName, String email, String phone, String password) {
        JSONArray usuarios = readArray(COLLECTION_USUARIOS);
        try {
            String nombres = fullName;
            String apellidos = "";
            int spaceIndex = fullName.indexOf(' ');
            if (spaceIndex != -1) {
                nombres = fullName.substring(0, spaceIndex);
                apellidos = fullName.substring(spaceIndex + 1);
            }
            String newId = "usr_cliente_" + System.currentTimeMillis();
            JSONObject newUser = obj(
                    "id", newId,
                    "rol", "cliente",
                    "nombres", nombres,
                    "apellidos", apellidos,
                    "email", email,
                    "password", password != null ? password : "",
                    "telefono", phone,
                    "estado", "activo",
                    "avatarKey", "sa_profile_user_1",
                    "inmobiliariaNombre", "The Editorial Estate"
            );
            usuarios.put(newUser);
            persistArray(COLLECTION_USUARIOS, usuarios);
            return newId;
        } catch (Exception ignored) {
            return "";
        }
    }

    public JSONObject getUserByCredentials(String email, String password) {
        if (email == null || password == null) return null;
        JSONArray usuarios = readArray(COLLECTION_USUARIOS);
        for (int i = 0; i < usuarios.length(); i++) {
            JSONObject user = usuarios.optJSONObject(i);
            if (user == null) continue;
            if (email.trim().equalsIgnoreCase(user.optString("email"))
                    && password.equals(user.optString("password"))) {
                return user;
            }
        }
        return null;
    }

    public JSONObject getUserById(String userId) {
        if (userId == null || userId.isEmpty()) return null;
        JSONArray usuarios = readArray(COLLECTION_USUARIOS);
        for (int i = 0; i < usuarios.length(); i++) {
            JSONObject user = usuarios.optJSONObject(i);
            if (user != null && userId.equals(user.optString("id"))) {
                return user;
            }
        }
        return null;
    }

    public void updateUsuario(String userId, String fullName, String email, String phone, String city) {
        JSONArray usuarios = readArray(COLLECTION_USUARIOS);
        try {
            for (int i = 0; i < usuarios.length(); i++) {
                JSONObject user = usuarios.optJSONObject(i);
                if (user != null && userId.equals(user.optString("id"))) {
                    String nombres = fullName;
                    String apellidos = "";
                    int spaceIndex = fullName.indexOf(' ');
                    if (spaceIndex != -1) {
                        nombres = fullName.substring(0, spaceIndex);
                        apellidos = fullName.substring(spaceIndex + 1);
                    }
                    user.put("nombres", nombres);
                    user.put("apellidos", apellidos);
                    user.put("email", email);
                    user.put("telefono", phone);
                    user.put("ciudad", city);
                    usuarios.put(i, user);
                    persistArray(COLLECTION_USUARIOS, usuarios);
                    return;
                }
            }
        } catch (Exception ignored) {}
    }

    public void addAdministrador(String fullName, String email, String phone, String agency, String password) {
        JSONArray usuarios = readArray(COLLECTION_USUARIOS);
        try {
            String nombres = fullName;
            String apellidos = "";
            int spaceIndex = fullName.indexOf(' ');
            if (spaceIndex != -1) {
                nombres = fullName.substring(0, spaceIndex);
                apellidos = fullName.substring(spaceIndex + 1);
            }
            String newId = "usr_admin_" + System.currentTimeMillis();
            JSONObject newUser = obj(
                    "id", newId,
                    "rol", "admin",
                    "nombres", nombres,
                    "apellidos", apellidos,
                    "email", email,
                    "password", password != null ? password : "",
                    "telefono", phone,
                    "estado", "activo",
                    "avatarKey", "sa_profile_admin",
                    "inmobiliariaNombre", agency
            );
            usuarios.put(newUser);
            persistArray(COLLECTION_USUARIOS, usuarios);
            
            addLogSistema("usuario", "exito", "Registro de Administrador", "Validacion Completada", "- Admin: " + nombres + " " + apellidos, "Superadmin registro al administrador " + nombres);
        } catch (Exception ignored) {
        }
    }

    public void updateSolicitudAsesorStatus(String email, String newStatus) {
        JSONArray solicitudes = readArray(COLLECTION_SOLICITUDES);
        try {
            for (int i = 0; i < solicitudes.length(); i++) {
                JSONObject solicitud = solicitudes.optJSONObject(i);
                if (solicitud != null && email.equals(solicitud.optString("email"))) {
                    solicitud.put("estado", newStatus);
                    solicitudes.put(i, solicitud);
                    persistArray(COLLECTION_SOLICITUDES, solicitudes);
                    
                    if ("aceptada".equalsIgnoreCase(newStatus)) {
                        addUsuarioAndGetId(solicitud.optString("nombre"), solicitud.optString("email"), "", "asesor123");
                        JSONArray usuarios = readArray(COLLECTION_USUARIOS);
                        for (int j = 0; j < usuarios.length(); j++) {
                            JSONObject user = usuarios.optJSONObject(j);
                            if (user != null && email.equals(user.optString("email"))) {
                                user.put("rol", "asesor");
                                user.put("inmobiliariaNombre", solicitud.optString("inmobiliariaNombre"));
                                user.put("avatarKey", solicitud.optString("avatarKey"));
                                usuarios.put(j, user);
                                break;
                            }
                        }
                        persistArray(COLLECTION_USUARIOS, usuarios);
                    }
                    
                    addLogSistema("actualizacion", "info", "Solicitud " + newStatus, "Asesor: " + solicitud.optString("nombre"), "- Email: " + email, "Superadmin " + newStatus + " la solicitud de asesor");
                    return;
                }
            }
        } catch (Exception ignored) {}
    }

    public void addLogSistema(String tipo, String nivel, String titulo, String subtitulo, String detalle, String resumen) {
        JSONArray logs = readArray(COLLECTION_LOGS);
        try {
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new java.util.Date());
            JSONObject newLog = obj(
                    "id", "log_" + System.currentTimeMillis(),
                    "tipo", tipo,
                    "nivel", nivel,
                    "titulo", titulo,
                    "subtitulo", subtitulo,
                    "tiempo", time,
                    "detalle", detalle,
                    "resumen", resumen
            );
            JSONArray newLogsArray = new JSONArray();
            newLogsArray.put(newLog);
            for (int i = 0; i < logs.length(); i++) {
                newLogsArray.put(logs.getJSONObject(i));
            }
            persistArray(COLLECTION_LOGS, newLogsArray);
        } catch (Exception ignored) {}
    }

    private int imageRes(String key) {
        if ("sa_avatar_01".equals(key)) return R.drawable.sa_avatar_01;
        if ("sa_avatar_02".equals(key)) return R.drawable.sa_avatar_02;
        if ("sa_avatar_03".equals(key)) return R.drawable.sa_avatar_03;
        if ("sa_avatar_04".equals(key)) return R.drawable.sa_avatar_04;
        if ("sa_avatar_05".equals(key)) return R.drawable.sa_avatar_05;
        if ("sa_avatar_06".equals(key)) return R.drawable.sa_avatar_06;
        if ("sa_avatar_07".equals(key)) return R.drawable.sa_avatar_07;
        if ("sa_avatar_08".equals(key)) return R.drawable.sa_avatar_08;
        if ("sa_avatar_09".equals(key)) return R.drawable.sa_avatar_09;
        if ("sa_profile_admin".equals(key)) return R.drawable.sa_profile_admin;
        if ("sa_profile_asesor_1".equals(key)) return R.drawable.sa_profile_asesor_1;
        if ("sa_profile_asesor_2".equals(key)) return R.drawable.sa_profile_asesor_2;
        if ("sa_profile_asesor_3".equals(key)) return R.drawable.sa_profile_asesor_3;
        if ("sa_profile_user_1".equals(key)) return R.drawable.sa_profile_user_1;
        if ("sa_profile_user_2".equals(key)) return R.drawable.sa_profile_user_2;
        if ("as_property_04".equals(key)) return R.drawable.as_property_04;
        if ("as_property_05".equals(key)) return R.drawable.as_property_05;
        if ("as_property_06".equals(key)) return R.drawable.as_property_06;
        if ("as_property_07".equals(key)) return R.drawable.as_property_07;
        if ("user_popular_1".equals(key)) return R.drawable.user_popular_1;
        if ("user_popular_2".equals(key)) return R.drawable.user_popular_2;
        if ("user_featured_house".equals(key)) return R.drawable.user_featured_house;
        if ("user_property_hero_real".equals(key)) return R.drawable.user_property_hero_real;
        return 0;
    }

    private int fallbackImageRes(String key) {
        int res = imageRes(key);
        return res == 0 ? R.drawable.user_featured_house : res;
    }
}
