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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LocalSchemaStorage {
    private static final String PREFS_NAME = "iot_local_schema_storage";
    private static final String KEY_INITIALIZED = "initialized_v5";

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

    public LocalSchemaStorage(Context context) {
        this.context = context.getApplicationContext();
        this.sharedPreferences = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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
                    "activo".equalsIgnoreCase(user.optString("estado")),
                    user.optString("fechaRegistro", "")
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
                    request.optString("estado").toUpperCase(Locale.ROOT),
                    request.optString("fechaRegistro", "")
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
            String fechaCorta = shortDateFromIso(log.optString("fechaIso", ""));
            String marcaTiempo = fechaCorta.isEmpty()
                    ? log.optString("tiempo")
                    : fechaCorta + " · " + log.optString("tiempo");
            items.add(new SuperadminResumenLogItem(
                    log.optString("tipo").toUpperCase(Locale.ROOT) + "      " + marcaTiempo,
                    log.optString("resumen"),
                    color,
                    color,
                    log.optString("fechaIso", "")
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
                    log.optString("fechaIso", ""),
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
            sharedPreferences.edit().putString(COLLECTION_PROYECTOS, projects.toString()).apply();
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
            sharedPreferences.edit().putString(COLLECTION_PROYECTOS, projects.toString()).apply();
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
            sharedPreferences.edit().putString(COLLECTION_SOLICITUDES, requests.toString()).apply();
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
                sharedPreferences.edit().putString(COLLECTION_SOLICITUDES, requests.toString()).apply();

                if ("aceptada".equals(normalizedStatus)) {
                    upsertAdvisorFromRequest(request);
                }

                String advisorName = request.optString("nombre", "Asesor");
                addAdminNotification(
                        "request_" + normalizedStatus + "_" + requestId + "_" + System.currentTimeMillis(),
                        "action",
                        "Solicitud " + ("aceptada".equals(normalizedStatus) ? "aceptada" : "rechazada"),
                        "Hace un momento",
                        advisorName,
                        "Proyecto: " + request.optString("proyectoNombre", "Sin proyecto"),
                        "VER SOLICITUDES"
                );
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
                items.add(new AdminProjectGalleryItem(imageRes(image.optString("imageKey"))));
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
            String dateVal = cita.optString("fechaTexto", "");
            if (dateVal.isEmpty()) {
                dateVal = relativeDate(cita.optInt("dateOffset", 0));
            }
            Cita item = new Cita(
                    cita.optString("id"),
                    cita.optString("clienteNombre"),
                    cita.optString("inmuebleNombre"),
                    cita.optString("hora"),
                    dateVal,
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

    public Cita getCitaById(String id) {
        if (id == null) return null;
        for (Cita cita : getAdvisorCitas()) {
            if (id.equals(cita.getId())) {
                return cita;
            }
        }
        return null;
    }

    public boolean updateCitaStatusAndDetails(String id, String status, String date, String time) {
        JSONArray citas = readArray(COLLECTION_CITAS);
        boolean updated = false;
        for (int i = 0; i < citas.length(); i++) {
            JSONObject cita = citas.optJSONObject(i);
            if (cita != null && id.equals(cita.optString("id"))) {
                try {
                    if (status != null) {
                        cita.put("estado", status);
                    }
                    if (date != null) {
                        cita.put("fechaTexto", date);
                        cita.put("dateOffset", 0);
                    }
                    if (time != null) {
                        cita.put("hora", time);
                    }
                    citas.put(i, cita);
                    updated = true;
                    break;
                } catch (JSONException ignored) {}
            }
        }
        if (updated) {
            sharedPreferences.edit().putString(COLLECTION_CITAS, citas.toString()).apply();
        }
        return updated;
    }

    public void addEventoCita(EventoCita event) {
        if (event == null) return;
        JSONArray events = readArray(COLLECTION_EVENTOS_CITA);
        try {
            JSONObject newEvent = obj(
                    "id", event.getId(),
                    "citaId", event.getCitaId(),
                    "titulo", event.getTitulo(),
                    "detalle", event.getDetalle(),
                    "fechaHora", event.getFechaHora(),
                    "tipo", event.getTipo()
            );
            events.put(newEvent);
            sharedPreferences.edit().putString(COLLECTION_EVENTOS_CITA, events.toString()).apply();
        } catch (Exception ignored) {}
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
            JSONObject separacion = separaciones.optJSONObject(i);
            if (separacion == null) {
                continue;
            }
            items.add(new Separacion(
                    separacion.optString("id"),
                    separacion.optString("clienteNombre"),
                    separacion.optString("inmuebleNombre"),
                    separacion.optString("montoTexto"),
                    separacion.optString("fechaTexto"),
                    imageRes(separacion.optString("imageKey")),
                    separacion.optString("estado")
            ));
        }
        return items;
    }

    public Separacion getSeparacionById(String id) {
        if (id == null) return null;
        for (Separacion sep : getAdvisorSeparaciones()) {
            if (id.equals(sep.getId())) {
                return sep;
            }
        }
        return null;
    }

    public void addSeparacion(Separacion sep) {
        if (sep == null) return;
        JSONArray separaciones = readArray(COLLECTION_SEPARACIONES);
        try {
            JSONObject newSep = obj(
                    "id", sep.getId(),
                    "clienteNombre", sep.getClientName(),
                    "inmuebleNombre", sep.getPropertyName(),
                    "montoTexto", sep.getPrice(),
                    "fechaTexto", sep.getDate(),
                    "estado", sep.getStatus(),
                    "imageKey", "as_property_01"
            );
            separaciones.put(newSep);
            sharedPreferences.edit().putString(COLLECTION_SEPARACIONES, separaciones.toString()).apply();
        } catch (Exception ignored) {}
    }

    public boolean updateSeparacionStatus(String id, String status) {
        JSONArray separaciones = readArray(COLLECTION_SEPARACIONES);
        boolean updated = false;
        for (int i = 0; i < separaciones.length(); i++) {
            JSONObject sep = separaciones.optJSONObject(i);
            if (sep != null && id.equals(sep.optString("id"))) {
                try {
                    sep.put("estado", status);
                    separaciones.put(i, sep);
                    updated = true;
                    break;
                } catch (JSONException ignored) {}
            }
        }
        if (updated) {
            sharedPreferences.edit().putString(COLLECTION_SEPARACIONES, separaciones.toString()).apply();
        }
        return updated;
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
        for (int i = 0; i < projects.length() && items.size() < 5; i++) {
            JSONObject project = projects.optJSONObject(i);
            if (project == null) {
                continue;
            }
            items.add(new UsuarioPropertyListItem(
                    project.optString("propertyId", "fallback_property"),
                    project.optString("badge"),
                    project.optString("nombre"),
                    project.optString("direccion"),
                    project.optString("precioDesde"),
                    imageRes(project.optString("userImageKey", project.optString("imageKey")))
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
                    cita.optString("estado").toUpperCase(Locale.ROOT),
                    cita.optString("fechaTexto") + ", " + cita.optString("hora"),
                    cita.optString("asesorNombre"),
                    imageRes(cita.optString("imageKey")),
                    cita.optString("meetingPoint"),
                    cita.optString("nota"),
                    "Confirmada".equalsIgnoreCase(cita.optString("estado"))
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
                    chat.optString("id"),           // chatId — nuevo campo
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

    /**
     * Retorna los mensajes de una conversación específica.
     * Si chatId es null o vacío, retorna los mensajes del seed global.
     */
    public List<MensajeChat> getChatMessages(String chatId) {
        List<MensajeChat> items = new ArrayList<>();
        JSONArray messages = readArray(COLLECTION_MENSAJES);
        for (int i = 0; i < messages.length(); i++) {
            JSONObject message = messages.optJSONObject(i);
            if (message == null) continue;

            // Filtra por chatId si viene; si el mensaje no tiene chatId (seed), lo muestra solo si no hay chatId específico
            String msgChatId = message.optString("chatId", "");
            if (chatId != null && !chatId.isEmpty()) {
                if (!msgChatId.isEmpty() && !chatId.equals(msgChatId)) continue;
                if (msgChatId.isEmpty()) continue; // no mostrar seed global en chats específicos
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

    public List<UsuarioNotificationItem> getUserNotifications(String clienteId) {
        List<UsuarioNotificationItem> items = new ArrayList<>();
        JSONArray notifications = readArray(COLLECTION_NOTIFICACIONES);
        // Carga las citas del cliente para enriquecer notificaciones de tipo "visit"
        JSONArray citas = readArray(COLLECTION_CITAS);

        for (int i = notifications.length() - 1; i >= 0; i--) {
            JSONObject notification = notifications.optJSONObject(i);
            if (notification == null || !"cliente".equals(notification.optString("recipientRole"))) {
                continue;
            }
            // Si tiene clienteId, filtra por él; si no (seed global), lo muestra a todos
            String notifClienteId = notification.optString("clienteId", "");
            if (!notifClienteId.isEmpty() && clienteId != null
                    && !clienteId.isEmpty() && !clienteId.equals(notifClienteId)) {
                continue;
            }

            UsuarioNotificationItem item = new UsuarioNotificationItem(
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
            );

            // Si es notificación de cita, busca la cita más reciente del cliente
            // para pasar datos reales al detalle
              if (item.getActionType() == UsuarioNotificationItem.ACTION_APPOINTMENT) {
                  JSONObject citaMatch = findLastCitaForCliente(citas, clienteId);
                  if (citaMatch != null) {
                      item.setCitaData(
                              citaMatch.optString("inmuebleNombre"),
                            citaMatch.optString("estado", "Confirmada").toUpperCase(Locale.ROOT),
                            citaMatch.optString("fechaTexto") + ", " + citaMatch.optString("hora"),
                            citaMatch.optString("asesorNombre"),
                            citaMatch.optString("meetingPoint"),
                            citaMatch.optString("nota"),
                              "Confirmada".equalsIgnoreCase(citaMatch.optString("estado"))
                      );
                  }
              } else if (item.getActionType() == UsuarioNotificationItem.ACTION_PAYMENT) {
                  item.setTramiteData(
                          notification.optString("tramiteTitle"),
                          notification.optString("tramiteCode"),
                          notification.optString("tramiteStatus"),
                          notification.optString("tramiteNote"),
                          notification.optString("tramiteDue"),
                          notification.optBoolean("tramiteCanPay", false)
                  );
              }

            items.add(item);
        }
        return items;
    }

    /** Retorna la cita más reciente del cliente (último elemento del array). */
    private JSONObject findLastCitaForCliente(JSONArray citas, String clienteId) {
        if (citas == null || clienteId == null || clienteId.isEmpty()) return null;
        for (int i = citas.length() - 1; i >= 0; i--) {
            JSONObject cita = citas.optJSONObject(i);
            if (cita != null && clienteId.equals(cita.optString("clienteId"))) {
                return cita;
            }
        }
        return null;
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
                obj("id", "usr_super_001", "rol", "superadmin", "nombres", "Julian", "apellidos", "Reed", "email", "superadmin@estate.pe", "password", "super123", "estado", "activo", "avatarKey", "sa_avatar_07", "inmobiliariaNombre", "Sistema", "fechaRegistro", relativeDate(-30)),
                obj("id", "usr_admin_001", "rol", "admin", "nombres", "Administrador", "apellidos", "Editorial", "email", "admin@editorialestate.com", "password", "admin123", "telefono", "+51 987 654 321", "estado", "activo", "avatarKey", "sa_profile_admin", "inmobiliariaId", "inmo_editorial", "inmobiliariaNombre", "The Editorial Estate", "fechaRegistro", relativeDate(-18)),
                obj("id", "usr_asesor_001", "rol", "asesor", "nombres", "Elena", "apellidos", "Valdes", "email", "evaldes@editorialestate.com", "password", "asesor123", "telefono", "+51 987 111 222", "estado", "activo", "avatarKey", "sa_profile_asesor_1", "rating", "5.0", "inmobiliariaId", "inmo_editorial", "inmobiliariaNombre", "The Editorial Estate", "proyectosAsignados", arrayStrings("Catalina Sky View", "Villa Luminara"), "fechaRegistro", relativeDate(-12)),
                obj("id", "usr_asesor_002", "rol", "asesor", "nombres", "Julian", "apellidos", "Costa", "email", "jcosta@editorialestate.com", "password", "asesor123", "telefono", "+51 987 222 333", "estado", "inactivo", "avatarKey", "sa_profile_asesor_3", "rating", "4.8", "inmobiliariaId", "inmo_editorial", "inmobiliariaNombre", "The Editorial Estate", "proyectosAsignados", arrayStrings("The Iron Works"), "fechaRegistro", relativeDate(-9)),
                obj("id", "usr_asesor_003", "rol", "asesor", "nombres", "Sofia", "apellidos", "Mendez", "email", "smendez@editorialestate.com", "password", "asesor123", "telefono", "+51 987 333 444", "estado", "activo", "avatarKey", "sa_profile_asesor_2", "rating", "4.9", "inmobiliariaId", "inmo_editorial", "inmobiliariaNombre", "The Editorial Estate", "proyectosAsignados", arrayStrings("Refugio Celeste", "Casa Meridian"), "fechaRegistro", relativeDate(-6)),
                obj("id", "usr_cliente_001", "rol", "cliente", "nombres", "Alicia", "apellidos", "Velarde", "email", "alicia.velarde@mail.com", "password", "cliente123", "telefono", "+51 987 456 210", "estado", "activo", "avatarKey", "sa_profile_user_1", "inmobiliariaNombre", "The Editorial Estate", "fechaRegistro", relativeDate(-4)),
                obj("id", "usr_cliente_002", "rol", "cliente", "nombres", "Julian", "apellidos", "Mendoza", "email", "julian.mendoza@mail.com", "password", "cliente123", "telefono", "+51 987 456 211", "estado", "activo", "avatarKey", "sa_profile_user_2", "inmobiliariaNombre", "The Editorial Estate", "fechaRegistro", relativeDate(-2))
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
                obj("projectId", "proy_001", "title", "Tipo A", "available", true, "area", "70 m2", "bedrooms", "2 habs", "totalAmount", "350,000 USD", "separationAmount", "1,500 USD"),
                obj("projectId", "proy_001", "title", "Tipo B", "available", false, "area", "80 m2", "bedrooms", "3 habs", "totalAmount", "400,000 USD", "separationAmount", "1,700 USD"),
                obj("projectId", "proy_001", "title", "Tipo C", "available", true, "area", "60 m2", "bedrooms", "1 hab", "totalAmount", "310,000 USD", "separationAmount", "1,400 USD"),
                obj("projectId", "proy_001", "title", "Tipo D", "available", true, "area", "95 m2", "bedrooms", "3 habs", "totalAmount", "410,000 USD", "separationAmount", "1,800 USD")
        );
    }

    private JSONArray seedAmenidades() {
        return array(
                obj("projectId", "proy_001", "title", "Coworking", "icon", "laptop", "selected", true),
                obj("projectId", "proy_001", "title", "Piscina", "icon", "pool", "selected", true),
                obj("projectId", "proy_001", "title", "Terraza", "icon", "home", "selected", false),
                obj("projectId", "proy_001", "title", "Sala lounge", "icon", "email", "selected", true),
                obj("projectId", "proy_001", "title", "Gym", "icon", "laptop", "selected", false),
                obj("projectId", "proy_001", "title", "Zona BBQ", "icon", "email", "selected", true)
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
                obj("id", "sol_001", "nombre", "Elena Valdes", "email", "evaldes@editorialestate.com", "subtitle", "Registro enviado hoy", "descripcion", "Solicita unirse a The Editorial Estate como asesora inmobiliaria.", "estado", "pendiente", "avatarKey", "sa_profile_asesor_1", "inmobiliariaNombre", "The Editorial Estate", "fechaRegistro", relativeDate(-1)),
                obj("id", "sol_002", "nombre", "Sofia Mendez", "email", "smendez@editorialestate.com", "subtitle", "Aceptada hace 2 dias", "descripcion", "Perfil aprobado para integrarse al equipo comercial.", "estado", "aceptada", "avatarKey", "sa_profile_asesor_2", "inmobiliariaNombre", "The Editorial Estate", "fechaRegistro", relativeDate(-3)),
                obj("id", "sol_003", "nombre", "Julian Costa", "email", "jcosta@editorialestate.com", "subtitle", "Registro enviado ayer", "descripcion", "Solicita habilitar acceso para gestionar proyectos activos.", "estado", "pendiente", "avatarKey", "sa_profile_asesor_3", "inmobiliariaNombre", "The Editorial Estate", "fechaRegistro", relativeDate(-2))
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
                obj("id", "ASP-294", "clienteNombre", "Hugo Pena", "inmuebleNombre", "Villa Luminara", "montoTexto", "$950,000", "fechaTexto", "12 Oct 2026", "estado", "Pendiente", "imageKey", "as_property_04"),
                obj("id", "ASP-288", "clienteNombre", "Pilar Ortiz", "inmuebleNombre", "The Iron Works", "montoTexto", "$1,200,000", "fechaTexto", "10 Oct 2026", "estado", "Pendiente", "imageKey", "as_property_05"),
                obj("id", "ASP-183", "clienteNombre", "Antonio Ruiz", "inmuebleNombre", "Refugio Celeste", "montoTexto", "$340,000", "fechaTexto", "08 Oct 2026", "estado", "Pendiente", "imageKey", "as_property_06"),
                obj("id", "ASP-150", "clienteNombre", "Maria Garcia", "inmuebleNombre", "Casa Meridian", "montoTexto", "$520,000", "fechaTexto", "05 Oct 2026", "estado", "Aprobado", "imageKey", "as_property_07")
        );
    }

    private JSONArray seedConversaciones() {
        return array(
                obj("id", "chat_001", "viewFor", "asesor", "nombre", "Julian Mendoza", "lastMessage", "El piso del entrepiso se ve...", "time", "14:02 PM", "avatarKey", "sa_profile_user_1", "initials", "", "unread", true),
                obj("id", "chat_002", "viewFor", "asesor", "nombre", "Elena Rossi", "lastMessage", "Le envio los planos para el...", "time", "AYER", "avatarKey", "sa_profile_user_2", "initials", "", "unread", false),
                obj("id", "chat_003", "viewFor", "asesor", "nombre", "Beatrice H.", "lastMessage", "Image_06ASD486GRE", "time", "MARTES", "avatarKey", "", "initials", "BH", "unread", false),
                obj("id", "chat_101", "viewFor", "cliente", "nombre", "The Editorial Estate", "lastMessage", "Tu cita para Villa Luminara fue confirmada.", "time", "10:24", "avatarKey", "sa_profile_admin", "initials", "TE", "usesInitials", false, "unread", true, "favorite", true),
                obj("id", "chat_102", "viewFor", "cliente", "nombre", "Elena Valdes", "lastMessage", "Puedo ayudarte con el detalle de la separacion.", "time", "AYER", "avatarKey", "sa_profile_asesor_1", "initials", "EV", "usesInitials", false, "unread", false, "favorite", true),
                obj("id", "chat_103", "viewFor", "cliente", "nombre", "Soporte de Pagos", "lastMessage", "Tu metodo de pago esta validado.", "time", "LUNES", "avatarKey", "", "initials", "SP", "usesInitials", true, "unread", false, "favorite", false)
        );
    }

    private JSONArray seedMensajes() {
        return array(
                obj("chatId", "chat_001", "dateHeader", true, "text", "LUNES, 24 DE OCT"),
                obj("id", "msg_001", "chatId", "chat_001", "text", "Hola Julian, vi la propiedad Villa Luminara. El plano se ve muy bien.", "time", "10:14 AM", "sentByMe", false),
                obj("id", "msg_002", "chatId", "chat_001", "text", "Buen dia. El proyecto tiene disponibilidad para visita este sabado.", "time", "10:16 AM", "sentByMe", true),
                obj("id", "msg_003", "chatId", "chat_001", "text", "Me gustaria agendar una visita presencial si es posible.", "time", "10:20 AM", "sentByMe", false),
                obj("id", "msg_004", "chatId", "chat_001", "text", "Claro, tengo un espacio a las 10:30 AM. Te lo separo.", "time", "10:28 AM", "sentByMe", true)
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
                obj("id", "res_001", "clienteNombre", "Alicia Velarde", "fecha", "24 Oct 2026", "fechaIso", relativeDate(-4), "proyectoNombre", "Villa Luminara", "comentario", "La asesora resolvio dudas tecnicas y financieras con claridad.", "rating", "5.0", "avatarKey", "sa_profile_user_1"),
                obj("id", "res_002", "clienteNombre", "Julian Mendoza", "fecha", "21 Oct 2026", "fechaIso", relativeDate(-9), "proyectoNombre", "The Iron Works", "comentario", "Buen seguimiento durante la visita y envio oportuno de planos.", "rating", "4.8", "avatarKey", "sa_profile_user_2"),
                obj("id", "res_003", "clienteNombre", "Maria Garcia", "fecha", "18 Oct 2026", "fechaIso", relativeDate(-15), "proyectoNombre", "Casa Meridian", "comentario", "La separacion fue fluida y el asesor explico los plazos.", "rating", "4.9", "avatarKey", "sa_profile_user_1")
        );
    }

    private JSONArray seedLogs() {
        return array(
                obj("id", "log_001", "tipo", "alerta", "nivel", "critico", "titulo", "Error de Sistema", "subtitulo", "Kernel-Level Exception", "tiempo", "14:20", "fechaIso", relativeDate(0), "detalle", "- ADMIN_042", "resumen", "Error critico detectado en validacion de pago"),
                obj("id", "log_002", "tipo", "pago", "nivel", "alerta", "titulo", "Pago Fallido", "subtitulo", "Ref: TXN-9921-BA", "tiempo", "Hace 5 min", "fechaIso", relativeDate(-1), "detalle", "- USER_ID: 8821", "resumen", "Pago fallido para separacion activa"),
                obj("id", "log_003", "tipo", "acceso", "nivel", "exito", "titulo", "Login Exitoso", "subtitulo", "Acceso desde IP: 192.168.1.1", "tiempo", "13:45", "fechaIso", relativeDate(-2), "detalle", "- PRINCIPAL ARCHITECT", "resumen", "M. Valdes inicio sesion desde Lima, PE"),
                obj("id", "log_004", "tipo", "actualizacion", "nivel", "info", "titulo", "Nueva Agencia", "subtitulo", "Inmobiliaria del Este", "tiempo", "12:10", "fechaIso", relativeDate(-3), "detalle", "- ADMIN_SYSTEM", "resumen", "Cambio de politica en The Editorial Estate"),
                obj("id", "log_005", "tipo", "usuario", "nivel", "exito", "titulo", "Registro de Usuario", "subtitulo", "Validacion de Correo Completada", "tiempo", "11:55", "fechaIso", relativeDate(-4), "detalle", "- USER_ID: 8824", "resumen", "E. Ramos registro un nuevo auditor regional")
        );
    }

    private JSONArray seedTramites() {
        return new JSONArray(); // vacío — se llena cuando el cliente hace una separación
    }

    private JSONArray seedHistorial() {
        return new JSONArray(); // vacío — se llena cuando el cliente completa operaciones
    }

    private JSONArray readArray(String key) {
        String raw = sharedPreferences.getString(key, "[]");
        try {
            return new JSONArray(raw);
        } catch (JSONException ignored) {
            sharedPreferences.edit().putString(key, "[]").apply();
            return new JSONArray();
        }
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

    private String shortDateFromIso(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) {
            return "";
        }
        try {
            java.util.Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(isoDate.trim());
            return new SimpleDateFormat("dd MMM", new Locale("es", "PE")).format(date);
        } catch (java.text.ParseException ignored) {
            return "";
        }
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
                    "estado", "Confirmada",
                    "meetingPoint", meetingPoint != null && !meetingPoint.trim().isEmpty() ? meetingPoint : "Lobby principal",
                    "nota", nota,
                    "imageKey", imageKey != null ? imageKey : "user_featured_house",
                    "hasCierre", false
            );
            citas.put(newCita);
            sharedPreferences.edit().putString(COLLECTION_CITAS, citas.toString()).apply();
        } catch (Exception ignored) {}
    }

    public void addChatMessage(String chatId, String text, boolean sentByMe) {
        JSONArray messages = readArray(COLLECTION_MENSAJES);
        try {
            JSONObject newMsg = obj(
                    "id", "msg_" + System.currentTimeMillis(),
                    "chatId", chatId != null ? chatId : "",
                    "text", text,
                    "time", new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new java.util.Date()),
                    "sentByMe", sentByMe
            );
            messages.put(newMsg);
            sharedPreferences.edit().putString(COLLECTION_MENSAJES, messages.toString()).apply();
        } catch (Exception ignored) {}
    }

    /**
     * Crea un trámite (separación en curso) para el cliente.
     * Aparece en la sección "Separaciones en curso" de Mi Actividad.
     */
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
            sharedPreferences.edit().putString(COLLECTION_TRAMITES, tramites.toString()).apply();
            return tramiteId;
        } catch (Exception ignored) {
            return "";
        }
    }

    /**
     * Crea un registro en el historial del cliente.
     * Aparece en la sección "Historial reciente" de Mi Actividad.
     */
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
            sharedPreferences.edit().putString(COLLECTION_HISTORIAL, historial.toString()).apply();
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
        return totalAmount.toUpperCase(Locale.ROOT).contains("USD") ? totalAmount : "USD " + totalAmount;
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
                sharedPreferences.edit().putString(COLLECTION_USUARIOS, usuarios.toString()).apply();
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
        sharedPreferences.edit().putString(COLLECTION_USUARIOS, usuarios.toString()).apply();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
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
            sharedPreferences.edit().putString(COLLECTION_NOTIFICACIONES, notifications.toString()).apply();
        } catch (Exception ignored) {}
    }
    /**
     * Crea una notificación in-app para el cliente.
     * Aparece en la pantalla de Notificaciones del rol cliente.
     *
     * @param clienteId   ID del cliente destinatario
     * @param tipo        "visit" o "approval"
     * @param titulo      Título de la notificación
     * @param body        Cuerpo del mensaje
     * @param actionText  Texto del botón CTA (puede ser vacío)
     * @param action      "appointment" o "payment"
     */
      public void addNotificacion(String clienteId, String tipo, String titulo,
                                   String body, String actionText, String action) {
          JSONArray notificaciones = readArray(COLLECTION_NOTIFICACIONES);
        try {
            String hora = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                    .format(new java.util.Date());
            JSONObject newNotif = obj(
                    "id", "notif_" + System.currentTimeMillis(),
                    "clienteId", clienteId != null ? clienteId : "",
                    "recipientRole", "cliente",
                    "tipo", tipo,
                    "titulo", titulo,
                    "badge", hora,
                    "body", body,
                    "actionText", actionText != null ? actionText : "",
                    "action", action != null ? action : "appointment"
            );
              notificaciones.put(newNotif);
              sharedPreferences.edit().putString(COLLECTION_NOTIFICACIONES, notificaciones.toString()).apply();
          } catch (Exception ignored) {}
      }

      public void addNotificacionPago(String clienteId, String propertyTitle, String amount, String tramiteId) {
          JSONArray notificaciones = readArray(COLLECTION_NOTIFICACIONES);
          try {
              String hora = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                      .format(new java.util.Date());
              JSONObject newNotif = obj(
                      "id", "notif_" + System.currentTimeMillis(),
                      "clienteId", clienteId != null ? clienteId : "",
                      "recipientRole", "cliente",
                      "tipo", "approval",
                      "titulo", "Separacion en proceso",
                      "badge", hora,
                      "body", "Tu separacion de " + propertyTitle + " por " + amount
                              + " esta en revision. Te notificaremos cuando sea aprobada.",
                      "actionText", "VER TRAMITE",
                      "action", "payment",
                      "tramiteTitle", propertyTitle,
                      "tramiteCode", "ID de tramite: " + tramiteId,
                      "tramiteStatus", "EN REVISION",
                      "tramiteNote", "Verificacion de documentos",
                      "tramiteDue", "Revision en curso, sin vencimiento inmediato.",
                      "tramiteCanPay", false
              );
              notificaciones.put(newNotif);
              sharedPreferences.edit().putString(COLLECTION_NOTIFICACIONES, notificaciones.toString()).apply();
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
            sharedPreferences.edit().putString(COLLECTION_USUARIOS, usuarios.toString()).apply();
            return newId;
        } catch (Exception ignored) {
            return "";
        }
    }

    /**
     * Busca un usuario por email y contraseña.
     * Retorna el JSONObject del usuario si las credenciales son correctas, null si no.
     */
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
                    sharedPreferences.edit().putString(COLLECTION_USUARIOS, usuarios.toString()).apply();
                    return;
                }
            }
        } catch (Exception ignored) {}
    }

    public void updateAdvisorProfile(String userId, String fullName, String email, String phone, String bio, String cargo) {
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
                    user.put("bio", bio);
                    user.put("cargo", cargo);
                    usuarios.put(i, user);
                    sharedPreferences.edit().putString(COLLECTION_USUARIOS, usuarios.toString()).apply();
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
            sharedPreferences.edit().putString(COLLECTION_USUARIOS, usuarios.toString()).apply();
            
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
                    sharedPreferences.edit().putString(COLLECTION_SOLICITUDES, solicitudes.toString()).apply();
                    
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
                        sharedPreferences.edit().putString(COLLECTION_USUARIOS, usuarios.toString()).apply();
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
            String fechaIso = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());
            JSONObject newLog = obj(
                    "id", "log_" + System.currentTimeMillis(),
                    "tipo", tipo,
                    "nivel", nivel,
                    "titulo", titulo,
                    "subtitulo", subtitulo,
                    "tiempo", time,
                "fechaIso", fechaIso,
                    "detalle", detalle,
                    "resumen", resumen
            );
            JSONArray newLogsArray = new JSONArray();
            newLogsArray.put(newLog);
            for (int i = 0; i < logs.length(); i++) {
                newLogsArray.put(logs.getJSONObject(i));
            }
            sharedPreferences.edit().putString(COLLECTION_LOGS, newLogsArray.toString()).apply();
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
}
