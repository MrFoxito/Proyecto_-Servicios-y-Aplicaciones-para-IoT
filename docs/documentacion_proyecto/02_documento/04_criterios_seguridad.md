# Criterios de seguridad

Esta sección describe únicamente mecanismos encontrados en el repositorio. Los puntos débiles se indican de forma explícita para no presentar como segura una función que solo está protegida en la interfaz.

## Autenticación

Firebase Authentication valida correo/contraseña y credenciales de Google. También se usa para recuperación de contraseña. Esto evita almacenar contraseñas en Firestore o `SharedPreferences`.

Implementación: `AuthSessionManager`, `LoginActivity` y `RegisterActivity`.

Limitación: no hay MFA, App Check ni verificación obligatoria de correo.

## Sesión local

Después del acceso se guardan UID, rol, nombre, correo y teléfono en preferencias privadas de la aplicación. Esto permite restaurar navegación y datos visuales.

El rol local no es una autorización confiable porque puede alterarse en un dispositivo comprometido. Firestore Rules vuelve a consultar `usuarios/{uid}`, lo que evita depender únicamente de la copia local.

## Control de roles en Firestore

Las funciones `isCliente`, `isAsesor`, `isAdmin` e `isSuperadmin` obtienen el rol desde Firestore. Las reglas aplican permisos diferentes en proyectos, empresas, asignaciones, citas, chats y logs.

Problema detectado: al crear un perfil propio solo se protege expresamente el rol `admin`. Se deben permitir de manera directa únicamente perfiles `cliente`; asesor, admin y superadmin deberían crearse mediante flujos administrativos confiables.

## Propiedad empresarial

Los administradores solo pueden crear proyectos con su propio `adminId` y `empresaId`. Las tipologías, amenidades e imágenes validan que el administrador sea dueño del proyecto. Las asignaciones exigen que proyecto y asesor pertenezcan a la misma empresa.

Esto evita que un administrador modifique recursos de otra inmobiliaria, siempre que los documentos de perfil y empresa sean correctos.

## Seguridad de citas

La reserva exige cliente autenticado, asesor asignado, participantes consistentes, slot coherente y bloqueo horario del cliente. La reprogramación, cancelación y asistencia restringen los campos que puede cambiar el asesor. Las reglas usan `getAfter` para comprobar que las escrituras relacionadas ocurren atómicamente.

Esto protege contra reservas duplicadas, cambio de participantes y citas sin slot. Las pruebas del emulador cubren reserva, concurrencia, reprogramación y cancelación.

## Seguridad del chat

Una conversación debe tener exactamente cliente y asesor como participantes. Los chats de proyecto validan que el asesor esté asignado. Solo un participante puede leer la conversación o crear mensajes, y `senderUid` debe coincidir con el usuario autenticado.

Los mensajes no pueden editarse ni eliminarse desde el cliente; solo el superadministrador puede hacerlo según las reglas.

## Validación de Supabase

La aplicación no envía la service role. Envía una publishable key y el ID token Firebase. La Edge Function valida firma, audiencia y emisor con las claves públicas de Firebase, consulta `usuarios/{uid}` y autoriza la ruta solicitada. La service role permanece como secreto del entorno de Supabase.

Las imágenes se limitan a JPEG, PNG y WebP con un máximo de 8 MB. Las rutas se limpian antes de crear el objeto.

Riesgo: el bucket es público para lectura. Además, el script heredado `supabase-storage-setup.sql` contiene políticas públicas de escritura. Debe comprobarse en producción que esas políticas hayan sido eliminadas y que todas las cargas pasen por `upload-media`.

## Variables y credenciales

`SUPABASE_URL`, `SUPABASE_PUBLISHABLE_KEY`, `SUPABASE_BUCKET`, `SUPABASE_UPLOAD_FUNCTION` y `MAPS_API_KEY` se leen desde variables de entorno o `local.properties`. El archivo `local.properties` está ignorado por Git.

La API key incluida en `google-services.json` identifica el proyecto Firebase; no funciona como secreto de servidor, pero debe restringirse por aplicación y API en Google Cloud. La service role de Supabase y los secretos de las Edge Functions no deben colocarse en Android.

## Protección de componentes Android

La mayoría de Activities no están exportadas. Las excepciones intencionales son el launcher, el detalle de proyecto para deep links y el registro de administrador por invitación. Los deep links validan o normalizan su identificador antes de consultar Firestore.

La aplicación solicita solo Internet, cámara y notificaciones. La cámara se declara opcional.

## Validaciones de negocio

Se validan campos obligatorios, longitud de contraseña, formato de fechas, fechas no pasadas, disponibilidad de citas, capacidad, estado comercial del proyecto, tipo y tamaño de imagen y autorización de carpetas.

Las validaciones de UI mejoran la experiencia, pero las reglas o funciones deben repetir toda condición sensible. Algunas colecciones todavía dependen demasiado del cliente.

## Controles que no están implementados

- Firebase App Check.
- MFA.
- Custom claims para roles.
- Firebase Cloud Functions.
- FCM.
- Cifrado adicional de datos de negocio.
- Pinning de certificados.
- Backend propio para operaciones administrativas.
- Reglas de Firebase Storage.
- Políticas estrictas para todas las colecciones heredadas.

## Acciones prioritarias

1. Impedir autoasignación de `asesor` y `superadmin` en `usuarios`.
2. Restringir por destinatario y campos las escrituras de `notificaciones`.
3. Restringir `tramites`, `historial_usuario` y actualizaciones de `separaciones`.
4. Unificar `logs` con `logs_sistema`.
5. Eliminar políticas públicas antiguas de inserción y actualización en Supabase Storage.
6. Evaluar custom claims o un backend para altas de roles y operaciones administrativas.
7. Activar App Check y presupuestos antes de producción.
