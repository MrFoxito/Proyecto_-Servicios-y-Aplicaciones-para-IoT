# Documentación del proyecto inmobiliario IoT

**Aplicación móvil Android para gestión de proyectos inmobiliarios**  
**Revisión del repositorio:** 17 de julio de 2026

---

## Índice

1. Descripción del proyecto
2. Arquitectura del sistema
3. Modelo de datos
4. Flujos por rol
5. Análisis de costos
6. Criterios de seguridad
7. Manual de instalación
8. Manual de usuario
9. Conclusiones
10. Referencias

---

## 1. Descripción del proyecto

El proyecto es una aplicación móvil Android orientada a la gestión y comercialización de proyectos inmobiliarios. Su objetivo es reunir en una sola plataforma tareas que normalmente se realizan por distintos medios: explorar inmuebles, conversar con un asesor, coordinar visitas, registrar separaciones y controlar el trabajo administrativo de una inmobiliaria.

Existen cuatro roles. El cliente consulta proyectos, ubicación, galería, tipologías y amenidades; también puede escanear un QR, iniciar un chat, reservar una cita y revisar su actividad. El asesor administra su agenda, conversa con clientes, registra asistencia y separaciones. El administrador mantiene proyectos, imágenes y asignaciones de su empresa, además de revisar notificaciones y reportes. El superadministrador gestiona cuentas, invitaciones, solicitudes, logs y reportes globales.

La aplicación fue desarrollada de forma nativa con Java, Activities y layouts XML. Firebase Authentication gestiona la identidad y Cloud Firestore guarda la información principal. Supabase Storage almacena imágenes mediante Edge Functions que validan tokens Firebase. Google Maps y Places ofrecen funciones geográficas; Glide carga imágenes y ZXing genera o escanea códigos QR.

El valor principal está en conectar el recorrido del cliente con la operación interna. Un QR abre el proyecto, el proyecto conduce al chat o a la cita, la cita aparece en la agenda del asesor asignado y una separación genera información para el administrador. Es una base académica amplia, aunque todavía requiere reforzar permisos, retirar almacenamiento heredado y reemplazar el pago simulado antes de una publicación comercial.

## 2. Arquitectura del sistema

La arquitectura es Android por capas, orientada a Activities. No implementa MVVM ni Clean Architecture de forma estricta. Las Activities controlan la interfaz y llaman a repositorios; los repositorios acceden a Firebase o Supabase; las entidades y modelos trasladan datos a los Adapters.

### Capas

- **Presentación:** Activities, XML, View Binding, RecyclerView y Adapters separados por rol.
- **Sesión:** `AuthSessionManager`, Firebase Authentication y una copia local en `SharedPreferences`.
- **Datos:** repositorios para proyectos, citas, agenda, chat, separaciones, asignaciones, reportes, notificaciones y cuentas.
- **Backend principal:** Cloud Firestore y Firebase Authentication.
- **Medios:** Supabase Storage detrás de `upload-media` y `migrate-media`.
- **Integraciones:** Google Maps, Places, Glide, OkHttp y ZXing.

No se encontraron ViewModel, LiveData, providers, inyección de dependencias, Firebase Realtime Database, Firebase Cloud Functions ni FCM. Las notificaciones son locales y documentos Firestore.

```text
Android UI
    │
    ▼
Activities ──► Repositorios ──► Firebase Auth / Firestore
                         │
                         └────► Edge Function Supabase
                                      │
                                      ▼
                               Supabase Storage
```

Firebase conserva identidad, perfiles y datos de negocio. Para una imagen, Android obtiene un ID token Firebase, lo envía a Supabase, la Edge Function comprueba firma y rol y guarda el archivo con la service role. La URL pública vuelve a Android y se registra en Firestore.

## 3. Modelo de datos

Firestore usa colecciones planas relacionadas por IDs.

| Entidad | Colección | Relaciones principales |
|---|---|---|
| Usuario | `usuarios` | Firebase UID, empresa, citas, chats y separaciones |
| Empresa | `empresas` | Administrador, proyectos y asesores |
| Proyecto | `proyectos` | Empresa, tipologías, amenidades, imágenes y asignaciones |
| Asignación | `asignaciones` | Proyecto y asesor |
| Cita | `citas` | Cliente, asesor, proyecto y slot |
| Slot asesor | `citas_slots` | Una o más citas según capacidad |
| Bloqueo cliente | `cliente_citas_slots` | Impide dos citas simultáneas |
| Evento | `eventos_cita` | Historial de una cita |
| Conversación | `conversaciones` | Cliente, asesor y proyecto opcional |
| Mensaje | `mensajes` | Conversación, emisor y receptor |
| Separación | `separaciones` | Cliente, asesor, proyecto, cita y administrador |
| Notificación | `notificaciones` | Destinatario y entidad de origen |

También existen `proyectos_tipologias`, `proyectos_amenidades`, `proyectos_imagenes`, `admin_invitations`, `asesor_disponibilidad`, `proyectos_disponibilidad`, `solicitudes_asesor`, `resenas`, `tramites`, `historial_usuario`, `logs_sistema` y `app_meta`.

El repositorio conserva nombres heredados como `projectId`, `propertyId` y `proyectoId`, además de inconsistencias entre `logs` y `logs_sistema`, y entre `empresas`, `inmobiliarias` e `Inmobiliarias`.

## 4. Flujos por rol

### Cliente

El cliente se registra o inicia sesión, explora proyectos y abre sus detalles. Desde allí puede ver galería y mapa, iniciar una conversación, reservar una cita o comenzar una separación si el proyecto está en preventa o venta. El QR usa `app://proyecto/{projectId}` y abre directamente el detalle. “Mi actividad” reúne citas, separaciones e historial; parte de trámites e historial aún usa almacenamiento local.

Para reservar, el sistema busca un asesor activo asignado, carga disponibilidad, comprueba día y capacidad y ejecuta una transacción. La transacción crea la cita, ocupa el slot, crea el bloqueo horario del cliente y registra un evento.

### Asesor

La agenda escucha asignaciones activas y citas relacionadas. El asesor abre el detalle, edita notas, conversa, marca asistencia, cancela o reprograma. Reprogramar libera el slot anterior, ocupa el nuevo y mueve el bloqueo del cliente. También puede registrar separaciones vinculadas a una cita.

### Administrador

El administrador gestiona únicamente proyectos y asesores de su empresa. Puede crear y editar proyectos, ubicación, tipologías, amenidades e imágenes; asignar asesores; revisar solicitudes, separaciones, pagos representados por la aplicación, notificaciones, reseñas y reportes. Las imágenes viajan a Supabase.

### Superadministrador

Administra usuarios, estados, invitaciones de administradores, solicitudes y reportes globales. Una invitación relaciona correo y empresa; el destinatario completa su cuenta y la invitación se marca como aceptada. También puede consultar logs y ejecutar migraciones administrativas.

## 5. Análisis de costos

La estimación usa 20 lecturas y 2 escrituras de Firestore por usuario activo al día, imágenes promedio de 0.5 MB y 20 visualizaciones mensuales por usuario. Los precios se consultaron en fuentes oficiales el 17 de julio de 2026.

Firestore incluye 50 000 lecturas y 20 000 escrituras diarias sin costo. Para ilustrar excedentes se usa `us-central1`, con USD 0.03 por 100 000 lecturas después de la cuota. Supabase Free incluye 1 GB de archivos, 5 GB de egress y 500 000 invocaciones; Pro comienza en USD 25 e incluye 100 GB de archivos y 250 GB de egress.

| Usuarios | Firebase estimado | Supabase técnico | Total aproximado |
|---:|---:|---:|---:|
| 100 | USD 0 | Free: USD 0 | USD 0 desarrollo; USD 25 producción recomendada |
| 500 | USD 0 | Free: USD 0 | USD 0 desarrollo; USD 25 producción |
| 1 000 | USD 0 | Pro recomendado | USD 25 |
| 5 000 | USD 0.45 por lecturas estimadas | Pro | USD 25.45 |

Las reglas con `get/getAfter`, listeners, almacenamiento, egress y región pueden elevar el costo. Google Maps SDK figura sin costo por cargas, pero Places y Autocomplete se cobran por evento después de su cuota.

## 6. Criterios de seguridad

### Controles existentes

- Firebase Authentication evita guardar contraseñas en la base de datos.
- Firestore Rules consulta el rol real del perfil autenticado.
- Los administradores se limitan por empresa y propiedad de proyecto.
- Chats validan participantes y emisor.
- Citas validan asignación, identidad, slots, bloqueos y transiciones.
- Supabase valida tokens Firebase y limita ruta, tipo y tamaño de imagen.
- Secrets sensibles se leen desde el entorno; la service role no está en Android.
- Activities internas no se exportan salvo deep links intencionales.

### Riesgos principales

1. La creación de perfiles solo bloquea autoasignación `admin`; debe impedir también `asesor` y `superadmin`.
2. `notificaciones`, `tramites`, `historial_usuario` y actualizaciones de `separaciones` tienen permisos amplios.
3. `SystemLogger` escribe en `logs`, pero las reglas usan `logs_sistema`.
4. Un script antiguo de Supabase crea políticas públicas de escritura que deben eliminarse.
5. No existen App Check, MFA ni custom claims.
6. El bucket de imágenes es público para lectura.
7. Conviven Firestore y almacenamiento local en algunos flujos.

## 7. Manual de instalación

### Requisitos

Android Studio, JDK 17 o superior, Android SDK 36.1, dispositivo API 30+, Node/npm, Firebase CLI y Supabase CLI. Flutter no aplica.

Crear `local.properties`:

```properties
sdk.dir=RUTA_AL_ANDROID_SDK
MAPS_API_KEY=clave_google
MAPS_API_KEY=tu_clave_google_maps
SUPABASE_URL=https://proyecto.supabase.co
SUPABASE_PUBLISHABLE_KEY=publishable_key
SUPABASE_BUCKET=app-images
SUPABASE_UPLOAD_FUNCTION=upload-media
```

Colocar `google-services.json` en `app/`, habilitar Email/Password y Google en Firebase, crear Firestore y desplegar:

```powershell
firebase deploy --only firestore:rules,firestore:indexes
```

Vincular Supabase, aplicar migraciones, retirar políticas públicas heredadas, configurar `FIREBASE_PROJECT_ID` y `SUPABASE_SERVICE_ROLE_KEY` como secretos y desplegar las dos Edge Functions.

Para compilar:

```powershell
.\gradlew.bat assembleDebug
```

Para probar reglas:

```powershell
npm ci
npx firebase-tools@14.15.2 emulators:exec --only firestore "npm run test:rules"
```

## 8. Manual de usuario

### Acceso

Ingresar con correo/contraseña o Google. El enlace de recuperación usa el correo escrito. Un cliente nuevo completa el formulario de registro. Administradores nuevos deben seguir una invitación del superadministrador.

### Cliente

Usar Explorar para abrir proyectos, galería y mapa. Desde el detalle se inicia chat, cita, QR o separación. En Actividad se revisan citas y trámites; en Chats aparecen conversaciones en tiempo real; en Perfil se editan datos y se cierra sesión.

### Asesor

Usar Mi agenda para consultar visitas. El detalle permite nota, chat, asistencia, reprogramación, cancelación y separación. Separaciones muestra registros del asesor; Chats permite responder; Perfil presenta empresa y proyectos asignados.

### Administrador

Usar Proyectos para crear y editar información, ubicación e imágenes. Asesores permite revisar perfiles y asignaciones. Notificaciones lleva a separaciones o pagos simulados. Reportes resume la actividad de la empresa. Perfil y Empresa actualizan información propia.

### Superadministrador

Usar Gestión de usuarios para revisar cuentas y estados. Registrar administrador crea empresa e invitación. Solicitudes permite aprobar asesores. Reportes y Logs muestran actividad global disponible.

La pantalla de pago no integra una pasarela bancaria real y algunas preferencias o controles de seguridad son demostrativos.

## 9. Conclusiones

El sistema conecta autenticación, catálogo, mapas, QR, citas, chat, separaciones y administración en una sola aplicación. La arquitectura por repositorios facilita identificar responsabilidades, y las citas utilizan transacciones coherentes. Para producción se deben corregir reglas de roles y colecciones heredadas, eliminar políticas públicas antiguas, unificar el esquema, retirar almacenamiento local duplicado, añadir pruebas de interfaz y usar un backend confiable para operaciones administrativas y pagos.

## 10. Referencias

- [Android Studio](https://developer.android.com/studio/intro)
- [Firebase Authentication](https://firebase.google.com/docs/auth)
- [Cloud Firestore: precios](https://firebase.google.com/docs/firestore/pricing)
- [Firebase: planes](https://firebase.google.com/docs/projects/billing/firebase-pricing-plans)
- [Supabase: precios](https://supabase.com/pricing)
- [Supabase Edge Functions](https://supabase.com/docs/guides/functions)
- [Supabase Storage Access Control](https://supabase.com/docs/guides/storage/security/access-control)
- [Google Maps Platform: precios](https://mapsplatform.google.com/pricing/)
- [Google Maps Platform: precios](https://developers.google.com/maps/billing-and-pricing/pricing)

La lista ampliada de fuentes está en `04_referencias/fuentes_oficiales.md`.
