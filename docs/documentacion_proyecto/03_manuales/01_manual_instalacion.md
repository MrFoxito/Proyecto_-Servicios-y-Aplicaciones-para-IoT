# Manual de instalación

## 1. Requisitos

- Windows, Linux o macOS.
- Android Studio compatible con Android Gradle Plugin 9.1.0.
- JDK 17 o superior para ejecutar Gradle. El código Java se compila con compatibilidad Java 11.
- Android SDK Platform 36 con minor API level 1.
- Un emulador o dispositivo con Android 11/API 30 como mínimo.
- Git.
- Node.js y npm para las pruebas de Firestore Rules.
- Firebase CLI para emuladores y despliegue.
- Supabase CLI y acceso al proyecto Supabase si se van a desplegar funciones o migraciones.
- Un proyecto de Google Cloud/Firebase con Maps y Places configurados.

Flutter no es necesario. El repositorio no contiene un proyecto Flutter.

## 2. Clonar y abrir

```powershell
git clone --branch leo-avance-12-07-26 https://github.com/MrFoxito/Proyecto_-Servicios-y-Aplicaciones-para-IoT.git
cd Proyecto_-Servicios-y-Aplicaciones-para-IoT
```

Abrir la carpeta raíz en Android Studio y esperar la sincronización de Gradle.

## 3. Configurar Android SDK y secretos

Crear `local.properties` en la raíz. No subir este archivo a Git.

```properties
sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk

MAPS_API_KEY=tu_clave_google_maps

SUPABASE_URL=https://tu-proyecto.supabase.co
SUPABASE_PUBLISHABLE_KEY=tu_publishable_key
SUPABASE_BUCKET=app-images
SUPABASE_UPLOAD_FUNCTION=upload-media
```

En macOS o Linux, `sdk.dir` debe apuntar a la ruta local del SDK sin el formato de Windows.

`MAPS_API_KEY` debe ser una clave restringida a la aplicación Android con Maps SDK for Android y Places API (New) habilitadas. La publishable key de Supabase puede estar en el cliente, pero nunca debe usarse una service role key en Android.

## 4. Configurar Firebase

1. Crear o seleccionar un proyecto Firebase.
2. Registrar una app Android con package `com.example.proyecto_iot`.
3. Descargar `google-services.json` y colocarlo en `app/google-services.json`.
4. Activar Authentication con Email/Password.
5. Para Google Sign-In, crear un cliente OAuth web y configurar SHA-1/SHA-256 de la aplicación. El plugin debe generar `default_web_client_id`.
6. Crear Firestore en modo Native.
7. Desplegar reglas e índices:

```powershell
firebase login
firebase use tu-project-id
firebase deploy --only firestore:rules,firestore:indexes
```

8. Crear por un procedimiento controlado el primer perfil `superadmin` en Authentication y `usuarios/{uid}`. No usar una cuenta pública para este proceso.

No es necesario habilitar FCM ni Firebase Cloud Functions para reproducir el código actual.

## 5. Configurar Supabase

1. Crear o seleccionar el proyecto Supabase.
2. Vincular la CLI:

```powershell
supabase login
supabase link --project-ref TU_PROJECT_REF
```

3. Revisar y aplicar `supabase/migrations/202606220001_stabilize_app_images.sql` y `supabase/storage_policies.sql`.
4. Crear el bucket `app-images` si no existe. Debe aceptar JPEG, PNG y WebP hasta 8 MB.
5. Eliminar cualquier política heredada que permita `INSERT` o `UPDATE` públicos. El archivo antiguo `supabase-storage-setup.sql` no debe aplicarse sin revisar porque contiene políticas de demostración abiertas.
6. Configurar secretos de las funciones:

```powershell
supabase secrets set FIREBASE_PROJECT_ID=tu-firebase-project-id
supabase secrets set SUPABASE_URL=https://tu-proyecto.supabase.co
supabase secrets set SUPABASE_SERVICE_ROLE_KEY=tu_service_role_solo_en_servidor
```

7. Desplegar:

```powershell
supabase functions deploy upload-media --no-verify-jwt
supabase functions deploy migrate-media --no-verify-jwt
```

`--no-verify-jwt` es intencional: las funciones reciben un token Firebase, no un JWT de Supabase, y lo validan manualmente.

## 6. Instalar dependencias de pruebas

```powershell
npm ci
```

Para ejecutar las pruebas de reglas:

```powershell
npx firebase-tools@14.15.2 emulators:exec --only firestore "npm run test:rules"
```

## 7. Ejecutar la aplicación

Desde Android Studio, seleccionar un dispositivo API 30 o superior y usar Run.

Desde terminal:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

El APK de debug se genera normalmente en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 8. Generar APK release

El proyecto no incluye una configuración de firma release. Para una entrega real se debe crear un keystore fuera del repositorio y configurar `signingConfigs` de forma segura.

```powershell
.\gradlew.bat assembleRelease
```

El release actual tiene `minifyEnabled false`; no aplica reducción ni ofuscación R8.

## 9. Errores comunes

### “SDK location not found”

Crear `local.properties` y corregir `sdk.dir`, o configurar `ANDROID_HOME`.

### Google Sign-In indica que falta cliente web

Configurar el proveedor Google en Firebase, las huellas SHA y un cliente OAuth web. Volver a descargar `google-services.json`.

### El mapa aparece vacío

Revisar `MAPS_API_KEY`, las restricciones de la clave, Maps SDK for Android, Places API (New) y la facturación de Google Cloud.

### Supabase rechaza imágenes

Comprobar URL, publishable key, función desplegada, secretos de la función, sesión Firebase, formato de imagen, límite de 8 MB y permisos de carpeta.

### Firestore muestra “permission-denied”

Confirmar que `usuarios/{uid}` exista, que su rol y empresa sean correctos, que reglas e índices estén desplegados y que el asesor tenga una asignación activa determinista.

### Firestore solicita un índice

Copiar la combinación sugerida al archivo `firestore.indexes.json`, probarla en el emulador y desplegarla.
