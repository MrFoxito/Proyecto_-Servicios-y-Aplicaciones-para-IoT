# Session Fix Log — Proyecto IoT Android App

**Fecha:** 24 Mayo 2026  
**Agente:** Kiro  
**Objetivo:** Auditar y corregir el rol cliente de la app Android para que funcione con LocalStorage real en lugar de datos hardcodeados o seed data falso.

---

## Contexto del proyecto

App Android nativa (Java) para una inmobiliaria de lujo llamada **The Editorial Estate**.  
Tiene 4 roles: `superadmin`, `admin`, `asesor`, `cliente`.  
No hay backend ni API — todo funciona con **SharedPreferences** simulando una base de datos local (`LocalSchemaStorage.java`).

**Estructura clave:**
- `AuthSessionManager.java` — maneja sesión activa (rol, userId, nombre, email, teléfono)
- `LocalSchemaStorage.java` — base de datos local en SharedPreferences con 16 colecciones JSON
- `UsuarioPropertyCatalog.java` — catálogo de propiedades hardcodeado en Java (5 propiedades)
- Roles en `AuthSessionManager`: `"user"`, `"asesor"`, `"admin"`, `"superadmin"`

---

## Problema inicial encontrado

El proyecto compilaba pero tenía múltiples problemas funcionales en el rol cliente:

1. **JDK incorrecto** en `gradle.properties` — apuntaba a Eclipse Adoptium que no existía
2. **`local.properties` faltaba** — no tenía la ruta del Android SDK
3. **Login sin validación real** — mostraba un `AlertDialog` para elegir rol manualmente
4. **Registro no guardaba contraseña** ni `userId` en sesión
5. **Perfil mostraba nombre hardcodeado** "Julian Thorne" del strings.xml
6. **Datos personales no cargaban ni guardaban** nada real
7. **Home tenía IDs de propiedades hardcodeados** en el código Java
8. **Seed data falso** en citas, trámites e historial — todos los usuarios veían los mismos datos
9. **Sin filtro por usuario** — `getUserAppointments()`, `getUserTramites()`, `getUserHistory()` devolvían todo sin filtrar
10. **Agendar cita** usaba `EditText` de texto libre para fecha y hora — formatos inconsistentes
11. **Reserva/Pago** no escribía nada en el storage al confirmar
12. **Mi Actividad** no se recargaba al volver — requería cerrar sesión para ver cambios
13. **Crash al abrir Registro** — `NullPointerException` por casteo incorrecto de `ImageButton` como `Button`

---

## Cambios realizados — archivo por archivo

### `gradle.properties`
- Cambió `org.gradle.java.home` de `C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot` a `C:\Program Files\Java\jdk-21.0.10`

### `local.properties` (creado)
- Creado con `sdk.dir=C\:\\Users\\ACER\\AppData\\Local\\Android\\Sdk`

---

### `AuthSessionManager.java`
**Antes:** Solo guardaba `registered`, `logged_in`, `role`.  
**Después:** Guarda también `user_id`, `user_name`, `user_email`, `user_phone`.

Métodos nuevos:
- `markRegisteredAndLoggedIn(userId, name, email, phone)` — versión con datos completos
- `getUserId()` — retorna el ID del usuario logueado
- `getUserName()` — retorna el nombre
- `getUserEmail()` — retorna el email
- `getUserPhone()` — retorna el teléfono
- `updateUserProfile(name, email, phone)` — actualiza datos sin cambiar sesión
- `logout()` — ahora también limpia userId, nombre, email, teléfono

---

### `LocalSchemaStorage.java`
**Cambio de versión:** `KEY_INITIALIZED` pasó de `"initialized_v3"` a `"initialized_v4"` para forzar re-seed en dispositivos existentes.

**Seed data modificado:**
- `seedUsuarios()` — ahora incluye campo `"password"` en todos los usuarios del seed
- `seedCitas()` — ahora retorna `new JSONArray()` (vacío)
- `seedTramites()` — ahora retorna `new JSONArray()` (vacío)
- `seedHistorial()` — ahora retorna `new JSONArray()` (vacío)

**Credenciales del seed (para pruebas):**
| Email | Password | Rol |
|---|---|---|
| `alicia.velarde@mail.com` | `cliente123` | cliente |
| `julian.mendoza@mail.com` | `cliente123` | cliente |
| `evaldes@editorialestate.com` | `asesor123` | asesor |
| `jcosta@editorialestate.com` | `asesor123` | asesor |
| `smendez@editorialestate.com` | `asesor123` | asesor |
| `admin@editorialestate.com` | `admin123` | admin |
| `superadmin@estate.pe` | `super123` | superadmin |

**Métodos modificados:**
- `getUserAppointments(String clienteId)` — ahora filtra por `clienteId` en el JSON
- `getUserTramites(String clienteId)` — ahora filtra por `clienteId`
- `getUserHistory(String clienteId)` — ahora filtra por `clienteId`
- `addCita(clienteId, clienteNombre, inmuebleNombre, ...)` — firma extendida, guarda `clienteId` y `clienteNombre` reales
- `addUsuario(...)` — ahora llama a `addUsuarioAndGetId` internamente
- `addUsuarioAndGetId(fullName, email, phone, password)` — firma extendida con password

**Métodos nuevos:**
- `getUserByCredentials(email, password)` — busca usuario por email+password, retorna `JSONObject` o `null`
- `getUserById(userId)` — busca usuario por ID
- `updateUsuario(userId, fullName, email, phone, city)` — actualiza datos del usuario en la colección
- `addTramite(clienteId, propertyTitle, amount)` — crea trámite con estado `EN REVISION`, retorna el código `#TE-XXXXX`
- `addHistorial(clienteId, propertyTitle, amount, tramiteId)` — crea registro en historial con estado `COMPLETADO`

---

### `LoginActivity.java`
**Antes:** Mostraba `AlertDialog` con 4 opciones de rol para elegir manualmente. No validaba nada.  
**Después:** Valida email + password contra `LocalSchemaStorage.getUserByCredentials()`. Si las credenciales son correctas, lee el rol del usuario y navega a la pantalla correcta. Si son incorrectas, muestra Toast de error.

Lógica de navegación por rol:
- `"user"` → `UsuarioHomeActivity`
- `"asesor"` → `AsesorHomeActivity`
- `"admin"` → `AdminHomeActivity`
- `"superadmin"` → `SuperadminResumenActivity`

Para el rol `"user"`, también llama a `markRegisteredAndLoggedIn()` con todos los datos del usuario para poblar la sesión completa.

---

### `RegisterActivity.java`
**Antes:** Crasheaba al abrir por `NullPointerException` — declaraba `backButton` como `Button` pero en el layout es `ImageButton`.  
**Después:**
- `backButton` declarado como `View` (compatible con cualquier tipo)
- Todos los elementos (`register`, `openLogin`, `backButton`) tienen verificación de null
- Llama a `addUsuarioAndGetId(name, mail, phoneValue, pass)` con contraseña
- Llama a `markRegisteredAndLoggedIn(newUserId, name, mail, phoneValue)` con datos completos

---

### `UsuarioHomeActivity.java`
**Antes:** Tenía IDs hardcodeados `UsuarioPropertyCatalog.ID_VILLA_LUMINARA`, etc.  
**Después:** Lee las propiedades del storage con `getUserPropertyListItems()` y asigna dinámicamente:
- Tarjetas featured: posición 0 y 2 del storage
- Filas populares: posición 1 y 3 del storage
- Textos (label, título, meta, precio) actualizados desde el storage
- Imágenes actualizadas desde el storage
- Clicks navegan al detalle con datos reales

---

### `UsuarioPerfilActivity.java`
**Antes:** Mostraba "Julian Thorne" hardcodeado del strings.xml.  
**Después:** Lee `AuthSessionManager.getUserName()` y lo muestra en `tvProfileName`.

---

### `UsuarioDatosPersonalesActivity.java`
**Antes:** Botón "Guardar" solo mostraba un Toast. No cargaba ni guardaba nada.  
**Después:**
- Al abrir: carga datos del usuario desde `LocalSchemaStorage.getUserById()` + `AuthSessionManager`
- Campos: nombre, email, teléfono, ciudad, canal preferido
- Al guardar: escribe en `AuthSessionManager.updateUserProfile()` y `LocalSchemaStorage.updateUsuario()`
- Actualiza el resumen visual en tiempo real

---

### `UsuarioActividadActivity.java`
**Antes:** Cargaba datos en `onCreate()` — no se actualizaba al volver de otra pantalla.  
**Después:**
- `onCreate()` solo llama a `setupUserBottomNav()`
- Los 3 RecyclerViews se cargan en `onResume()` — se recargan cada vez que la pantalla vuelve a ser visible
- Los 3 métodos de consulta pasan `getClienteId()` para filtrar por usuario logueado
- `getClienteId()` lee de `AuthSessionManager.getUserId()`

---

### `UsuarioCitaConfirmacionActivity.java`
**Antes:** Llamaba `addCita(propertyTitle, date, time, ...)` con firma antigua y `clienteNombre` hardcodeado como "Alicia Velarde".  
**Después:** Llama `addCita(clienteId, clienteNombre, propertyTitle, ...)` con datos reales de la sesión activa.

---

### `UsuarioAgendarCitaActivity.java`
**Antes:** Campos de fecha y hora eran `EditText` de texto libre — el usuario podía escribir cualquier formato.  
**Después:**
- **Fecha:** campo no editable, abre `DatePickerDialog` al tocar. Fechas pasadas bloqueadas con `setMinDate(hoy)`. Máximo 1 año hacia adelante. Formato guardado: `"26 Oct 2026"`
- **Hora:** campo no editable, abre `TimePickerDialog` al tocar. Formato 12h con AM/PM: `"11:30 AM"`
- **Contacto:** pre-llenado automático con `AuthSessionManager.getUserPhone()`. Validación mínima de 7 caracteres
- **Validaciones en orden:** fecha → hora → contacto (mínimo 7 chars)
- Botón "CONFIRMAR CITA" cambió de `user_detail_primary_button_bg` (gradiente oscuro) a `app:backgroundTint="@color/app_brand_navy_2"` para coincidir con el botón "AGENDAR CITA" del detalle de propiedad

---

### `UsuarioReservaPagoActivity.java`
**Antes:** Pantalla completamente estática. Botón "PROCEDER AL PAGO" solo mostraba Toast y navegaba.  
**Después:**
- Recibe `EXTRA_PROPERTY_TITLE`, `EXTRA_PROPERTY_PRICE`, `EXTRA_PROPERTY_LOCATION` por Intent
- Muestra el título y precio reales de la propiedad seleccionada
- Al presionar "PROCEDER AL PAGO":
  1. Llama `LocalSchemaStorage.addTramite(clienteId, propertyTitle, propertyPrice)` → aparece en Separaciones en curso
  2. Llama `LocalSchemaStorage.addHistorial(clienteId, propertyTitle, propertyPrice, tramiteId)` → aparece en Historial reciente
  3. Navega a `UsuarioActividadActivity` donde los items aparecen al instante

Constantes públicas nuevas: `EXTRA_PROPERTY_TITLE`, `EXTRA_PROPERTY_PRICE`, `EXTRA_PROPERTY_LOCATION`

---

### `UsuarioPropiedadDetalleActivity.java`
**Antes:** Botón "SEPARAR INMUEBLE" abría `UsuarioReservaPagoActivity` sin pasar ningún dato.  
**Después:** Pasa `EXTRA_PROPERTY_TITLE`, `EXTRA_PROPERTY_PRICE`, `EXTRA_PROPERTY_LOCATION` leídos de los TextViews del detalle.

---

### Layouts modificados

**`activity_usuario_perfil.xml`**
- Agregado `android:id="@+id/tvProfileName"` al TextView del nombre

**`activity_usuario_datos_personales.xml`**
- Agregados IDs: `tvPersonalSummaryName`, `tvPersonalSummaryContact`
- Agregados IDs a los 5 EditTexts: `etPersonalName`, `etPersonalEmail`, `etPersonalPhone`, `etPersonalCity`, `etPersonalPrefContact`

**`activity_usuario_home.xml`**
- Agregados IDs a los TextViews de las filas populares: `tvPopularLabel1`, `tvPopularTitle1`, `tvPopularMeta1`, `tvPopularPrice1`, `tvPopularLabel2`, `tvPopularTitle2`, `tvPopularMeta2`, `tvPopularPrice2`

**`activity_usuario_agendar_cita.xml`**
- `inputAppointmentDate`: `inputType="none"`, `focusable="false"`, `focusableInTouchMode="false"`, `cursorVisible="false"`
- `inputAppointmentTime`: mismos atributos
- Botón `btnConfirmAppointment`: cambió de `android:background="@drawable/user_detail_primary_button_bg"` a `app:backgroundTint="@color/app_brand_navy_2"`

**`activity_usuario_reserva_pago.xml`**
- Agregado `android:id="@+id/tvReservaPropertyTitle"` al TextView del título en el hero
- Agregado `android:id="@+id/tvReservaPropertyValue"` al TextView del precio
- Agregado `android:id="@+id/tvReservaPropertyLocation"` al TextView de ubicación/plan

---

## Estado final de flujos — Rol Cliente

### ✅ Funcionan completo
| Flujo | Notas |
|---|---|
| Registro | Guarda nombre, email, teléfono, contraseña. Crea userId único |
| Login real | Valida contra storage. Redirige por rol |
| Logout | Limpia sesión completa |
| Home | Lee propiedades del storage dinámicamente |
| Listado de propiedades | Lee del storage |
| Detalle de propiedad | Datos del catálogo por propertyId |
| Agendar cita | DatePicker + TimePicker + contacto pre-llenado + validaciones |
| Confirmación de cita | Guarda cita con clienteId en storage |
| Mi Actividad — Citas | Filtrado por clienteId, recarga en onResume |
| Mi Actividad — Trámites | Filtrado por clienteId, recarga en onResume |
| Mi Actividad — Historial | Filtrado por clienteId, recarga en onResume |
| Detalle de cita | Datos reales del Intent |
| Detalle de trámite | Datos reales, botón pagar/contactar |
| Detalle de historial | Datos reales |
| Notificaciones | Filtrado por recipientRole: "cliente" |
| Chats (lista) | Filtrado por viewFor: "cliente", filtros funcionales |
| Perfil — nombre | Nombre real del usuario logueado |
| Datos personales | Carga y guarda datos reales |
| Separar inmueble → Pago | Recibe datos reales, simula pago, escribe trámite + historial |

### ⚠️ Funcionan parcialmente
| Flujo | Problema pendiente |
|---|---|
| Chat detalle | Mensajes previos son del seed global, no filtrados por conversación. Mensajes nuevos se agregan visualmente pero no persisten al cerrar |
| Notificaciones → Detalle de cita | Abre detalle con strings fijos del XML, no con la cita real del storage |
| Mapa de exploración | Tarjetas tienen IDs hardcodeados (ID_VILLA_LUMINARA, ID_REFUGIO_CELESTE), no lee del storage |

### ❌ Sin lógica real (pantallas decorativas)
| Flujo | Estado |
|---|---|
| Métodos de pago | Solo muestra tarjetas del layout. "Agregar" solo muestra Toast |
| Preferencias | "Guardar" solo muestra Toast |
| Seguridad | "Revisar accesos" solo muestra Toast |

---

## Cosas importantes a tener en cuenta

### Sobre el storage
- El storage usa `KEY_INITIALIZED = "initialized_v4"`. Si se cambia algo en el seed, hay que incrementar esta versión para que los dispositivos existentes re-inicialicen.
- Las colecciones `citas`, `tramites` e `historial` arrancan **vacías** — el cliente las llena con sus acciones.
- Las colecciones `proyectos`, `conversaciones`, `notificaciones`, `usuarios`, etc. tienen seed data del sistema que sí se mantiene.
- El campo `clienteId` es el que conecta citas/trámites/historial con el usuario. Sin él, los filtros no funcionan.

### Sobre el catálogo de propiedades
- `UsuarioPropertyCatalog.java` tiene 5 propiedades hardcodeadas en Java con todos sus datos (precio, descripción, amenidades, imágenes).
- Los proyectos en el storage tienen un campo `propertyId` que mapea a los IDs del catálogo (`ID_VILLA_LUMINARA`, etc.).
- Si se quieren agregar propiedades dinámicas desde el admin, hay que extender tanto el catálogo como el storage.

### Sobre el login
- El login valida email + password en texto plano — no hay hashing. Aceptable para un prototipo local pero no para producción.
- Los usuarios del seed tienen passwords simples (`cliente123`, `admin123`, etc.).
- Al registrarse, el usuario siempre queda con rol `"cliente"`. No hay flujo de registro para asesores o admins desde la app.

### Sobre la navegación
- `UsuarioActividadActivity` usa `onResume()` para recargar datos — esto es intencional para que los cambios aparezcan inmediatamente al volver.
- Las otras activities del cliente aún usan `onCreate()` — si se necesita recarga en tiempo real en otras pantallas, aplicar el mismo patrón.

### Sobre el APK
- El APK de debug está en: `app/build/outputs/apk/debug/app-debug.apk`
- Para compilar: `.\gradlew.bat assembleDebug` desde la raíz del proyecto
- JDK requerido: Java 21 en `C:\Program Files\Java\jdk-21.0.10`
- Android SDK en: `C:\Users\ACER\AppData\Local\Android\Sdk`
