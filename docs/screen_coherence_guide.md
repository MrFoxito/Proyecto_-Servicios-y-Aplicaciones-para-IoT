# Guia de coherencia visual para nuevas screens

## Objetivo
Esta guia define restricciones claras para que cualquier nueva screen mantenga el mismo estilo que las screens actuales de usuario (Explorar, Actividad, Chats, Perfil).

## 1) Estructura base obligatoria
- Root: `FrameLayout` con `android:background="@color/app_bg"`.
- Contenido: `ScrollView` + `LinearLayout` vertical.
- Padding del contenido: `left/right = @dimen/space_24`.
- Top padding recomendado:
  - 52dp en screens de contenido (Explorar/Actividad/Chats).
  - 32-36dp en Perfil.
- Bottom padding del `ScrollView`: `108dp` (para no chocar con navbar).
- Navbar: incluir siempre `@layout/view_usuario_bottom_nav` al fondo.
- No modificar la estructura visual del navbar.

## 2) Sistema tipografico oficial (obligatorio)
No usar valores sueltos en XML si existe estilo global. Usar `res/values/styles.xml`.

### 2.1 Jerarquia canonica

#### Header 1 (H1) - Titulos de seccion
Ejemplos: "Descubre", "Mi Actividad", "Registrar Separacion".
- Estilo: `TextRoleH1`
- Fuente: Manrope (`@font/manrope_extrabold`)
- Peso visual: 700-800 (implementado 800)
- Tamano: 30sp (rango objetivo 28-32)
- Tracking: `-0.025` (-2.5%)
- Color: `@color/app_text_primary` (`#042534`)

#### Header 2 (H2) - Titulos de tarjetas/subsecciones
Ejemplos: "Proyectos destacados", "Villa Luminara".
- Estilo: `TextRoleH2`
- Fuente: Manrope (`@font/manrope_bold`)
- Peso visual: 600-700 (implementado 700)
- Tamano: 20sp (rango objetivo 18-22)
- Tracking: `-0.01`
- Color: `@color/app_text_primary`

#### Header 3 / Overline - Etiquetas de categoria
Ejemplos: "EDICION LIMITADA", "PREVENTA", "FECHA Y HORA".
- Estilo: `TextRoleH3` (+ variantes `TextTypeLabelCaps*`)
- Fuente: Manrope Bold
- Tamano: 11sp (rango objetivo 10-12)
- Transformacion: mayusculas
- Tracking: 0.06 a 0.10
- Color: `@color/app_accent_gold`, `@color/app_text_muted` o blanco en fondo oscuro

#### Paragraph - Cuerpo de texto
Ejemplos: descripciones de cards, textos descriptivos, mensajes.
- Estilo: `TextRoleParagraph`
- Fuente: Inter Regular (`@font/inter_regular`)
- Peso: 400
- Tamano: 15sp (rango objetivo 14-16)
- Line-height: 24sp (ratio 1.6)
- Color: `@color/app_text_secondary`

#### Caption / Micro-copy
Ejemplos: "Madrid, Espana", "10:24 AM", metadatos tecnicos.
- Estilo: `TextRoleCaption`
- Fuente: Manrope Medium
- Peso: 500
- Tamano: 12sp
- Color: `@color/app_text_muted`

### 2.2 Reglas de implementacion para TODA la app
- Todo texto nuevo debe mapearse a uno de estos roles: H1, H2, H3, Paragraph, Caption.
- No usar `android:textSize`/`android:textStyle` hardcodeado si existe estilo equivalente.
- No mezclar sans-serif del sistema con Manrope/Inter.
- H1 no puede bajar de 28sp ni subir de 32sp.
- Paragraph no puede usar menos de 14sp ni mas de 16sp.

## 3) Colores oficiales (no inventar nuevos)
Tomar solo tokens de `res/values/colors.xml`.

### Texto
- `app_text_primary` `#042534` (petroleum profundo)
- `app_text_secondary` `#1E3B4A`
- `app_text_muted` `#6B7280`
- `app_text_soft` `#94A3B8`

### Base
- `app_bg` `#F3F4F6`
- `app_surface` `#F7F8FA`
- `app_surface_card` `#F7F8FA`
- `app_surface_muted` `#EAEBED`

### Marca/acento
- `app_brand_navy` `#081736`
- `app_brand_navy_2` `#0B2A44`
- `app_accent_gold` `#7A5C0D`

## 4) Espaciado y medidas
Usar `res/values/dimens.xml` y evitar valores sueltos.
- Espaciado permitido: `4, 8, 12, 16, 20, 24, 28, 32, 40dp`
- Altura minima touch: `48dp`

## 5) Responsive y safe area
- Solo `sp` para texto y `dp` para layout.
- Evitar anchos/altos rigidos innecesarios.
- Respetar insets del sistema.
- Mantener `paddingBottom=108dp` en contenido con navbar.
- Probar en al menos 3 anchos: ~360dp, ~411dp, ~430dp.

## 6) Reglas de copy
- Tono premium/editorial.
- Convencion unica para moneda, fecha y hora.
- Evitar truncado agresivo fuera de listas.

## 7) Prompt de control para cualquier modelo/IA (usar tal cual)
Usa este bloque antes de pedir una screen nueva:

```text
Aplica estrictamente el sistema de diseno de la app.
- Tipografia obligatoria:
  - H1 = TextRoleH1 (Manrope 800, 30sp, letterSpacing -0.025, color #042534)
  - H2 = TextRoleH2 (Manrope 700, 20sp, letterSpacing -0.01)
  - H3/Overline = TextRoleH3 (Manrope 700, 11sp, uppercase, letterSpacing 0.08)
  - Paragraph = TextRoleParagraph (Inter 400, 15sp, lineHeight 24sp)
  - Caption = TextRoleCaption (Manrope 500, 12sp)
- No usar textSize/textStyle hardcodeado si existe estilo global.
- Usar solo colores tokenizados en colors.xml.
- Mantener safe area, espaciado consistente y navbar actual.
- Si un texto no encaja, mapearlo al rol mas cercano sin inventar uno nuevo.
```

## 8) Checklist antes de cerrar una screen
- [ ] Todo texto usa estilos globales de tipografia por rol.
- [ ] No hay hardcodes tipograficos innecesarios.
- [ ] Usa paleta oficial (sin colores ad hoc).
- [ ] Sin desborde sobre status bar / navbar del sistema.
- [ ] Navbar visible, limpio y no alterado.
- [ ] Build compila sin errores (`assembleDebug`).

## 9) Backlog oficial de mejora (rol usuario)
Usar este documento como fuente de hallazgos y prioridades:
- `docs/usuario_uiux_auditoria.md`

## 10) Concepto visual oficial de la app (usar siempre)
Aqui tienes el desglose detallado:

### 1. Concepto Visual: "Petroleum & Parchment" (Petroleo y Pergamino)
El diseno busca emular una revista de arquitectura de alta gama. Se basa en el uso de capas (layering) y profundidad tonal en lugar de bordes o lineas divisorias, lo que crea una interfaz limpia pero con mucha textura visual.

### 2. Paleta de Colores (principales de la app)
Color Primario: Petroleum Blue (`#1E3B4A` / `#042534`)  
Uso: Botones principales, estados activos, tipografia de titulos (H1) y acentos de marca. Representa confianza, profundidad y exclusividad.

Color de Fondo/Superficie: Stone & Parchment (`#F9F9F9` / `#F3F3F3`)  
Uso: Fondos de pantalla, tarjetas y superficies elevadas. No usar blanco puro, sino tonos piedra sutiles que recuerdan al papel de alta calidad.

Color de Acento: Soft Gold / Amber (`#745B00`)  
Uso: Etiquetas de categoria (como "EDICION LIMITADA"), iconos especiales o estados de "Pendiente". Aporta el toque de lujo sin saturar.

Escala de Grises: Slate & Zinc  
Uso: Texto de cuerpo (Paragraph), datos tecnicos y estados inactivos. Usar grises azulados para mantener la armonia con el Petroleum Blue.

### 3. Principios de diseno
- Ausencia de Bordes: la separacion entre elementos se logra exclusivamente mediante cambios sutiles de tono entre superficies y sombras suaves para crear elevacion.
- Composicion Editorial: mucho espacio negativo (aire), alineaciones asimetricas y uso de imagenes de gran formato que respiren en la pantalla.
- Efectos de Capa: uso de glassmorphism (desenfoque de fondo) en barras de navegacion y modales para dar sensacion de profundidad fisica.

### 4. Resumen de estilo
Es un tema claro (Light Mode) por defecto, con contraste alto en tipografia para asegurar legibilidad, manteniendo una apariencia sobria, premium y funcional.

### Regla obligatoria de uso
- Estos son los colores y lineamientos principales de diseno de la app.
- Para nuevas screens o cambios visuales, usar siempre esta base.
- Si hay duda entre dos tonos o estilos, priorizar este bloque (seccion 10) y la jerarquia tipografica de la seccion 2.
