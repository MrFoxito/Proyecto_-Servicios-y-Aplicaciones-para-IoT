# Documentación oficial complementaria

**Proyecto:** Servicios y Aplicaciones para IoT
**Fecha de revisión:** 19 de julio de 2026

Este documento resume los capítulos requeridos para la documentación oficial. Fue elaborado a partir del código Android, las reglas de Firestore, las funciones de Supabase y la configuración del proyecto.

## 3. Análisis de costos

La aplicación utiliza Firebase Authentication para el acceso de usuarios, Cloud Firestore para los datos del negocio, Supabase Storage para las imágenes y servicios de Google Maps, Places y Mapbox para funciones de ubicación. Firebase Storage está declarado como dependencia, pero no forma parte del flujo activo de imágenes.

Firestore cobra por lecturas, escrituras, eliminaciones, almacenamiento, índices y transferencia. Su cuota gratuita incluye 50 000 lecturas, 20 000 escrituras y 20 000 eliminaciones diarias, además de 1 GiB de almacenamiento. Los chats, calendarios y listados con listeners en tiempo real pueden aumentar las lecturas. Las transacciones de cita también realizan varias operaciones para proteger la disponibilidad.

Supabase almacena las imágenes del bucket `app-images`. El plan Free resulta suficiente para desarrollo: incluye 1 GB de archivos, 5 GB de salida y 500 000 invocaciones de Edge Functions, pero pausa proyectos inactivos. Para producción se recomienda Supabase Pro, desde USD 25 mensuales, por continuidad, copias diarias y mayor capacidad.

| Escenario | Firebase/Firestore | Supabase | Recomendación |
|---|---|---|---|
| Desarrollo o demo | cuota gratuita según consumo | Free | controlar imágenes y no depender de disponibilidad continua |
| Piloto de hasta 500 usuarios | vigilar listeners y cuota diaria | Pro recomendado | crear presupuestos y alertas |
| Producción inicial | facturación habilitada | Pro | medir consumo real mensual |
| Crecimiento | análisis de índices, consultas y transferencia | Pro o superior | ajustar según usuarios activos y multimedia |

Google Maps/Places se factura por eventos o SKU habilitados y Mapbox por usuarios activos mensuales del SDK móvil. Se deben crear alertas de presupuesto, restricciones de claves y cuotas. Los precios cambian según región y proveedor, por lo que las cifras deben verificarse antes de contratar.

## 4. Criterios de seguridad

La seguridad se apoya en Firebase Authentication, Firestore Rules y Edge Functions de Supabase. La información de sesión guardada en `SharedPreferences` solo mejora la navegación: no otorga permisos y no reemplaza las reglas del servidor.

Los roles son `cliente`, `asesor`, `admin` y `superadmin`. Las reglas verifican el rol en `usuarios/{uid}`, la pertenencia a empresa, la propiedad del proyecto y las asignaciones activas. Un cliente solo puede gestionar sus propias citas; un asesor accede a citas y chats de proyectos donde posee una asignación activa; un administrador actúa sobre su empresa; el superadministrador realiza acciones globales.

Las reservas y cancelaciones de citas se procesan de forma atómica. La operación relaciona cita, slot de asesor, bloqueo de horario del cliente y evento. Así se evita que dos usuarios reserven el mismo horario o que un tercero cancele una cita ajena. Un error `PERMISSION_DENIED` debe revisarse validando sesión, perfil, rol, asignación, datos enviados y reglas desplegadas; no se deben abrir las reglas con acceso público.

El chat se vincula a cliente, asesor y proyecto. Solo sus participantes pueden leer o enviar mensajes. La creación del chat valida que el asesor esté activo y asignado al proyecto.

Las imágenes se suben mediante `upload-media`: la app envía un token Firebase, la Edge Function valida identidad y rol, y luego usa la service role de Supabase desde el servidor. La service role nunca debe estar en Android. El bucket de imágenes usa URLs públicas, por lo que no debe contener documentos ni datos sensibles.

Como mejoras de producción se recomienda activar Firebase App Check, MFA para cuentas privilegiadas, restringir colecciones heredadas, verificar que Supabase no mantenga políticas públicas de escritura y usar firma/ofuscación para releases.

## 5. Manual de instalación

### Requisitos

- Android Studio compatible con Android Gradle Plugin 9.1.0.
- JDK 17 o superior; el código usa compatibilidad Java 11.
- Android SDK Platform 36 y dispositivo/emulador Android 11 (API 30) o superior.
- Git, Node.js, npm, Firebase CLI y, para funciones, Supabase CLI.

### Configuración local

Clonar el repositorio, abrirlo en Android Studio y crear `local.properties` a partir de `local.properties.example`:

```properties
sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk
MAPBOX_ACCESS_TOKEN=pk.TOKEN_PUBLICO
MAPBOX_DOWNLOADS_TOKEN=sk.TOKEN_SECRETO_SOLO_BUILD
MAPS_API_KEY=CLAVE_RESTRINGIDA_DE_GOOGLE
SUPABASE_URL=https://TU_PROYECTO.supabase.co
SUPABASE_PUBLISHABLE_KEY=TU_CLAVE_PUBLICABLE
SUPABASE_BUCKET=app-images
SUPABASE_UPLOAD_FUNCTION=upload-media
```

No subir `local.properties`, keystores, tokens secretos ni la service role de Supabase a Git.

### Firebase y Google Sign-In

1. Registrar la app con package `com.example.proyecto_iot`.
2. Descargar `google-services.json` y colocarlo en `app/`.
3. Habilitar Email/Password y Google en Firebase Authentication.
4. Registrar SHA-1 y SHA-256 de debug y release, además del cliente OAuth web.
5. Crear Firestore Native y desplegar reglas e índices:

```powershell
firebase login
firebase use <ID_DEL_PROYECTO>
firebase deploy --only firestore:rules,firestore:indexes
```

El primer superadministrador debe ser creado mediante un procedimiento controlado; no mediante registro público.

### Supabase y mapas

Crear el bucket `app-images`, aplicar las políticas versionadas y configurar en Supabase los secretos `FIREBASE_PROJECT_ID`, `SUPABASE_URL` y `SUPABASE_SERVICE_ROLE_KEY`. Después desplegar:

```powershell
supabase functions deploy upload-media --no-verify-jwt
supabase functions deploy migrate-media --no-verify-jwt
```

`--no-verify-jwt` es intencional: las funciones validan un token de Firebase, no un JWT de Supabase. La clave de Google debe restringirse por aplicación, SHA y APIs. El token secreto de descargas de Mapbox solo se usa durante el build.

### Compilación y pruebas

```powershell
npm ci
npx firebase-tools@14.15.2 emulators:exec --only firestore "npm run test:rules"
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

El APK debug se genera en `app/build/outputs/apk/debug/app-debug.apk`. Para release se requiere un keystore externo, firma segura y registro de las huellas release en Firebase/Google Cloud.

## 6. Manual de usuario

### Cliente

El cliente puede registrarse con correo/contraseña o iniciar sesión con Google. Desde **Explorar** revisa proyectos, abre sus detalles, consulta galería, tipologías y amenidades. **Ver en el mapa** muestra únicamente la ubicación del proyecto abierto y centra la cámara en sus coordenadas.

En **Contactar asesor**, la app busca asesores activos asignados al proyecto. Si hay varios, el cliente elige uno. El sistema crea o reutiliza el chat vinculado al mismo cliente, asesor y proyecto. Desde el detalle de una cita, se abre directamente el chat con el asesor asignado a esa cita.

Para agendar una cita se elige asesor, fecha, horario, teléfono y nota. La confirmación solo aparece cuando Firestore guarda exitosamente la reserva. Luego, **Ir a mi actividad** abre las citas del usuario y **Seguir explorando** vuelve a la pantalla principal. Desde Actividad se puede abrir una cita, contactar al asesor, ver la zona o cancelarla. Al cancelar correctamente, el horario se libera.

### Asesor

El asesor consulta su agenda, abre citas de proyectos asignados, responde chats, registra notas, marca asistencia, reprograma o cancela según sus permisos. Solo visualiza información relacionada con asignaciones activas. También puede consultar separaciones y editar su perfil.

### Administrador

El administrador gestiona proyectos, asesores, asignaciones, empresa, reportes y notificaciones de su propia inmobiliaria. La creación, edición y previsualización de ubicación de proyectos usan Mapbox. Las imágenes se almacenan en Supabase y sus URLs se registran en Firestore. Una asignación activa define qué proyectos y citas gestiona cada asesor.

### Superadministrador

El superadministrador visualiza indicadores globales, usuarios, solicitudes, reportes y logs. También crea empresas e invitaciones para administradores. Estas acciones deben ejecutarse con cuentas individuales y protegidas.

## 7. Conclusiones

La aplicación ofrece una solución inmobiliaria Android con exploración de proyectos, mapas, citas, chats, separaciones y administración por roles. Firebase concentra autenticación y datos transaccionales; Supabase complementa el almacenamiento de imágenes mediante Edge Functions; Mapbox y Google Maps cubren diferentes funciones de ubicación.

Los flujos de citas y chat incorporan controles importantes: las citas validan cliente, asesor, proyecto y disponibilidad; las conversaciones están separadas por proyecto y solo pueden ser usadas por sus participantes. Los mapas enfocados evitan mostrar proyectos ajenos al abrir una ubicación desde un detalle o cita.

La solución es adecuada como base académica y funcional. Para publicación real se deben completar controles de producción: App Check, MFA para privilegios altos, auditoría, políticas de privacidad, firma release, restricciones de secretos, monitoreo de costos y revisión periódica de reglas y políticas de Storage.

## 8. Referencias

### Fuentes internas

- Código Android: `app/src/main/java` y `app/src/main/res`.
- Configuración: `app/build.gradle`, `AndroidManifest.xml`, `local.properties.example`.
- Seguridad: `firestore.rules`, `firestore.indexes.json`.
- Supabase: `supabase/functions`, `supabase/migrations` y `supabase/storage_policies.sql`.
- Documento base: *Documentacion_Tecnica_Proyecto_IoT.pdf*.

### Fuentes oficiales

- [Firebase Authentication](https://firebase.google.com/docs/auth)
- [Facturación de Cloud Firestore](https://firebase.google.com/docs/firestore/pricing)
- [Reglas de seguridad de Firestore](https://firebase.google.com/docs/firestore/security/get-started)
- [Transacciones de Firestore](https://firebase.google.com/docs/firestore/manage-data/transactions)
- [Precios de Supabase](https://supabase.com/pricing)
- [Seguridad de Supabase Storage](https://supabase.com/docs/guides/storage/security/access-control)
- [Google Maps Platform: precios y facturación](https://developers.google.com/maps/billing-and-pricing)
- [Uso y facturación de Places API](https://developers.google.com/maps/documentation/places/web-service/usage-and-billing)
- [Precios de Mapbox](https://docs.mapbox.com/accounts/guides/pricing/)
- [Android Studio](https://developer.android.com/studio/intro)
