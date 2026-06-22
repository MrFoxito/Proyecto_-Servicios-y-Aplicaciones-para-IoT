# Fix de pantallas (responsive + tipografia)

## Objetivo
Ajustar las pantallas para que mantengan el mismo estilo visual en distintos tamanos de moviles, sin desbordes, con tipografia consistente y buena adaptacion.

## Cambios obligatorios
- Eliminar `px` en textos y usar solo `sp` para fuentes.
- Usar solo `dp` para margenes, paddings, anchos y altos.
- Centralizar medidas en `res/values/dimens.xml` (espaciados, radios, alturas, iconos).
- Centralizar tipografia en estilos (`res/values/styles.xml`) para no repetir `textSize`, `fontFamily`, `textStyle`.
- Unificar jerarquia tipografica (h1, h2, body, caption, labels).

## Sistema tipografico global (definicion exacta)
Objetivo visual: look editorial sobrio como la referencia, con titulos fuertes y metadata compacta.

### Font family (global)
- Primaria: `sans-serif`
- Semibold/Medium: `sans-serif-medium`

### Font weights
- Regular: `400`
- Medium: `500`
- Bold: `700`

### Escala tipografica base (usar en styles.xml)
- `type_display_sm`: 42sp, 700, `sans-serif`, lineHeight 48sp, letterSpacing 0
- `type_title_lg`: 38sp, 700, `sans-serif`, lineHeight 42sp, letterSpacing 0
- `type_title_md`: 34sp, 700, `sans-serif`, lineHeight 38sp, letterSpacing 0
- `type_title_sm`: 30sp, 700, `sans-serif`, lineHeight 34sp, letterSpacing 0
- `type_body_lg`: 20sp, 400, `sans-serif`, lineHeight 28sp, letterSpacing 0
- `type_body_md`: 18sp, 400, `sans-serif`, lineHeight 25sp, letterSpacing 0
- `type_body_sm`: 16sp, 400, `sans-serif`, lineHeight 22sp, letterSpacing 0.005
- `type_label_caps_lg`: 13sp, 500, `sans-serif-medium`, lineHeight 16sp, letterSpacing 0.14
- `type_label_caps_md`: 12sp, 500, `sans-serif-medium`, lineHeight 15sp, letterSpacing 0.12
- `type_label_caps_sm`: 11sp, 500, `sans-serif-medium`, lineHeight 14sp, letterSpacing 0.10
- `type_nav_label`: 11sp, 500, `sans-serif-medium`, lineHeight 14sp, letterSpacing 0.08
- `type_price_lg`: 40sp, 700, `sans-serif`, lineHeight 44sp, letterSpacing 0
- `type_price_md`: 36sp, 700, `sans-serif`, lineHeight 40sp, letterSpacing 0

### Jerarquia por tipo de texto en la app
- Header principal (`Descubre`): `type_title_md`
- Subheader descriptivo (`Hallazgos...`): `type_body_lg`
- Titulo de seccion (`Proyectos destacados`, `Mas populares`): `type_title_sm`
- Subtitulo de seccion (`Excelencia...`, `Tendencias...`): `type_body_sm`
- CTA lateral (`VER TODO`): `type_label_caps_lg`
- Badge sobre card (`EDICION LIMITADA`): `type_label_caps_md`
- Nombre de propiedad (`Villa Luminara`): `type_title_sm` con peso 400
- Ubicacion/metadata (`VALENCIA, ESPANA`): `type_label_caps_md`
- Precio principal (`1.450.000EUR`): `type_price_lg`
- Texto de tarjeta popular (`The Iron Works`): `type_title_md` con peso 400
- Metadata de tarjeta popular (`Brooklyn...`): `type_body_sm`
- Precio en tarjeta popular (`$2.100.000`): `type_price_md`
- Labels navbar (`EXPLORAR`, etc.): `type_nav_label`

### Reglas de consistencia
- No mezclar pesos dentro de un mismo nivel (ej. titulos siempre 700 salvo nombre de propiedad).
- No usar `textStyle=\"bold\"` suelto; definir todo por estilo tipografico.
- Mantener letterSpacing en textos caps/labels; no aplicarlo a titulos normales.
- Mantener lineHeight fijo por nivel para evitar saltos visuales entre dispositivos.

## Sistema de color global (definicion exacta)
Objetivo visual: elegante, sobrio y premium, con base fria y acento dorado.

### Paleta principal (tokens + HEX)
- `color_bg_app`: `#F3F4F6` (fondo general)
- `color_surface`: `#F7F8FA` (superficies suaves)
- `color_surface_muted`: `#EAEBED` (inputs/buscador)
- `color_text_primary`: `#04192B` (titulos principales)
- `color_text_secondary`: `#1F2429` (subtitulos fuertes)
- `color_text_muted`: `#747B86` (metadata secundaria)
- `color_text_soft`: `#90979F` (placeholders)
- `color_brand_navy`: `#081736` (activo navbar / bloques oscuros)
- `color_brand_navy_2`: `#0B2A44` (bordes/acento frio oscuro)
- `color_accent_gold`: `#7A5C0D` (acciones y etiquetas premium)
- `color_white`: `#FFFFFF`
- `color_black`: `#000000`

### Paleta funcional por componentes
- Header iconos: `color_text_primary`
- Search bg: `color_surface_muted`
- Search icon/text hint: `color_text_soft`
- CTA textual (ej. `VER TODO`): `color_accent_gold`
- Card badge bg: `#EAF0F4`
- Card badge text: `#13212D`
- Precio destacado: `color_accent_gold`
- Bottom nav container: `color_surface`
- Bottom nav activo bg: `color_brand_navy`
- Bottom nav activo texto/icono: `#F4F6FA`
- Bottom nav inactivo texto/icono: `#9AA5B7` / `#94A0B2`
- Card mapa gradiente: `#0B3B50` -> `#79BDC0`
- Boton mapa bg: `#7F5D00`, texto `#F8F7EE`

### Reglas de color
- No introducir nuevos azules/dorados fuera de tokens.
- Titulos siempre en `color_text_primary`.
- Subtitulos y cuerpo corto en `color_text_secondary`.
- Metadata/ayuda en `color_text_muted`.
- Placeholders siempre en `color_text_soft`.
- Cualquier CTA premium en `color_accent_gold`.

## Espaciado, radios y forma
Definir en `dimens.xml`:
- Espaciado base: `4dp, 8dp, 12dp, 16dp, 20dp, 24dp, 28dp, 32dp, 40dp`
- Radio `sm`: `12dp` (search/input)
- Radio `md`: `16dp` (cards secundarias)
- Radio `lg`: `24dp` (cards principales / mapa)
- Radio `xl`: `28dp` (card hero)

Reglas:
- Distancia entre secciones grandes: `28dp` a `40dp`.
- Distancia titulo-subtitulo: `4dp` a `8dp`.
- Distancia bloque-search a siguiente seccion: `32dp` a `40dp`.

## Bordes y elevacion
- Estilo general plano, casi sin sombras.
- Si se usa borde, grosor `1dp` o `2dp` maximo.
- Cards hero: borde oscuro suave (`color_brand_navy_2`) sin sombra pesada.
- Navbar: elevacion baja (`6dp`-`8dp`) solo para separarlo del contenido.

## Iconografia
- Iconos tipo Material, trazo limpio.
- Tamano estandar:
  - Navegacion/top: `18dp`-`20dp`
  - Search: `20dp`-`22dp`
- Tint por estado:
  - Activo: `#F4F6FA`
  - Inactivo: `#94A0B2`
  - Header: `color_text_primary`

## Componentes clave (receta visual)
- Header principal:
  - Titulo: `type_title_md`, `color_text_primary`
  - Descripcion: `type_body_lg`, `color_text_secondary`
- Buscador:
  - Fondo `color_surface_muted`, radio `12dp`
  - Placeholder `type_body_sm`, `color_text_soft`
- Titulos de seccion:
  - `type_title_sm`, `color_text_primary`
- Subtitulos de seccion:
  - `type_body_sm`, `color_text_secondary`
- Etiquetas caps (`VER TODO`, ubicacion):
  - `type_label_caps_lg/md`, `color_accent_gold` o `color_text_secondary`
- Precio:
  - `type_price_lg/md`, `color_accent_gold` o `color_text_primary` segun bloque
- Bottom nav:
  - Fondo `color_surface`, item activo `color_brand_navy`
  - Label `type_nav_label`

## Personalidad visual de la app
- Estilo: editorial premium inmobiliario.
- Sensacion: limpia, aireada, elegante, con contraste controlado.
- Evitar:
  - Saturacion alta
  - Demasiadas sombras
  - Colores fuera de paleta
  - Tipografias mezcladas

## Checklist de fidelidad visual (contra referencia)
- Tipografia coincide por nivel (tamano/peso/lineHeight/spacing).
- Paleta coincide por bloque y estados.
- Espaciado vertical consistente entre secciones.
- Radios consistentes en cards, search y navbar.
- Iconos con tamano/tint correcto.
- Bottom nav legible y estable en distintos tamanos de pantalla.

## Layout responsive
- Evitar tamanos fijos grandes (ej. cards con ancho/alto rigido) cuando sea posible.
- Preferir `ConstraintLayout` en secciones complejas para alinear y escalar mejor.
- Usar `0dp` + constraints/weights en lugar de valores duros cuando aplique.
- Definir `minHeight` en botones/cards para mantener usabilidad sin romper layout.
- Usar `RecyclerView` para listas/carruseles (mejor que `ScrollView` horizontal con vistas fijas).

## Safe area / desbordes
- Aplicar insets de sistema en todas las pantallas (no solo superadmin):
  - Login
  - Register
  - Usuario (home, actividad, chats, perfil)
  - Admin / Asesor
- Asegurar padding inferior del contenido cuando exista bottom nav para no solaparse con barra de gestos/sistema.
- Verificar estado con barra de navegacion por botones y por gestos.

## Navbar de usuario
- Mantener altura minima de 56dp.
- Objetivos tactiles de al menos 48dp.
- Iconos entre 18dp y 20dp.
- Labels del navbar en 11sp-12sp con el mismo peso en todos los tabs.
- Estado activo/inactivo con contraste claro y consistente.

## Variantes por tamano de pantalla
Crear carpetas de valores para adaptar escala sin duplicar layouts:
- `res/values-sw320dp/`
- `res/values-sw360dp/`
- `res/values-sw411dp/`
- `res/values-sw600dp/` (tablet)

En esas carpetas ajustar:
- Tipografias (`sp`)
- Espaciados (`dp`)
- Alturas de componentes clave

## Accesibilidad y estabilidad visual
- Probar con tamano de fuente del sistema 1.15x y 1.30x.
- Evitar cortes de texto: usar `maxLines` y `ellipsize` solo donde sea necesario.
- Mantener contraste adecuado en textos secundarios y labels pequenas.

## Pantallas a priorizar
1. `activity_usuario_home.xml`
2. `view_usuario_bottom_nav.xml`
3. `activity_login.xml`
4. `activity_register.xml`

## Criterio de terminado
- No hay desbordes en moviles pequenos ni medianos.
- Tipografia consistente entre secciones.
- Navbar no se solapa con sistema.
- Escala visual estable en diferentes tamanos de pantalla.
- Misma identidad visual en todo el flujo.
