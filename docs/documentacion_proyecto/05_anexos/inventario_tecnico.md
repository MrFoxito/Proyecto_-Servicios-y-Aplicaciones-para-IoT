# Inventario técnico

## Proyecto Android

| Elemento | Valor |
|---|---|
| Tipo | Aplicación Android nativa |
| Lenguaje | Java |
| UI | XML, Activities, RecyclerView, View Binding |
| Módulos | `app` |
| Namespace/package | `com.example.proyecto_iot` |
| Application ID | `com.example.proyecto_iot` |
| minSdk | 30 |
| targetSdk | 36 |
| compileSdk | 36, minor API level 1 |
| Compatibilidad del código | Java 11 |
| Android Gradle Plugin | 9.1.0 |
| Gradle Wrapper | 9.3.1 |
| Archivos Java auditados | 172 |
| Layouts XML | 118 |

## Dependencias declaradas

| Biblioteca | Versión | Uso encontrado |
|---|---:|---|
| Firebase BoM | 33.7.0 | Versionado coordinado de SDK Firebase |
| Firebase Auth | BoM | Sesión, registro, Google y reset |
| Firebase Firestore | BoM | Base de datos y tiempo real |
| Firebase Storage | BoM | Declarada, sin uso activo encontrado |
| AppCompat | 1.7.1 | Compatibilidad de Activities |
| Material Components | 1.14.0 | Controles y estilos |
| Activity | 1.13.0 | APIs AndroidX |
| ConstraintLayout | 2.2.1 | Layouts |
| RecyclerView | 1.4.0 | Listas |
| Credential Manager | 1.6.0 | Google Sign-In |
| Google ID | 1.2.0 | Token de Google |
| OkHttp | 4.12.0 | Edge Functions Supabase |
| Glide | 4.16.0 | Carga de imágenes |
| ZXing Core | 3.5.3 | QR |
| ZXing Embedded | 4.3.0 | Escáner QR |
| Google Maps | 19.2.0 | Selector/visualización geográfica |
| Places | 4.4.1 | Búsqueda de lugares |
| Google Maps SDK for Android | 19.2.0 | Exploración, selección y previsualización de ubicaciones |
| Kizitonwose Calendar | 2.4.1 | Agenda del asesor |
| JUnit | 4.13.2 | Pruebas unitarias |
| Firebase Rules Unit Testing | 5.0.0 | Pruebas de reglas vía npm |

## Paquetes de código

- `admin`: pantallas, adaptadores, modelos, notificaciones y almacenamiento local administrativo.
- `asesor`: agenda, citas, separaciones, chat y perfil.
- `usuario`: catálogo, detalle, mapa, QR, citas, chat, actividad y perfil.
- `superadmin`: usuarios, invitaciones, solicitudes, logs y reportes globales.
- `data`: repositorios Firebase/Supabase, reglas de negocio, migración y almacenamiento local.
- `entity`: `Proyecto`, `Cita`, `EventoCita`, `Chat`, `MensajeChat`, `Separacion`, `CalendarDay` e `Inmobiliaria`.
- `maps`: configuración, selector y previsualización de ubicación.

## Configuración externa

- Firebase: `.firebaserc`, `firebase.json`, `firestore.rules`, `firestore.indexes.json`, `app/google-services.json`.
- Supabase: `supabase/config.toml`, migraciones, políticas y Edge Functions.
- Secretos locales: `local.properties` y variables de entorno.
- Tests de reglas: `tests/firestore.rules.test.mjs`.

## Navegación principal

| Rol | Barra principal |
|---|---|
| Cliente | Explorar, Actividad, Chats, Perfil |
| Asesor | Mi agenda, Separaciones, Chats, Perfil |
| Administrador | Inicio, Proyectos, Asesores, Reportes, Perfil |
| Superadministrador | Resumen, usuarios, solicitudes/reportes/logs según menú |

## Servicios descartados tras la auditoría

- Flutter: no existe.
- Firebase Realtime Database: no existe.
- Firebase Cloud Functions: no existe.
- Firebase Cloud Messaging: no existe.
- Supabase Auth: no se usa.
- Supabase Postgres como base de negocio: no se usa.
- Supabase Realtime: no se usa.
- Pasarela de pago real: no existe.
