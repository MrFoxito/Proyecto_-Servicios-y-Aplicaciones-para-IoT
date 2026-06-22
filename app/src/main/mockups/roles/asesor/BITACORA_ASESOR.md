# Bitacora de Avances - Rol Asesor

Este documento registra los cambios y decisiones tomadas durante el desarrollo de las interfaces y la navegacion del rol **Asesor**.

## Formato de Registro

### [Fecha] | [Quien lo hizo]
- **Cambio:** descripcion breve del avance.
- **Archivos:** rutas modificadas o creadas.
- **Estado:** en progreso / hecho / pendiente de revision.
- **Notas:** observaciones tecnicas o funcionales.

---

### 2026-04-20 | IA
- **Cambio:** Creacion inicial de las cuatro vistas principales del rol Asesor con bottom navigation: Mi Agenda, Separaciones, Chats y Mi Perfil.
- **Archivos:**
    - `app/src/main/java/com/example/proyecto_iot/BaseAsesorActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AsesorMiAgendaActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AsesorSeparacionesActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AsesorChatsActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AsesorPerfilActivity.java`
    - `app/src/main/res/layout/partial_asesor_bottom_nav.xml`
    - `app/src/main/res/layout/activity_asesor_miagenda.xml`
    - `app/src/main/res/layout/activity_asesor_separaciones.xml`
    - `app/src/main/res/layout/activity_asesor_chats.xml`
    - `app/src/main/res/layout/activity_asesor_perfil.xml`
    - `app/src/main/res/drawable/as_*.xml`
    - `app/src/main/res/drawable/ic_as_*.xml`
    - `app/src/main/res/drawable/as_property_*.png`
    - `app/src/main/AndroidManifest.xml`
- **Estado:** hecho, pendiente de prueba visual en emulador.
- **Notas:** Se mantuvo `MainActivity` sin cambios. Las acciones hacia vistas secundarias muestran temporalmente un mensaje de pendiente hasta que se implementen las pantallas restantes del flujo.

---

### 2026-04-20 | IA
- **Cambio:** Implementacion de ocho vistas secundarias del flujo Asesor y conexion de la navegacion desde las vistas principales.
- **Archivos:**
    - `app/src/main/java/com/example/proyecto_iot/AsesorDetalleCitaActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AsesorReprogramarCitaActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AsesorHistorialCitasActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AsesorRegistrarSeparacionActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AsesorSolicitudSeparacionActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AsesorPagoAprobadoActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AsesorChatIndividualActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AsesorEditarPerfilActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AsesorMiAgendaActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AsesorSeparacionesActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AsesorChatsActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/AsesorPerfilActivity.java`
    - `app/src/main/res/layout/activity_asesor_detalle_cita.xml`
    - `app/src/main/res/layout/activity_asesor_reprogramar_cita.xml`
    - `app/src/main/res/layout/activity_asesor_historial_citas.xml`
    - `app/src/main/res/layout/activity_asesor_registrar_separaciones.xml`
    - `app/src/main/res/layout/activity_asesor_solicitud_separacion.xml`
    - `app/src/main/res/layout/activity_asesor_pago_aprobado.xml`
    - `app/src/main/res/layout/activity_asesor_chat_individual.xml`
    - `app/src/main/res/layout/activity_asesor_editar_perfil.xml`
    - `app/src/main/res/drawable/as_*.xml`
    - `app/src/main/res/drawable/ic_as_*.xml`
    - `app/src/main/AndroidManifest.xml`
- **Estado:** hecho, pendiente de prueba visual en emulador.
- **Notas:** Las vistas secundarias no incluyen bottom navigation. Se conectaron detalle/reprogramar/historial desde Mi Agenda, registrar/solicitud/pago aprobado desde Separaciones, chat individual desde Chats y editar perfil desde Mi Perfil.

---

### 2026-04-20 | IA
- **Cambio:** Registro de indicacion para probar el flujo Asesor en emulador.
- **Archivos:**
    - `app/src/main/mockups/roles/asesor/BITACORA_ASESOR.md`
- **Estado:** pendiente de prueba visual en emulador.
- **Notas:** El proyecto compila correctamente con `./gradlew.bat assembleDebug`. `MainActivity` se mantiene sin cambios y sigue apuntando al flujo Admin; para probar Asesor directamente en emulador se puede cambiar temporalmente la entrada a `AsesorMiAgendaActivity` y luego restaurarla a `AdminResumenActivity`.

---

### 2026-04-20 | IA
- **Cambio:** Correccion de cierre de sesion en perfil Asesor y aplicacion comun de safe area/tipografia para pantallas Asesor.
- **Archivos:**
    - `app/src/main/java/com/example/proyecto_iot/RoleUiHelper.java`
    - `app/src/main/java/com/example/proyecto_iot/asesor/BaseAsesorActivity.java`
    - `app/src/main/java/com/example/proyecto_iot/asesor/AsesorPerfilActivity.java`
- **Estado:** hecho, compilado correctamente.
- **Notas:** El boton `Cerrar Sesion` ahora limpia la sesion con `AuthSessionManager.logout()` y redirige directamente a `LoginActivity` con el stack limpio. `BaseAsesorActivity` aplica insets de sistema y normalizacion de textos segun la guia visual. Verificacion realizada con `./gradlew.bat assembleDebug` usando JDK local 26 con resultado BUILD SUCCESSFUL.
