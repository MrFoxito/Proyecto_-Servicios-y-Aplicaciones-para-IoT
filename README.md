# Proyecto – Servicios y Aplicaciones para IoT

Rama de entrega: [`G3_final_IoT`](https://github.com/MrFoxito/Proyecto_-Servicios-y-Aplicaciones-para-IoT/tree/G3_final_IoT)

Aplicación Android para la gestión de proyectos inmobiliarios con roles de cliente, asesor, administrador y superadministrador. Incluye exploración de proyectos, Mapbox, citas, separaciones, chat, reportes y gestión de usuarios.

## Requisitos

- Android Studio con Android SDK instalado.
- JDK 11 o superior.
- Node.js y Firebase CLI, solo para pruebas o despliegue de reglas Firestore.
- Un proyecto Firebase configurado en `app/google-services.json`.
- Un token público de Mapbox para usar los mapas.

## Clonar la rama final

```bash
git clone --branch G3_final_IoT https://github.com/MrFoxito/Proyecto_-Servicios-y-Aplicaciones-para-IoT.git
cd Proyecto_-Servicios-y-Aplicaciones-para-IoT
```

## Configuración local

`local.properties` no se versiona. Crea el archivo en la raíz del proyecto o copia `local.properties.example` y ajusta las rutas y valores locales.

```properties
sdk.dir=C\:\\ruta\\a\\tu\\Android\\Sdk
MAPBOX_ACCESS_TOKEN=pk.tu_token_publico_de_mapbox
```

`MAPBOX_ACCESS_TOKEN` debe ser un token público que empiece con `pk.`. Nunca subas tokens, claves de Firebase, claves de Supabase ni archivos `local.properties` al repositorio.

Mapbox se usa para la selección y previsualización de ubicaciones de proyectos. Si el token falta o no es válido, la aplicación muestra un fallback seguro y las demás pantallas continúan funcionando.

## Compilar la APK de prueba

En Windows PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug
```

La APK se genera en:

```text
app\build\outputs\apk\debug\app-debug.apk
```

Para ejecutar también las pruebas unitarias:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

## Firestore

Las reglas e índices del proyecto se encuentran en `firestore.rules` y `firestore.indexes.json`. Tras cambiar de proyecto Firebase o clonar en un entorno nuevo, inicia sesión con Firebase CLI y despliega ambos:

```powershell
firebase deploy --only firestore:rules,firestore:indexes
```

Para probar las reglas localmente con Firestore Emulator:

```powershell
npm install
firebase emulators:exec --only firestore "npm run test:rules"
```

## Notas funcionales

- Cliente: explora proyectos, escanea QR, agenda citas, consulta actividad y conversa con el asesor asignado.
- Asesor: visualiza su agenda, gestiona citas y registra separaciones vinculadas a proyectos y clientes válidos.
- Administrador: administra proyectos, asesores y reportes. Los reportes muestran montos de separaciones aprobadas o pagadas; no presentan una separación como venta total del inmueble.
- Superadministrador: administra empresas, administradores y solicitudes de asesores.

Una separación representa el monto de reserva de una tipología. El precio total del inmueble se conserva como referencia y no se registra como una venta completa.

## Verificación de esta rama

La versión publicada se validó con:

```text
testDebugUnitTest
assembleDebug
npm run test:rules mediante Firestore Emulator
```
