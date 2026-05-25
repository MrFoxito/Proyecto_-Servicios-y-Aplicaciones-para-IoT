# Bitácora de Avances - Rol Admin

Este documento sirve para registrar los avances, cambios y decisiones tomadas durante el desarrollo de las interfaces y la lógica del rol **Admin**.

## Formato de Registro
Cada vez que terminemos una tarea o pantalla, agregaremos una entrada con el siguiente formato:

### [Fecha] | [Quién lo hizo: Ej. Usuario / IA]
- **Cambio:** [Descripción breve de lo que se hizo, ej. Creación de la pantalla de Login para Admin]
- **Archivos:** `[Rutas de los archivos modificados o creados, ej. app/src/main/res/layout/activity_admin_login.xml]`
- **Estado:** [en progreso / hecho / pendiente de revisión]
- **Notas:** [Cualquier observación importante, como dependencias pendientes o detalles técnicos]

---

### 2026-04-17 | DAVID
- **Cambio:** Implementación de la vista de "Inicio" (Dashboard) para el rol Admin con alta fidelidad respecto al mockup.
- **Archivos:**
    - `app/src/main/res/layout/activity_admin_resumen.xml`
    - `app/src/main/res/drawable/ic_home.xml`, `ic_projects.xml`, `ic_advisors.xml`, `ic_reports.xml`, `ic_profile_outline.xml`, `ic_location.xml`, `ic_email.xml`, `ic_phone.xml`, `ic_bell.xml`
    - `app/src/main/res/drawable/ad_pill_active.xml`, `ad_bg_button_dark.xml`, `ad_bottom_nav_bg.xml`, `ad_bg_icon_rounded.xml`
    - `app/src/main/java/com/example/proyecto_iot/MainActivity.java`
- **Estado:** hecho
- **Notas:** Se replicó el diseño exacto del mockup (tarjetas de contacto, galería, estado y menú de navegación inferior). Se configuró `MainActivity` para abrir directamente la vista de Admin temporalmente.

---

### 2026-04-18 | DAVID
- **Cambio:** Avance general del flujo Admin con nuevas pantallas, detalles, navegación base y registro de activities en el manifiesto.
- **Archivos:**
    - `app/src/main/res/layout/activity_admin_proyectos.xml`
    - `app/src/main/res/layout/activity_admin_asesores.xml`
    - `app/src/main/res/layout/activity_admin_reportes.xml`
    - `app/src/main/res/layout/activity_admin_perfil.xml`
    - `app/src/main/res/layout/activity_admin_editar_empresa.xml`
    - `app/src/main/res/layout/activity_admin_detalle_proyecto.xml`
    - `app/src/main/res/layout/activity_admin_detalle_asesor.xml`
    - `app/src/main/res/layout/activity_admin_resenas_asesor.xml`
    - `app/src/main/res/layout/activity_admin_detalle_separacion.xml`
    - `app/src/main/res/layout/activity_admin_detalle_pago.xml`
    - `app/src/main/res/layout/activity_admin_resumen.xml`
    - `app/src/main/java/com/example/proyecto_iot/BaseAdminActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminResumenActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminProyectosActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminAsesoresActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminReportesActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminPerfilActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminEditarEmpresaActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminDetalleProyectoActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminDetalleAsesorActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminResenasAsesorActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminDetalleSeparacionActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminDetallePagoActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminNotificacionesActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminAsignarProyectoAsesorActivity.java`
    - `app/src/main/AndroidManifest.xml`
- **Estado:** hecho
- **Notas:** Se agregaron pantallas principales y de detalle del rol Admin, junto con clases Activity para conectar la navegación y dejar registradas las vistas en `AndroidManifest.xml`.

---

### 2026-04-19 | DAVID
- **Cambio:** Finalización de la navegación del rol Admin usando Bottom Navigation para las vistas principales y flujos internos para las pantallas secundarias.
- **Archivos:**
    - `app/src/main/java/com/example/proyecto_iot/BaseAdminActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminResumenActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminProyectosActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminAsesoresActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminReportesActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminPerfilActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminNotificacionesActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminDetalleAsesorActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminAsignarProyectoAsesorActivity.java`
    - `app/src/main/res/layout/activity_admin_resumen.xml`
    - `app/src/main/res/layout/activity_admin_asesores.xml`
    - `app/src/main/res/layout/activity_admin_detalle_asesor.xml`
    - `app/src/main/res/layout/activity_admin_notificaciones.xml`
    - `app/src/main/AndroidManifest.xml`
- **Estado:** hecho, pendiente de prueba en emulador
- **Notas:** Se descartó el uso de menú hamburguesa/AdminMenu para la presentación. La navegación principal queda en `Inicio`, `Proyectos`, `Asesores`, `Reportes` y `Perfil`; los subflujos conectan editar empresa, notificaciones, detalle de pago/separación, crear/detallar/editar proyecto, crear/detallar asesor, reseñas y asignación de proyecto. Se validaron referencias de IDs de forma estática; queda pendiente probar en Android Studio con emulador o dispositivo físico.

---

### 2026-04-19 | DAVID
- **Cambio:** Registro del recurso de color transparente para reutilizarlo en layouts y drawables del rol Admin.
- **Archivos:**
    - `app/src/main/res/values/colors.xml`
- **Estado:** hecho
- **Notas:** Se agregó el color `transparent` con valor `#00000000`, dejando disponible una referencia centralizada para fondos o estados visuales sin color visible.

---

### 2026-04-19 | DAVID
- **Cambio:** Consolidacion de flujos avanzados del rol Admin: solicitudes de asesores, filtros, navegacion, formularios, perfil, reportes y notificaciones.
- **Archivos:**
    - app/src/main/AndroidManifest.xml
    - app/src/main/java/com/example/proyecto_iot/AdminAsesoresActivity.java
    - app/src/main/java/com/example/proyecto_iot/AdminAsignarProyectoAsesorActivity.java
    - app/src/main/java/com/example/proyecto_iot/AdminCrearAsesorActivity.java
    - app/src/main/java/com/example/proyecto_iot/AdminCrearProyectoActivity.java
    - app/src/main/java/com/example/proyecto_iot/AdminEditarPerfilActivity.java
    - app/src/main/java/com/example/proyecto_iot/AdminEditarProyectoActivity.java
    - app/src/main/java/com/example/proyecto_iot/AdminNotificacionesActivity.java
    - app/src/main/java/com/example/proyecto_iot/AdminProyectosActivity.java
    - app/src/main/java/com/example/proyecto_iot/AdminSolicitudAsesoresActivity.java
    - app/src/main/java/com/example/proyecto_iot/AdminVerSolicitudActivity.java
    - app/src/main/res/layout/activity_admin_asesores.xml, activity_admin_asignar_proyecto_asesor.xml, activity_admin_crear_proyecto.xml, activity_admin_detalle_asesor.xml, activity_admin_detalle_proyecto.xml
    - app/src/main/res/layout/activity_admin_editar_perfil.xml, activity_admin_editar_proyecto.xml, activity_admin_notificaciones.xml, activity_admin_perfil.xml, activity_admin_proyectos.xml
    - app/src/main/res/layout/activity_admin_reportes.xml, activity_admin_resenas_asesor.xml, activity_admin_solicitud_asesores.xml, activity_admin_ver_solicitud.xml
    - app/src/main/res/drawable/bg_report_bar_dark.xml, bg_report_bar_muted.xml, bg_report_card.xml, bg_report_card_dark.xml, bg_report_donut_main.xml, bg_report_donut_track.xml
    - app/src/main/res/values/colors.xml
- **Estado:** hecho, pendiente de prueba en emulador
- **Notas:** Se registraron las nuevas activities AdminSolicitudAsesoresActivity y AdminVerSolicitudActivity en el manifiesto. En asesores se agregaron filtros por estado y acceso a solicitudes; en notificaciones se incorporaron filtros por tipo, descarte por deslizamiento y estado sin notificaciones; en proyectos se conectaron botones para ver detalle. Tambien se refinaron pantallas de crear/editar proyecto, editar perfil, asignacion de asesor, resenas y reportes, incluyendo nuevos recursos drawable para tarjetas y barras de reportes.

---

### 2026-04-20 | IA
- **Cambio:** Correccion de navegacion y acciones puntuales del rol Admin en proyectos, asesores, resenas y asignacion.
- **Archivos:**
    - `app/src/main/java/com/example/proyecto_iot/AdminProyectosActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminDetalleProyectoActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminAsignarProyectoAsesorActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminEditarProyectoActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AdminDetalleAsesorActivity.java`
    - `app/src/main/res/layout/activity_admin_proyectos.xml`
    - `app/src/main/res/layout/activity_admin_detalle_proyecto.xml`
    - `app/src/main/res/layout/activity_admin_asignar_proyecto_asesor.xml`
    - `app/src/main/res/layout/activity_admin_detalle_asesor.xml`
    - `app/src/main/res/layout/activity_admin_editar_proyecto.xml`
- **Estado:** hecho, compilado correctamente
- **Notas:** Se corrigio la apertura de resenas desde la tarjeta correspondiente en detalle de asesor. Se habilitaron filtros en proyectos y asignacion de proyectos. En detalle de proyecto se elimino "Descargar Dossier" y se dejo solo "Asignar Asesor", conectado a la vista de asignacion. En editar proyecto se alinearon acciones de tipologias, areas comunes y mapa con el comportamiento de crear proyecto. Finalmente, el boton "Asignar Proyecto" de detalle de asesor fue ajustado para dirigir a `AdminAsesoresActivity`. Verificacion realizada con `./gradlew.bat assembleDebug` con resultado BUILD SUCCESSFUL.

---

### 2026-04-20 | IA
- **Cambio:** Correccion de cierre de sesion en perfil Admin y aplicacion comun de safe area/tipografia para pantallas Admin.
- **Archivos:**
    - `app/src/main/java/com/example/proyecto_iot/RoleUiHelper.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/BaseAdminActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminPerfilActivity.java`
- **Estado:** hecho, compilado correctamente
- **Notas:** El boton `Cerrar Sesion` ahora limpia la sesion con `AuthSessionManager.logout()` y redirige directamente a `LoginActivity` con el stack limpio. `BaseAdminActivity` aplica insets de sistema y normalizacion de textos segun la guia visual. Verificacion realizada con `./gradlew.bat assembleDebug` usando JDK local 26 con resultado BUILD SUCCESSFUL.

---

### 2026-04-25 | IA
- **Cambio:** Integracion de `View Binding` exclusivamente para el rol Admin, reemplazando accesos directos con `findViewById(...)` en las activities Admin y habilitando la generacion de bindings en el modulo `app`.
- **Archivos:**
    - `app/build.gradle`
    - `app/src/main/java/com/example/proyecto_iot/admin/BaseAdminActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminAsesoresActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminAsignarProyectoAsesorActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminCrearProyectoActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminDetalleAsesorActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminDetallePagoActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminDetalleProyectoActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminDetalleSeparacionActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminEditarEmpresaActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminEditarPerfilActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminEditarProyectoActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminMenuActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminNotificacionesActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminPerfilActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminProyectosActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminReportesActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminResenasAsesorActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminResumenActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminSolicitudAsesoresActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminVerSolicitudActivity.java`
    - `app/src/main/mockups/roles/admin/BITACORA_ADMIN.md`
- **Estado:** hecho, pendiente de verificacion de compilacion
- **Notas:** Se agrego `buildFeatures { viewBinding true }` y un helper en `BaseAdminActivity` para admitir `setContentView(binding)`. El alcance se limito al paquete `com.example.proyecto_iot.admin`, sin migrar asesor, usuario ni superadmin.

---

### 2026-04-25 | IA
- **Cambio:** Registro de continuidad para siguiente sesion sobre posible migracion a `RecyclerView` en el rol Admin.
- **Archivos:**
    - `app/src/main/mockups/roles/admin/BITACORA_ADMIN.md`
- **Estado:** pendiente
- **Notas:** Se confirmo que en este proyecto es viable implementar `RecyclerView` con sus piezas base: `RecyclerView` como contenedor, `Adapter` para enlazar datos, `ViewHolder` para cada item, `item layout` para el diseno individual y `LayoutManager` para la disposicion. No se implemento aun. Posibles candidatas para futura migracion: listas de proyectos, asesores, notificaciones y solicitudes del rol Admin. Retomar desde esta decision antes de escribir codigo.

---

### 2026-05-02 | IA
- **Cambio:** Actualizacion visual de las vistas `adminCrearProyecto`, `adminEditarProyecto` y `adminDetalleProyecto` segun los nuevos mockups compartidos para Admin.
- **Archivos:**
    - `app/src/main/res/layout/activity_admin_crear_proyecto.xml`
    - `app/src/main/res/layout/activity_admin_editar_proyecto.xml`
    - `app/src/main/res/layout/activity_admin_detalle_proyecto.xml`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminDetalleProyectoActivity.java`
    - `app/src/main/mockups/roles/admin/BITACORA_ADMIN.md`
- **Estado:** hecho, pendiente de verificacion visual en emulador
- **Notas:** Se reorganizo el contenido para aproximarlo al orden y jerarquia de los mockups: material visual, datos base, ubicacion, mapa, estado comercial, tipologias, amenidades, fecha y tarjeta QR. En `adminDetalleProyecto` se retiro la accion de `Asignar Asesor`; ahora la unica accion principal es `Editar Proyecto`, tal como indica el tercer mockup.

---

### 2026-05-02 | IA
- **Cambio:** Implementacion de `RecyclerView` en vistas del rol Admin para listas de proyectos, asesores, notificaciones y solicitudes.
- **Archivos:**
    - `app/build.gradle`
    - `gradle/libs.versions.toml`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminProyectosActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminAsesoresActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminNotificacionesActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminSolicitudAsesoresActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/adapter/AdminProjectsAdapter.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/adapter/AdminAdvisorsAdapter.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/adapter/AdminNotificationsAdapter.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/adapter/AdminRequestsAdapter.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/model/AdminProjectItem.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/model/AdminAdvisorItem.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/model/AdminNotificationItem.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/model/AdminRequestItem.java`
    - `app/src/main/res/layout/activity_admin_proyectos.xml`
    - `app/src/main/res/layout/activity_admin_asesores.xml`
    - `app/src/main/res/layout/activity_admin_notificaciones.xml`
    - `app/src/main/res/layout/activity_admin_solicitud_asesores.xml`
    - `app/src/main/res/layout/item_admin_proyecto.xml`
    - `app/src/main/res/layout/item_admin_asesor.xml`
    - `app/src/main/res/layout/item_admin_asesor_footer.xml`
    - `app/src/main/res/layout/item_admin_project_chip.xml`
    - `app/src/main/res/layout/item_admin_notification_section.xml`
    - `app/src/main/res/layout/item_admin_notification_card.xml`
    - `app/src/main/res/layout/item_admin_solicitud.xml`
    - `app/src/main/mockups/roles/admin/BITACORA_ADMIN.md`
- **Estado:** hecho, compilado correctamente
- **Notas:** La migracion se limito al paquete `com.example.proyecto_iot.admin`. Se reemplazaron tarjetas fijas en XML por listas con `RecyclerView`, `Adapter` y modelos dedicados. Se conservaron filtros y navegacion principal; en `Notificaciones` se agrego manejo de secciones y descarte por swipe sobre items. Verificacion realizada con `./gradlew.bat assembleDebug` con resultado `BUILD SUCCESSFUL`.

---

### 2026-05-04 | IA
- **Cambio:** Migracion adicional a `RecyclerView` en el flujo Admin para resenas de asesor, asignacion de proyecto, proyectos asignados en detalle de asesor y subsecciones repetibles de detalle de proyecto.
- **Archivos:**
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminResenasAsesorActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminAsignarProyectoAsesorActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminDetalleAsesorActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminDetalleProyectoActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/adapter/AdminReviewsAdapter.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/adapter/AdminAssignableProjectsAdapter.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/adapter/AdminAssignedProjectsAdapter.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/adapter/AdminProjectGalleryAdapter.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/adapter/AdminProjectTypologiesAdapter.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/adapter/AdminProjectAmenitiesAdapter.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/model/AdminReviewItem.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/model/AdminAssignableProjectItem.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/model/AdminAssignedProjectItem.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/model/AdminProjectGalleryItem.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/model/AdminProjectTypologyItem.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/model/AdminProjectAmenityItem.java`
    - `app/src/main/res/layout/activity_admin_resenas_asesor.xml`
    - `app/src/main/res/layout/activity_admin_asignar_proyecto_asesor.xml`
    - `app/src/main/res/layout/activity_admin_detalle_asesor.xml`
    - `app/src/main/res/layout/activity_admin_detalle_proyecto.xml`
    - `app/src/main/res/layout/item_admin_resena.xml`
    - `app/src/main/res/layout/item_admin_asignable_project.xml`
    - `app/src/main/res/layout/item_admin_assigned_project.xml`
    - `app/src/main/res/layout/item_admin_project_gallery_image.xml`
    - `app/src/main/res/layout/item_admin_tipologia.xml`
    - `app/src/main/res/layout/item_admin_amenidad.xml`
    - `app/src/main/mockups/roles/admin/BITACORA_ADMIN.md`
- **Estado:** hecho, compilado correctamente
- **Notas:** `AdminResenasAsesorActivity` ahora renderiza comentarios con una lista vertical. `AdminAsignarProyectoAsesorActivity` paso de tarjetas fijas a una lista filtrable de proyectos con accion de asignacion. `AdminDetalleAsesorActivity` usa una lista horizontal para proyectos asignados y `AdminDetalleProyectoActivity` usa `RecyclerView` para galeria, tipologias y amenidades. Verificacion realizada con `./gradlew.bat assembleDebug` con resultado `BUILD SUCCESSFUL`.

---

### 2026-05-04 | IA
- **Cambio:** Uniformidad visual en formularios Admin de proyecto y correccion de scroll en resenas de asesor.
- **Archivos:**
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminCrearProyectoActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminEditarProyectoActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminResenasAsesorActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/adapter/AdminProjectVisualEditorAdapter.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/model/AdminProjectVisualItem.java`
    - `app/src/main/res/layout/activity_admin_crear_proyecto.xml`
    - `app/src/main/res/layout/activity_admin_editar_proyecto.xml`
    - `app/src/main/res/layout/activity_admin_resenas_asesor.xml`
    - `app/src/main/res/layout/item_admin_project_visual_editor.xml`
    - `app/src/main/mockups/roles/admin/BITACORA_ADMIN.md`
- **Estado:** hecho, compilado correctamente
- **Notas:** Se reemplazo el bloque fijo de `Material visual` en `AdminCrearProyectoActivity` y `AdminEditarProyectoActivity` por una galeria horizontal con `RecyclerView` y acciones mock de agregar/cambiar foto, alineando estas vistas con el lenguaje visual de `AdminDetalleProyectoActivity`. Ademas, `AdminResenasAsesorActivity` dejo de usar `RecyclerView` dentro de `ScrollView`, permitiendo visualizar y desplazar correctamente toda la lista de resenas. Verificacion realizada con `./gradlew.bat assembleDebug` con resultado `BUILD SUCCESSFUL`.

---

### 2026-05-04 | IA
- **Cambio:** Ajustes finales de navegacion y jerarquia visual en vistas Admin de asesores, detalle de proyecto y perfil.
- **Archivos:**
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminAsesoresActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminPerfilActivity.java`
    - `app/src/main/res/layout/activity_admin_asesores.xml`
    - `app/src/main/res/layout/item_admin_asesor_footer.xml`
    - `app/src/main/res/layout/activity_admin_detalle_proyecto.xml`
    - `app/src/main/res/layout/activity_admin_perfil.xml`
    - `app/src/main/mockups/roles/admin/BITACORA_ADMIN.md`
- **Estado:** hecho, compilado correctamente
- **Notas:** En `AdminAsesoresActivity` se elimino el acceso superior duplicado y la accion quedo abajo como footer con el texto `Ver solicitudes`. En `AdminDetalleProyectoActivity` se reforzo la barra inferior fija de `Editar Proyecto` para mantenerla visible sin depender del scroll. En `AdminPerfilActivity` se retiro la navegacion superior de `Mi Perfil` para evitar un retroceso innecesario desde una vista principal. Verificacion realizada con `./gradlew.bat assembleDebug` con resultado `BUILD SUCCESSFUL`.

---

### 2026-05-04 | IA
- **Cambio:** Ajuste de accesos fijos en asesores y ampliacion de filtros en notificaciones.
- **Archivos:**
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminAsesoresActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminNotificacionesActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/adapter/AdminAdvisorsAdapter.java`
    - `app/src/main/res/layout/activity_admin_asesores.xml`
    - `app/src/main/res/layout/activity_admin_notificaciones.xml`
    - `app/src/main/mockups/roles/admin/BITACORA_ADMIN.md`
- **Estado:** hecho, compilado correctamente
- **Notas:** En `AdminAsesoresActivity` la accion `Ver solicitudes` dejo de estar como footer desplazable y paso a mostrarse como boton fijo sobre la barra inferior, visible sin hacer scroll. En `AdminNotificacionesActivity` se agrego el filtro `Todos`, manteniendo tambien `Separaciones` y `Pagos`, con la logica de render y descarte sincronizada con el filtro activo. Verificacion realizada con `./gradlew.bat assembleDebug` con resultado `BUILD SUCCESSFUL`.

---

### 2026-05-23 | IA
- **Cambio:** Implementacion inicial de Storage Local para el flujo Admin de asignacion de proyecto a asesor.
- **Archivos:**
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminAsignarProyectoAsesorActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/model/AdminAssignmentRecord.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/storage/AdminLocalStorage.java`
    - `app/src/main/res/layout/activity_admin_asignar_proyecto_asesor.xml`
    - `app/src/main/mockups/roles/admin/BITACORA_ADMIN.md`
- **Estado:** hecho, compilado correctamente
- **Notas:** Se creo una capa separada `AdminLocalStorage` basada en `SharedPreferences` para guardar el historial local de asignaciones como JSON. Al presionar `Asignar`, la pantalla guarda proyecto, ubicacion, vecindario, estado, asesor y fecha; luego muestra un bloque de historial local con la ultima asignacion y las tres entradas mas recientes. El alcance se mantuvo solo en el rol Admin para demostrar persistencia local antes de conectar sincronizacion entre roles con Firebase. Verificacion realizada con `./gradlew.bat assembleDebug` con resultado `BUILD SUCCESSFUL`.

---

### 2026-05-23 | IA
- **Cambio:** Ampliacion de Storage Local para preferencias, borradores, historial de edicion y descartes de notificaciones en el rol Admin.
- **Archivos:**
    - `app/src/main/java/com/example/proyecto_iot/admin/storage/AdminLocalStorage.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/model/AdminProjectDraft.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/model/AdminEditedProjectRecord.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminProyectosActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminAsesoresActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminSolicitudAsesoresActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminAsignarProyectoAsesorActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminCrearProyectoActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminEditarProyectoActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminNotificacionesActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/adapter/AdminProjectFormTypologiesAdapter.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/adapter/AdminProjectFormAmenitiesAdapter.java`
    - `app/src/main/res/layout/activity_admin_crear_proyecto.xml`
    - `app/src/main/res/layout/activity_admin_editar_proyecto.xml`
    - `app/src/main/mockups/roles/admin/BITACORA_ADMIN.md`
- **Estado:** hecho, compilado correctamente
- **Notas:** Se guardan y restauran los ultimos filtros usados en Proyectos, Asesores, Solicitudes, Asignacion de proyectos y Notificaciones. `AdminCrearProyectoActivity` guarda un borrador local al volver/cancelar y restaura nombre, descripcion, direccion, ciudad, mapa, estado, tipologias, amenidades y fecha al reingresar; al publicar limpia el borrador. `AdminEditarProyectoActivity` guarda/restaura borrador de edicion y registra un historial local de proyectos editados al guardar cambios, visible en la misma pantalla. `AdminNotificacionesActivity` persiste los IDs descartados por swipe para que no reaparezcan al reabrir la vista. Verificacion realizada con `./gradlew.bat assembleDebug` con resultado `BUILD SUCCESSFUL`.

---

### 2026-05-23 | IA
- **Cambio:** Creacion de vista dedicada para consultar el historial local de asignaciones de proyectos a asesores desde el rol Admin.
- **Archivos:**
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminHistorialAsignacionesActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminAsesoresActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/adapter/AdminAssignmentHistoryAdapter.java`
    - `app/src/main/res/layout/activity_admin_historial_asignaciones.xml`
    - `app/src/main/res/layout/item_admin_assignment_history.xml`
    - `app/src/main/res/layout/activity_admin_asesores.xml`
    - `app/src/main/AndroidManifest.xml`
    - `app/src/main/mockups/roles/admin/BITACORA_ADMIN.md`
- **Estado:** hecho, compilado correctamente
- **Notas:** La nueva pantalla lee directamente `AdminLocalStorage.getAssignmentHistory()` y presenta cada registro con proyecto, asesor, ubicacion, estado y fecha de asignacion. El acceso se ubico en la seccion `Asesores`, debajo de los filtros, con una accion visible `Ver historial de asignaciones` para mantener el flujo dentro de Admin y hacer mas evidente la persistencia local. Verificacion realizada con `./gradlew.bat assembleDebug` con resultado `BUILD SUCCESSFUL`.

---

### 2026-05-23 | IA
- **Cambio:** Implementacion de notificaciones locales para eventos del rol Admin.
- **Archivos:**
    - `app/src/main/AndroidManifest.xml`
    - `app/src/main/java/com/example/proyecto_iot/admin/notifications/AdminNotificationHelper.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminAsignarProyectoAsesorActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminCrearProyectoActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminEditarProyectoActivity.java`
    - `app/src/main/mockups/roles/admin/BITACORA_ADMIN.md`
- **Estado:** hecho, compilado correctamente
- **Notas:** Se agrego el permiso `POST_NOTIFICATIONS`, un canal `Eventos Admin` y un helper central para crear/emitir notificaciones locales con `NotificationCompat`. Al asignar un proyecto a un asesor se emite una notificacion `Asesor asignado` que abre el historial local de asignaciones. Tambien se emiten notificaciones al publicar un proyecto y al guardar cambios de edicion, ambas dirigidas al listado de proyectos. En Android 13+ se solicita el permiso de notificaciones desde las pantallas Admin involucradas. Verificacion realizada con `./gradlew.bat assembleDebug` con resultado `BUILD SUCCESSFUL`.

---

### 2026-05-25 | IA
- **Cambio:** Registro del ultimo avance de la implementacion de storage local del rol Admin.
- **Archivos:**
    - `app/src/main/java/com/example/proyecto_iot/admin/storage/AdminLocalStorage.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/model/AdminAssignmentRecord.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/model/AdminProjectDraft.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/model/AdminEditedProjectRecord.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminAsignarProyectoAsesorActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminHistorialAsignacionesActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminCrearProyectoActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminEditarProyectoActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminProyectosActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminAsesoresActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminSolicitudAsesoresActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/admin/AdminNotificacionesActivity.java`
    - `app/src/main/mockups/roles/admin/BITACORA_ADMIN.md`
- **Estado:** documentado, APK debug generado correctamente
- **Notas:** El ultimo avance deja consolidado el storage local de Admin sobre `SharedPreferences` con datos serializados en JSON. Actualmente persiste historial de asignaciones de proyectos a asesores, ultimos filtros usados por pantalla, borradores de creacion y edicion de proyectos, historial local de proyectos editados y notificaciones descartadas por swipe. La pantalla de historial de asignaciones permite consultar la persistencia desde `Asesores`, y las acciones de asignar, publicar proyecto y guardar edicion tambien disparan notificaciones locales del canal `Eventos Admin`. Verificacion reciente realizada con `./gradlew.bat --no-daemon assembleDebug` con resultado `BUILD SUCCESSFUL`; APK generado en `app/build/outputs/apk/debug/app-debug.apk`.
