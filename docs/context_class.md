# 📱 Guía de Estilo y Stack Tecnológico - IoT PUCP (1TEL05)



## 🏗 Arquitectura y Estructura
* [cite_start]**Vistas (UI):** Definidas exclusivamente en archivos XML dentro de `res/layout`[cite: 232, 242].
* [cite_start]**Componentes Core:** Uso de `Activities` para representar cada pantalla interactiva[cite: 275].
* [cite_start]**Gestión de Recursos:** - Colores, Strings y Dimensiones deben ir en `res/values/`[cite: 232].
    - [cite_start]Usar `dp` para dimensiones de vistas y `sp` para tamaño de texto[cite: 253].
    - [cite_start]Iconos mediante "Vector Assets" (Material Icons)[cite: 328].

## 🎨 Diseño de Interfaz (UI)
* [cite_start]**Root Layouts:** - `ConstraintLayout`: Uso de restricciones (top, bottom, left, right) para alinear elementos[cite: 246, 247].
    - [cite_start]`LinearLayout`: Para alineación secuencial (vertical/horizontal)[cite: 251].
* **Componentes Obligatorios:**
    - [cite_start]`TextView`, `EditText`, `Button`, `ScrollView`[cite: 242, 255].
    - [cite_start]Para listas complejas, se prefiere `RecyclerView` sobre ScrollView[cite: 258].

## ⚙️ Lógica y Eventos
* [cite_start]**Captura de Clicks:** - Opción A: Atributo `android:onClick` en XML vinculado a un método `public void` en Java[cite: 269].
    - [cite_start]Opción B: `setOnClickListener` dentro de `onCreate()` en Java[cite: 271, 273].
* [cite_start]**Navegación entre Pantallas:** - Uso de `Explicit Intent` para iniciar actividades propias[cite: 281, 282].
    - [cite_start]Paso de datos mediante `putExtra` (llave-valor) y recuperación con `getIntent().get...Extra()`[cite: 286, 291, 294].
* [cite_start]**Regreso de Datos:** Implementar `registerForActivityResult` y `ActivityResultLauncher` para recibir información de una actividad hija[cite: 305, 306].

## 🔄 Ciclo de Vida y Persistencia (Punto Crítico)
* [cite_start]**Activity Lifecycle:** Gestión obligatoria de estados: `onCreate`, `onStart`, `onResume`, `onPause`, `onStop`, `onDestroy`[cite: 314].
* [cite_start]**Configuration Changes (Rotación):** - Guardar estado en `onSaveInstanceState(Bundle outState)` usando `outState.put...()`[cite: 322, 323].
    - [cite_start]Restaurar datos en `onCreate(Bundle savedInstanceState)` validando si el Bundle no es nulo[cite: 324, 325].

## ☰ Menús y App Bar
* [cite_start]**App Bar (Action Bar):** - Inflar mediante `onCreateOptionsMenu`[cite: 330].
    - [cite_start]Manejar clics con `onOptionsItemSelected` usando `item.getItemId()`[cite: 331, 332].
* [cite_start]**Navegación Ancestral (Up Navigation):** Definir `android:parentActivityName` en el `AndroidManifest.xml` para habilitar el botón de retroceso en la App Bar[cite: 303, 304].

## 🔐 Seguridad y Buenas Prácticas
* [cite_start]**Validación:** Validar siempre los campos de entrada tanto en Front-end como en Back-end[cite: 190].
* [cite_start]**Logs:** Usar la clase `Log.d(TAG, Valor)` para depuración en la consola[cite: 240].