# Instrucciones para redactar y diagramar la arquitectura del sistema

## Objetivo

Redacta la sección **Arquitectura del sistema** de una documentación técnica universitaria y genera una imagen horizontal, limpia y profesional que represente la arquitectura real de la aplicación descrita en este documento.

El proyecto es una aplicación móvil Android para la gestión y comercialización de proyectos inmobiliarios. Tiene cuatro roles: cliente, asesor, administrador y superadministrador.

La redacción debe ser formal, clara y académica, en español. No debes inventar componentes, patrones ni tecnologías que no estén indicados aquí.

---

## Clasificación correcta de la arquitectura

La aplicación utiliza una **arquitectura Android por capas orientada a Activities, complementada con el patrón Repository e integración de servicios externos**.

No debe describirse como:

- MVVM.
- Clean Architecture.
- Arquitectura de microservicios.
- Arquitectura basada en ViewModel, LiveData, StateFlow, Hilt o Dagger.

Las Activities participan tanto en la interfaz como en la coordinación de flujos. Reciben acciones del usuario, validan datos básicos, invocan repositorios mediante callbacks o listeners y actualizan la interfaz. Por ello, la separación de responsabilidades existe, pero no es estricta como en MVVM o Clean Architecture.

---

## Descripción técnica exacta

### 1. Usuarios y roles

Los actores del sistema son:

- **Cliente:** explora proyectos, revisa ubicación, galería, tipologías y amenidades; escanea QR; conversa con asesores; agenda y cancela citas; registra separaciones; revisa actividad e historial.
- **Asesor:** administra agenda, conversaciones, asistencia, cancelaciones, reprogramaciones y separaciones de los proyectos que tiene asignados.
- **Administrador:** administra proyectos, imágenes, tipologías, amenidades, ubicación, asesores asignados, reportes, solicitudes, notificaciones y separaciones de su empresa.
- **Superadministrador:** administra cuentas, empresas, invitaciones, solicitudes, logs y reportes globales.

### 2. Capa de presentación Android

La aplicación fue desarrollada de forma nativa con **Java y Android SDK**. La interfaz está organizada principalmente por Activities y por paquetes de rol.

Componentes principales de esta capa:

- Activities por rol: `usuario`, `asesor`, `admin` y `superadmin`.
- Activities base: `BaseUsuarioActivity`, `BaseAsesorActivity`, `BaseAdminActivity` y `BaseSuperadminActivity`.
- Layouts XML.
- View Binding en las pantallas que lo requieren.
- RecyclerView y Adapters para listas de proyectos, citas, chats, reportes, notificaciones y asignaciones.
- Formularios y validaciones básicas de entrada.
- Navegación mediante `Intent`, extras y deep links.
- Deep links de proyecto con el formato `app://proyecto/{projectId}`.

Las Activities mantienen estado de pantalla en campos de memoria y reciben resultados mediante callbacks y listeners de Firestore. No existe una capa formal de ViewModels.

### 3. Sesión y control transversal

`AuthSessionManager` es un singleton que coordina la sesión local.

Sus funciones principales son:

- Usar Firebase Authentication para acceso con correo/contraseña y Google Sign-In.
- Consultar el perfil del usuario en Firestore después de autenticarse.
- Guardar UID, rol, nombre, correo y teléfono en SharedPreferences.
- Apoyar el enrutamiento inicial según el rol.

SharedPreferences se utiliza para sesión, preferencias y algunos flujos locales heredados o demostrativos. **No representa la seguridad real del sistema.** El control de acceso efectivo se aplica en Firebase Authentication y las reglas de Firestore.

### 4. Capa de repositorios

Los repositorios centralizan las consultas, escrituras, transacciones y listeners. Las Activities los llaman directamente.

Agrupa los repositorios en el diagrama de esta manera:

#### Datos, cuentas y proyectos

- `FirebaseDataRepository`: perfiles, invitaciones, proyectos, tipologías, amenidades, imágenes, catálogos y metadatos.
- `AccountRepository`: perfil y empresa asociada a la cuenta.
- `ProjectAssignmentRepository`: asignación y desasignación de asesores a proyectos.

#### Operación inmobiliaria

- `FirebaseAppointmentRepository`: asesores asignados, disponibilidad, reserva, cancelación, reprogramación, asistencia, slots y eventos de citas.
- `AdvisorAgendaRepository`: combina asignaciones activas y citas para formar la agenda e historial del asesor.
- `FirebaseChatRepository`: conversaciones y mensajes en tiempo real entre cliente y asesor; puede vincular una conversación a un proyecto.
- `FirebaseSeparationRepository`: creación, consulta y actualización de separaciones.

#### Soporte administrativo, multimedia y legado

- `FirebaseAdminNotificationRepository`: notificaciones administrativas y decisiones sobre separaciones.
- `FirebaseReportRepository`: agregación de datos para reportes administrativos.
- `ProjectMediaRepository` y `SupabaseStorageRepository`: carga de imágenes hacia Supabase.
- `DataMigrationRepository`: normalización de datos heredados y migración de imágenes.
- `LocalSchemaStorage` y `AdminLocalStorage`: respaldo local de flujos heredados, demostrativos u offline.
- `SystemLogger`: registro de eventos del sistema.

### 5. Modelos y entidades

Los modelos trasladan información entre repositorios, Activities y Adapters. Algunos ejemplos son:

- Usuario.
- Empresa o inmobiliaria.
- Proyecto.
- Asignación.
- Cita.
- Slot de cita.
- Conversación y mensaje.
- Separación.
- Evento de cita.
- Notificación y reporte.

Firestore usa colecciones planas y relaciones mediante IDs. Las entidades principales incluyen `usuarios`, `empresas`, `proyectos`, `asignaciones`, `citas`, `citas_slots`, `cliente_citas_slots`, `eventos_cita`, `conversaciones`, `mensajes`, `separaciones`, `notificaciones` y colecciones de apoyo de proyectos.

---

## Firebase: backend principal

Firebase es el backend principal de la aplicación.

### Firebase Authentication

Firebase Authentication gestiona la identidad de usuarios mediante:

- Correo y contraseña.
- Inicio de sesión con Google mediante Credential Manager.

El UID autenticado se relaciona con el documento de perfil ubicado en `usuarios/{uid}`.

### Cloud Firestore

Cloud Firestore almacena los datos principales del negocio:

- Perfiles y empresas.
- Proyectos, tipologías, amenidades e imágenes.
- Asignaciones de asesores.
- Citas, slots de disponibilidad, bloqueos por cliente y eventos.
- Conversaciones y mensajes.
- Separaciones, notificaciones, solicitudes, reseñas y reportes.

Firestore también se usa para listeners en tiempo real, principalmente en agendas, chats, citas, asignaciones y notificaciones.

Las operaciones importantes de citas usan transacciones. Una reserva puede crear o actualizar de forma atómica:

1. La cita.
2. El slot del asesor.
3. El bloqueo horario del cliente.
4. El evento de historial.

### Seguridad Firestore

Las reglas de Firestore son la barrera de autorización real. Validan identidad, rol, participantes, propiedad de datos y asignación activa entre asesor y proyecto.

En el diagrama, las reglas, índices, listeners y transacciones deben aparecer **dentro del bloque Cloud Firestore**; no deben aparecer como repositorios separados.

### Aclaración importante sobre Firebase Storage

Firebase Storage figura como dependencia configurada, pero no forma parte del flujo activo documentado de carga de imágenes. No se encontraron llamadas activas a `FirebaseStorage`, `putFile` o `getDownloadUrl`.

Por ello:

- No debe aparecer como componente principal del diagrama.
- Si se desea mencionarlo, debe mostrarse con línea punteada y el texto: **“Dependencia configurada, sin flujo activo de almacenamiento”**.

Tampoco se utilizan Firebase Cloud Functions, Firebase Cloud Messaging, Firebase Realtime Database ni Firebase Cloud Functions en el flujo actual.

---

## Supabase: servicio complementario de multimedia

Supabase no reemplaza Firebase y no se usa para autenticación, Postgres, Realtime ni lógica principal de negocio.

Su uso se limita a imágenes mediante:

- `SupabaseStorageRepository` en Android.
- `ProjectMediaRepository` como fachada de carga de medios.
- Edge Function `upload-media`.
- Edge Function `migrate-media` para tareas administrativas.
- Supabase Storage, con el bucket `app-images`.

### Flujo correcto de imágenes

1. La Activity solicita la carga a `ProjectMediaRepository` o `SupabaseStorageRepository`.
2. Android obtiene el ID token del usuario autenticado en Firebase.
3. `OkHttp` envía imagen, token Firebase y metadatos a la Edge Function `upload-media`.
4. La Edge Function valida la firma, emisor, audiencia y perfil/rol del usuario Firebase.
5. La Edge Function usa su service role privada para guardar el archivo en Supabase Storage.
6. Supabase devuelve la URL pública del recurso.
7. Android registra la URL y metadatos de la imagen en Cloud Firestore.

La aplicación Android nunca incluye la service role de Supabase.

---

## Integraciones de cliente

Estas bibliotecas no son capas de backend; son integraciones usadas por la aplicación Android.

- **Mapbox:** vista previa y selección de ubicación en pantallas administrativas de proyectos.
- **Google Maps:** exploración de proyectos para cliente.
- **Google Places:** búsqueda y autocompletado de direcciones en la selección de ubicación.
- **ZXing:** generación y escaneo de QR para abrir proyectos mediante deep link.
- **Glide:** carga de imágenes remotas en la interfaz.
- **OkHttp:** comunicación HTTP con las Edge Functions de Supabase.

---

## Flujo general de datos que debe acompañar al diagrama

1. El usuario interactúa con una Activity.
2. La Activity valida campos básicos y navega según el rol o acción elegida.
3. La Activity invoca el repositorio correspondiente.
4. El repositorio consulta o actualiza Firebase Authentication y Cloud Firestore.
5. Firestore Rules valida la autorización de datos por UID, rol y relaciones de negocio.
6. Si la operación es multimedia, el repositorio obtiene el token Firebase y llama a una Edge Function de Supabase mediante OkHttp.
7. La Edge Function valida token y rol, guarda la imagen y devuelve una URL.
8. La Activity recibe callbacks o listeners y actualiza la interfaz, modelos y Adapters.

---

## Especificación visual obligatoria para la imagen

Genera una imagen horizontal, de estilo profesional y académico, con fondo blanco, bordes suaves y colores consistentes. Debe ser legible en una diapositiva o informe A4 horizontal.

### Orden visual de arriba hacia abajo

1. **Usuarios:** Cliente, Asesor, Administrador y Superadministrador.
2. **Aplicación Android nativa:** bloque grande con Presentación, Sesión y control, Repositorios, y Modelos/entidades.
3. **Firebase:** bloque naranja con Firebase Authentication, Cloud Firestore y Firestore Rules/índices/transacciones/listeners.
4. **Supabase:** bloque turquesa con Edge Functions y Supabase Storage.
5. **Integraciones Android:** Mapbox, Google Maps, Places, ZXing, Glide y OkHttp.

### Flechas obligatorias

- Usuarios → Presentación Android.
- Presentación → Sesión/control → Repositorios.
- Repositorios → Firebase Authentication y Cloud Firestore.
- Firebase Authentication → token Firebase → Edge Functions Supabase.
- Repositorio de multimedia → OkHttp → Edge Functions Supabase.
- Edge Functions → Supabase Storage.
- Edge Functions → URL/metadatos → Cloud Firestore.
- Presentación Android → mapas, QR y Glide.

### Restricciones del diagrama

- No usar el título “Clean Architecture” ni “MVVM”.
- No mostrar Firebase Storage como flujo principal.
- No mostrar Supabase Auth, Supabase Database, Supabase Realtime, FCM, Firebase Cloud Functions o Realtime Database.
- No afirmar que SharedPreferences protege datos: solo mantiene sesión y datos locales de apoyo.
- No dibujar un repositorio individual para cada tabla; agruparlos por responsabilidad para evitar saturación visual.

---

## Texto breve sugerido para el informe

> El sistema implementa una arquitectura Android por capas orientada a Activities y complementada con el patrón Repository. Las Activities gestionan la interacción, navegación y validación básica; los repositorios centralizan el acceso a Firebase, Supabase e integraciones externas; y las entidades/modelos transportan datos hacia los adaptadores y componentes visuales. La aplicación no implementa de forma estricta MVVM ni Clean Architecture, ya que las Activities también coordinan operaciones de negocio mediante callbacks y listeners.
>
> Firebase Authentication y Cloud Firestore constituyen el backend principal. Firestore almacena los datos de negocio, listeners en tiempo real y transacciones de citas; sus reglas aplican la autorización por UID, rol y asignación activa. AuthSessionManager y SharedPreferences conservan estado de sesión para navegación y presentación, pero no sustituyen las reglas de seguridad.
>
> Supabase se utiliza únicamente para medios. La aplicación obtiene un ID token de Firebase y lo envía mediante OkHttp a una Edge Function; esta valida la identidad y el rol, almacena el archivo en Supabase Storage y devuelve una URL que se registra como metadato en Firestore.
