# Informe técnico de auditoría

## Resumen ejecutivo

El sistema es una aplicación Android nativa de un solo módulo con 172 archivos Java y más de cien layouts XML. El código está dividido por rol y cuenta con una capa de repositorios que concentra buena parte del acceso a Firebase y Supabase. La arquitectura es funcional para un proyecto académico, aunque varias Activities todavía contienen lógica de negocio, consultas directas y estado local.

Firebase es el backend principal. Authentication gestiona la sesión y Firestore conserva casi todas las entidades. Supabase actúa como almacenamiento de imágenes detrás de Edge Functions que validan tokens de Firebase. No se encontró Firebase Cloud Messaging, Firebase Cloud Functions, Supabase Auth, Supabase Database ni Supabase Realtime.

## Flujo completo de la aplicación

`MainActivity` abre `LoginActivity`. El usuario puede autenticarse con correo y contraseña, usar Google o abrir el registro. Después del acceso se consulta `usuarios/{uid}`, se guarda una copia de la sesión en preferencias y se navega según el rol:

- `cliente` → `UsuarioHomeActivity`.
- `asesor` → `AsesorHomeActivity`, que conduce a la agenda.
- `admin` → `AdminHomeActivity`; si el perfil está incompleto se abre su edición.
- `superadmin` → `SuperadminResumenActivity`.

El cliente explora proyectos obtenidos de Firestore, abre detalles, galería o mapa y puede iniciar un chat, reservar una cita, escanear el QR o comenzar una separación. El asesor recibe citas relacionadas con sus proyectos activos, puede registrar asistencia, reprogramar, cancelar, conversar y registrar una separación. El administrador crea y edita proyectos, administra asesores y recibe notificaciones por separaciones. El superadministrador gestiona cuentas, invitaciones, solicitudes, reportes y logs.

## Integraciones verificadas

### Firebase Authentication

Se usan correo/contraseña, recuperación de contraseña y Google Sign-In. La identidad Firebase se conserva durante la sesión. El perfil de aplicación vive en Firestore y contiene el rol. No se encontró MFA, App Check ni custom claims.

### Cloud Firestore

Es la fuente principal para usuarios, empresas, proyectos, asignaciones, citas, chats, separaciones y notificaciones. Existen consultas en tiempo real y transacciones. La estructura es plana: no se encontraron subcolecciones operativas; las relaciones se expresan mediante IDs y listas de participantes.

### Firebase Storage

La biblioteca está declarada, pero el código actual no la invoca. No existe `storage.rules`. El almacenamiento activo de imágenes es Supabase.

### Cloud Functions y FCM

No están implementados. Las notificaciones Android son locales y se originan desde código cliente o documentos de Firestore. No hay servicio que reciba tokens FCM ni dependencia `firebase-messaging`.

### Supabase

Se usa Storage y Edge Functions. `upload-media` permite imágenes JPEG, PNG o WebP de hasta 8 MB y autoriza carpetas según el usuario Firebase. `migrate-media` requiere el rol superadmin. Ambas funciones tienen `verify_jwt=false` en el gateway porque validan manualmente un token de Firebase.

## Reglas de negocio confirmadas

- Los estados comerciales se normalizan a `en_planos`, `preventa` o `venta`.
- Una cita puede programarse en cualquiera de esos estados.
- Una separación solo puede crearse en preventa o venta.
- El QR canónico es `app://proyecto/{projectId}`.
- Una cita requiere un asesor activo asignado al proyecto.
- La disponibilidad prioriza `proyectos_disponibilidad/{projectId}`, luego `asesor_disponibilidad/{asesorId}` y finalmente horarios por defecto.
- La reserva crea una cita, un slot, un bloqueo global del cliente y un evento dentro de una transacción.
- Un cliente no puede tener dos citas en la misma fecha y hora, aunque los asesores sean distintos.
- Reprogramar mueve el slot y el bloqueo; cancelar los libera.
- La agenda del asesor solo conserva citas de proyectos con asignación activa.
- Una conversación de proyecto relaciona cliente, asesor y proyecto.
- Una separación puede marcar `hasCierre` en la cita que la originó.

## Riesgos e inconsistencias

### Críticos

1. Las reglas de creación de `usuarios` solo bloquean la autoasignación del rol `admin`. Un cliente que use el SDK directamente podría intentar crear su perfil con rol `superadmin` o `asesor`. La UI no lo ofrece, pero la seguridad no debe depender de la UI.
2. `SystemLogger` escribe en la colección `logs`, mientras las reglas y pantallas administrativas utilizan `logs_sistema`. `logs` no tiene una regla explícita y las escrituras del logger pueden ser rechazadas.
3. El archivo raíz `supabase-storage-setup.sql` crea políticas públicas de inserción y actualización. Las migraciones nuevas no eliminan expresamente esas políticas heredadas. Si el script antiguo fue ejecutado, podrían continuar permitiéndose cargas directas sin pasar por la Edge Function.

### Altos

1. `notificaciones` permite crear y actualizar a cualquier usuario autenticado. Un cliente podría fabricar o alterar notificaciones.
2. `tramites` permite lectura, creación y actualización a cualquiera de los cuatro roles sin comprobar propiedad o participantes.
3. `historial_usuario` permite leer y crear a cualquier usuario autenticado.
4. Las actualizaciones de `separaciones` permiten a participantes cambiar el documento sin limitar campos ni transiciones de estado.
5. Los roles se leen de documentos Firestore en cada evaluación. No hay custom claims ni backend confiable para operaciones administrativas sensibles.
6. El bucket de imágenes es público. Esto es necesario para las URLs actuales, pero cualquier persona con la URL puede descargar el objeto.

### Medios

1. `AuthSessionManager` contiene un método para cargar dominios desde `Inmobiliarias`, pero no se invoca en el constructor. La asignación automática cliente/asesor por dominio puede usar una caché vacía.
2. Conviven nombres heredados: `empresas`, `inmobiliarias` e `Inmobiliarias`; `logs` y `logs_sistema`; `projectId`, `propertyId` y `proyectoId`. Esto aumenta consultas duplicadas y migraciones.
3. Parte de “Mi actividad” todavía escribe trámites e historial en `LocalSchemaStorage`, aunque citas y separaciones se guardan en Firestore.
4. Hay lógica de validación tanto en Activities como en repositorios y reglas. No siempre coinciden, especialmente en disponibilidad configurable.
5. `LocalSchemaStorage` contiene datos semilla y funciones heredadas que pueden confundirse con la fuente real.
6. La aplicación declara Firebase Storage, Google Maps y Places aunque el flujo principal de imágenes usa Supabase.

### Mantenibilidad

- No hay ViewModel ni repositorios inyectados; las Activities crean dependencias directamente.
- Se usan callbacks anidados, lo que dificulta pruebas y manejo uniforme de errores.
- Hay modelos de Firestore, modelos de UI y modelos locales con campos parecidos pero nombres distintos.
- Varias consultas no tienen paginación y algunos listeners pueden producir muchas lecturas.
- La cobertura automatizada se concentra en reglas y QR; faltan pruebas instrumentadas de navegación y repositorios.

## Conclusión

La aplicación implementa un flujo inmobiliario amplio y las integraciones principales están conectadas. Su punto más sólido es la separación reciente de repositorios y el uso de transacciones en citas. Los mayores riesgos están en la autorización de roles, las colecciones con reglas demasiado amplias, la coexistencia de esquemas antiguos y la política heredada de Supabase Storage. Antes de una publicación real se debería cerrar esos permisos, unificar nombres de entidades, eliminar fuentes locales duplicadas y trasladar operaciones administrativas a un backend confiable.
