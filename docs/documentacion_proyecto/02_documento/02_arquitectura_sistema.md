# Arquitectura del sistema

## Tipo de arquitectura

El proyecto utiliza una arquitectura Android por capas, pero no aplica de forma estricta un patrón como Clean Architecture o MVVM. La interfaz está organizada alrededor de Activities y Adapters. Las Activities reciben las acciones del usuario y llaman directamente a repositorios. Los repositorios encapsulan las consultas y escrituras en Firebase o Supabase. Los modelos y entidades trasladan los datos hacia los adaptadores que finalmente actualizan la interfaz.

Se puede describir como una arquitectura orientada a Activities con una capa de repositorios y servicios externos. No existen ViewModel, LiveData, StateFlow, providers ni un contenedor de inyección de dependencias. El estado se conserva principalmente en campos de cada Activity, modelos en memoria, listeners de Firestore y `SharedPreferences`.

## Componentes

### Presentación Android

La interfaz está escrita en Java y XML. Cada flujo se representa mediante una Activity. Las clases `BaseUsuarioActivity`, `BaseAsesorActivity`, `BaseAdminActivity` y `BaseSuperadminActivity` centralizan parte de la navegación y apariencia de cada rol. RecyclerView y sus Adapters representan proyectos, citas, mensajes, notificaciones, reportes y otras listas.

La navegación se realiza con `Intent`. `MainActivity` abre el login. Después de autenticar, `LoginActivity` dirige al inicio correspondiente según el campo `rol` del documento `usuarios/{uid}`. Las pantallas secundarias reciben identificadores y datos por extras.

### Sesión y autenticación

`AuthSessionManager` funciona como singleton. Utiliza Firebase Authentication para correo/contraseña y Google Sign-In mediante Credential Manager. Después del acceso consulta el perfil de Firestore y guarda una copia básica de UID, rol, nombre, correo y teléfono en `SharedPreferences`.

La autorización real de datos no depende de esos valores locales, sino de `firestore.rules`, que vuelve a consultar el perfil del usuario autenticado. Los valores locales sirven para navegación y presentación, por lo que no deben considerarse una barrera de seguridad.

### Repositorios

- `FirebaseDataRepository`: perfiles, invitaciones, proyectos, tipologías, amenidades, imágenes y catálogo del cliente.
- `FirebaseAppointmentRepository`: asesores asignados, disponibilidad, reserva, reprogramación, cancelación, asistencia e historial de citas.
- `AdvisorAgendaRepository`: combina asignaciones activas con listeners de citas y alimenta agenda e historial del asesor.
- `FirebaseChatRepository`: conversaciones y mensajes en tiempo real para cliente y asesor.
- `FirebaseSeparationRepository`: creación, consulta y actualización de separaciones.
- `FirebaseAdminNotificationRepository`: notificaciones administrativas y decisiones sobre separaciones.
- `ProjectAssignmentRepository`: asignación y desasignación de asesores a proyectos.
- `FirebaseReportRepository`: agregación de datos para reportes del administrador.
- `AccountRepository`: perfiles y empresa vinculada a una cuenta.
- `ProjectMediaRepository` y `SupabaseStorageRepository`: carga de imágenes a Supabase.
- `DataMigrationRepository`: normalización de documentos antiguos y migración de imágenes.
- `LocalSchemaStorage` y `AdminLocalStorage`: almacenamiento local que aún se usa en flujos heredados o demostrativos.

### Firebase

Firebase Authentication mantiene la identidad principal. Firestore es la base de datos operativa y también se usa en tiempo real para citas, chats, asignaciones, separaciones y notificaciones. Las operaciones de citas usan transacciones para coordinar el documento de la cita, el slot del asesor, el bloqueo horario del cliente y el evento de historial.

La dependencia de Firebase Storage está incluida en Gradle y existe un bucket en `google-services.json`, pero no se encontraron llamadas a `FirebaseStorage`, `putFile` o `getDownloadUrl`. Tampoco existe un archivo de reglas de Storage. Por lo tanto, Firebase Storage no forma parte del flujo activo documentado.

No existe dependencia ni servicio de Firebase Cloud Messaging. Las notificaciones visibles son notificaciones locales creadas con `NotificationCompat` y documentos Firestore. Tampoco hay código de Firebase Cloud Functions.

### Supabase

Supabase no reemplaza a Firebase. No se usa Supabase Auth, Postgres, Realtime ni funciones de base de datos. Su uso se limita al bucket público `app-images` y a las Edge Functions `upload-media` y `migrate-media`.

La aplicación obtiene el ID token del usuario Firebase y llama a `upload-media` con OkHttp. La función verifica firma, emisor y audiencia del token usando las claves públicas de Firebase, consulta el perfil en Firestore y autoriza carpetas según UID y rol. Luego usa la service role de Supabase para subir la imagen. La URL pública resultante se guarda como metadato en Firestore.

### Mapas, lugares y QR

Google Maps se usa para el mapa de exploración y la selección de ubicación de proyectos; Places permite la búsqueda de direcciones. `MAPS_API_KEY` se carga desde `local.properties` mediante Secrets Gradle Plugin. ZXing se utiliza para crear el QR del proyecto y escanear valores con el formato `app://proyecto/{projectId}`. Ese deep link abre `UsuarioPropiedadDetalleActivity`.

## Flujo de datos

1. La Activity recibe una acción del usuario.
2. Valida campos básicos y llama al repositorio correspondiente.
3. El repositorio consulta o modifica Firebase; para imágenes llama a una Edge Function de Supabase.
4. Firebase Rules o la validación de la Edge Function comprueban la identidad y el rol.
5. El resultado vuelve mediante callbacks o listeners.
6. La Activity actualiza sus modelos y el Adapter redibuja la pantalla.

## Diagrama general

```text
┌──────────────────────────────────────────────┐
│ Aplicación Android (Java + XML + Activities) │
└──────────────────────┬───────────────────────┘
                       │
          ┌────────────▼────────────┐
          │ Repositorios y sesión   │
          │ callbacks / listeners   │
          └───────┬─────────┬───────┘
                  │         │
       ┌──────────▼───┐   ┌─▼───────────────────┐
       │ Firebase Auth│   │ Cloud Firestore     │
       │ identidad    │   │ datos + tiempo real │
       └───────┬──────┘   └──────────┬──────────┘
               │ token               │ URL y metadatos
               └──────────┬──────────┘
                          ▼
                ┌────────────────────┐
                │ Supabase Function  │
                │ valida Firebase    │
                └─────────┬──────────┘
                          ▼
                ┌────────────────────┐
                │ Supabase Storage   │
                │ imágenes públicas  │
                └────────────────────┘
```

## Dependencias entre módulos funcionales

- Proyectos depende de empresa, administrador, tipologías, amenidades e imágenes.
- Asignaciones depende de proyectos y perfiles con rol asesor.
- Citas depende de proyecto, asignación activa, disponibilidad, cliente y asesor.
- Agenda depende de las asignaciones activas y las citas del asesor.
- Chat depende de cliente, asesor y, para conversaciones de proyecto, de una asignación activa.
- Separaciones depende del estado comercial del proyecto y puede depender de una cita.
- Notificaciones administrativas dependen de separaciones, proyectos y asignaciones.
- Reportes agregan proyectos, asignaciones, citas y separaciones.
- Supabase Storage depende de una sesión Firebase válida y de los metadatos guardados en Firestore.
