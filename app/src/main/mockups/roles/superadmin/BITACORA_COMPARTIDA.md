# Bitacora compartida - Superadmin

Este archivo centraliza todos los cambios del rol `superadmin`.
La idea es que todo el equipo pueda revisar rapidamente que se hizo y que falta.

## Como registrar cambios

Usar este formato en cada actualizacion:
- Fecha: YYYY-MM-DD
- Responsable: nombre
- Cambio: que se hizo
- Archivos: rutas tocadas
- Estado: hecho / en progreso / pendiente
- Notas: bloqueos, decisiones o dependencias

## Historial

### 2026-04-09 | Copilot
- Cambio: se creo la estructura inicial de mockups por rol.
- Archivos: `app/src/main/mockups/roles/`
- Estado: hecho
- Notas: se dejaron vacias `admin_inmobiliaria`, `asesor_ventas` y `cliente`.

### 2026-04-09 | Copilot
- Cambio: se creo el README inicial de superadmin.
- Archivos: `app/src/main/mockups/roles/superadmin/README.md`
- Estado: hecho
- Notas: aun no se crean vistas XML.

### 2026-04-09 | Copilot
- Cambio: se crea esta bitacora compartida para coordinacion del equipo.
- Archivos: `app/src/main/mockups/roles/superadmin/BITACORA_COMPARTIDA.md`
- Estado: hecho
- Notas: usar este archivo como punto de comparticion entre roles.

### 2026-04-09 | Copilot
- Cambio: implementacion inicial de 8 vistas superadmin en Android (Java/XML) con navegacion mock.
- Archivos: `app/src/main/java/com/example/proyecto_iot/` y `app/src/main/res/layout/` (pantallas `activity_superadmin_*`).
- Estado: hecho
- Notas: se replico la estructura visual de los mockups, con datos estaticos y flujo de pantalla para el hito de UI/navigation.

### 2026-04-09 | Copilot
- Cambio: rediseño visual de `Gestion de Usuarios` para acercar fidelidad al mockup (header, filtros, tabla, estados y paginacion).
- Archivos: `app/src/main/res/layout/activity_superadmin_gestion_usuarios.xml`, `app/src/main/res/drawable/sa_pill_active.xml`, `app/src/main/res/drawable/sa_pill_inactive.xml`, `app/src/main/res/drawable/sa_toggle_on.xml`, `app/src/main/res/drawable/sa_toggle_off.xml`.
- Estado: hecho
- Notas: se mantuvo la navegacion ya implementada; cambios centrados en estilo visual.

### 2026-04-09 | Copilot
- Cambio: ajuste fino de color y tamanos en `Gestion de Usuarios` (titulo superior, encabezado principal y botones registrar/solicitudes).
- Archivos: `app/src/main/res/layout/activity_superadmin_gestion_usuarios.xml`, `app/src/main/res/drawable/sa_btn_primary_users.xml`, `app/src/main/res/drawable/sa_btn_secondary_users.xml`.
- Estado: hecho
- Notas: cambios enfocados en fidelidad visual respecto al mockup adjunto.

### 2026-04-09 | Copilot
- Cambio: correccion de fidelidad en `Gestion de Usuarios` para quitar tinte morado de botones y dejar `Usuarios` en dorado en el titulo principal.
- Archivos: `app/src/main/res/layout/activity_superadmin_gestion_usuarios.xml`.
- Estado: hecho
- Notas: se forzo `android:backgroundTint=@null` en botones para respetar drawables personalizados.

### 2026-04-09 | Copilot
- Cambio: ajuste visual fino en `Gestion de Usuarios` para botones y menu superior (primario azul, secundario blanco con sombra, icono hamburguesa uniforme).
- Archivos: `app/src/main/res/layout/activity_superadmin_gestion_usuarios.xml`, `app/src/main/res/drawable/sa_btn_primary_users.xml`, `app/src/main/res/drawable/sa_btn_secondary_users.xml`, `app/src/main/res/drawable/ic_sa_menu_uniform.xml`.
- Estado: hecho
- Notas: se verifico compilacion con `assembleDebug`.

### 2026-04-09 | Copilot
- Cambio: rediseño completo de `Solicitudes de Asesores` para alta fidelidad visual respecto al mockup (topbar, cards, chip pendiente, botones y espaciados).
- Archivos: `app/src/main/res/layout/activity_superadmin_aprobacion_asesores.xml`, `app/src/main/res/drawable/sa_card_request.xml`, `app/src/main/res/drawable/sa_chip_pending.xml`, `app/src/main/res/drawable/sa_btn_reject.xml`, `app/src/main/res/drawable/sa_avatar_sq_dark.xml`, `app/src/main/res/drawable/sa_avatar_sq_red.xml`, `app/src/main/res/drawable/sa_avatar_sq_blue.xml`.
- Estado: hecho
- Notas: se verifico compilacion con `assembleDebug`; el layout usa placeholders de avatar por no contar con imagenes finales dentro de `res/drawable`.

### 2026-04-09 | Copilot
- Cambio: rediseño de la vista de menu lateral superadmin para alinearla al mockup (panel 320dp, perfil superior, dashboard activo oscuro, opciones grises, cierre de sesion con borde rojo).
- Archivos: `app/src/main/res/layout/activity_superadmin_menu.xml`, `app/src/main/res/drawable/sa_drawer_active.xml`, `app/src/main/res/drawable/sa_drawer_logout_bg.xml`, `app/src/main/res/drawable/sa_avatar_menu.xml`, `app/src/main/res/drawable/sa_dot_online_menu.xml`.
- Estado: hecho
- Notas: se verifico compilacion con `assembleDebug`.

### 2026-04-09 | Copilot
- Cambio: rediseño de `Registrar Nuevo Administrador` para alta fidelidad visual (topbar, titulos, inputs, selector de agencia y boton guardar con gradiente).
- Archivos: `app/src/main/res/layout/activity_superadmin_registrar_administrador.xml`, `app/src/main/res/drawable/sa_input_register.xml`, `app/src/main/res/drawable/sa_selector_agency.xml`, `app/src/main/res/drawable/sa_btn_save_admin.xml`.
- Estado: hecho
- Notas: se mantuvieron IDs de navegacion (`btnOpenMenu`, `btnSaveAdmin`) y se valido build con `assembleDebug`.

### 2026-04-09 | Copilot
- Cambio: rediseño de `Reportes` (globales) para acercar alta fidelidad al mockup: topbar, chips, metricas, secciones Performance/Tendencias, bloque visual y bottom nav con Agencias activo.
- Archivos: `app/src/main/res/layout/activity_superadmin_reportes_globales.xml`.
- Estado: hecho
- Notas: se mantuvieron IDs de navegacion y se valido compilacion con `assembleDebug`.

### 2026-04-09 | Copilot
- Cambio: rediseño de la vista final de `Reportes de Usuarios` para alta fidelidad al mockup (metricas de usuarios, actividad con grafico, leyenda y bottom nav con `Clientes` activo).
- Archivos: `app/src/main/res/layout/activity_superadmin_reportes_usuarios.xml`, `app/src/main/res/drawable/sa_chart_users_lines.xml`, `app/src/main/res/drawable/sa_reports_card.xml`.
- Estado: hecho
- Notas: se validó compilacion con `assembleDebug`.

### 2026-04-09 | Copilot
- Cambio: ajuste global de tema Material 3 para eliminar tintes lilas heredados por defecto y alinear toda la app a la paleta superadmin.
- Archivos: `app/src/main/res/values/themes.xml`.
- Estado: hecho
- Notas: se definieron `colorPrimary/Secondary/Surface` y acentos de control; build validado con `assembleDebug`.

### 2026-04-09 | Copilot
- Cambio: ajuste de `Resumen` para que `Registrar Administrador` use estilo arena (sin tinte del tema).
- Archivos: `app/src/main/res/layout/activity_superadmin_resumen.xml`.
- Estado: hecho
- Notas: se forzo `backgroundTint` nulo para respetar el color del drawable.

### 2026-04-09 | Copilot
- Cambio: refinamiento de `Reportes de Usuarios` con estilo mas fino (chips, cards y tipografias) para alinearlo al look de Agencias.
- Archivos: `app/src/main/res/layout/activity_superadmin_reportes_usuarios.xml`.
- Estado: hecho
- Notas: se mantuvo `Clientes` activo en bottom nav.

### 2026-04-09 | Copilot
- Cambio: integracion de fotos desde `imagenes temporales` en avatares de vistas clave.
- Archivos: `app/src/main/res/drawable/sa_profile_admin.png`, `app/src/main/res/drawable/sa_profile_asesor_1.png`, `app/src/main/res/drawable/sa_profile_asesor_2.png`, `app/src/main/res/drawable/sa_profile_asesor_3.png`, `app/src/main/res/drawable/sa_profile_user_1.png`, `app/src/main/res/drawable/sa_profile_user_2.png`, y layouts superadmin de resumen/menu/usuarios/reportes/logs/aprobacion.
- Estado: hecho
- Notas: build validado con `assembleDebug`.

### 2026-04-09 | Copilot
- Cambio: ajuste fino de `Reportes de Usuarios` para igualar el estilo de Agencia (fuentes mas contenidas, cuadros mas compactos y bottom nav menos pesado).
- Archivos: `app/src/main/res/layout/activity_superadmin_reportes_usuarios.xml`.
- Estado: hecho
- Notas: refinado visual post-feedback del usuario; build validado.

### 2026-04-09 | Copilot
- Cambio: aplicacion de fotos reales de `imagenes temporales` en avatares de menu, resumen y solicitudes de asesores.
- Archivos: `app/src/main/res/layout/activity_superadmin_menu.xml`, `app/src/main/res/layout/activity_superadmin_resumen.xml`, `app/src/main/res/layout/activity_superadmin_aprobacion_asesores.xml` y recursos `sa_profile_*.png`.
- Estado: hecho
- Notas: se uso `ImageView` con `centerCrop` para mayor realismo visual.

### 2026-04-09 | Copilot
- Cambio: ajuste de fotos de perfil redondeadas en headers de vistas superadmin.
- Archivos: `app/src/main/res/drawable/sa_avatar_circle_mask.xml` y layouts `activity_superadmin_resumen.xml`, `activity_superadmin_gestion_usuarios.xml`, `activity_superadmin_aprobacion_asesores.xml`, `activity_superadmin_registrar_administrador.xml`, `activity_superadmin_reportes_globales.xml`, `activity_superadmin_reportes_usuarios.xml`, `activity_superadmin_logs.xml`.
- Estado: hecho
- Notas: se aplico `clipToOutline` con mascara circular y se validó build con `assembleDebug`.

### 2026-04-09 | Copilot
- Cambio: correccion en `Resumen` para fijar `Registrar Administrador` en tono crema (sin tinte azul) y reducir el tamano de `Estado Global`.
- Archivos: `app/src/main/res/layout/activity_superadmin_resumen.xml`.
- Estado: hecho
- Notas: se aplico `android/app:backgroundTint=@null` y se valido compilacion con `assembleDebug`.

### 2026-04-09 | Copilot
- Cambio: en `Gestion de Usuarios`, el boton `Ver Solicitudes de Asesores` se unifico al color crema de la paleta.
- Archivos: `app/src/main/res/layout/activity_superadmin_gestion_usuarios.xml`.
- Estado: hecho
- Notas: se uso `@drawable/sa_bg_button_light` con `backgroundTint` nulo y se valido build con `assembleDebug`.
