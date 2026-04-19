# Auditoria UI/UX - Rol Usuario

Fecha: 2026-04-19

Este documento resume los problemas detectados en las pantallas del rol usuario (Explore, Actividad, Chats, Perfil, Detalle, Reserva, Notificaciones y Chat detalle), ordenados por impacto.

## P0 - Critico

1. **Jerarquia tipografica desproporcionada para mobile**
   - Hay titulos y cifras con `sp` excesivos que rompen lectura y balance visual.
   - Referencias:
     - `app/src/main/res/layout/activity_usuario_propiedad_detalle.xml:83`
     - `app/src/main/res/layout/activity_usuario_propiedad_detalle.xml:127`
     - `app/src/main/res/layout/activity_usuario_reserva_pago.xml:208`
     - `app/src/main/res/layout/activity_usuario_reserva_pago.xml:235`

2. **Componentes con tamanos fijos que no escalan bien**
   - Cards y CTAs con anchos/altos rigidos generan cortes y desproporcion en pantallas pequenas.
   - Referencias:
     - `app/src/main/res/layout/activity_usuario_home.xml:157`
     - `app/src/main/res/layout/activity_usuario_home.xml:196`
     - `app/src/main/res/layout/activity_usuario_chat_detalle.xml:196`

3. **Chat detalle no funcional para escribir**
   - El campo de entrada parece input pero no es editable real.
   - Referencia:
     - `app/src/main/res/layout/activity_usuario_chat_detalle.xml:313`

## P1 - Alto

4. **Botones implementados como `TextView` clickeable**
   - Impacta accesibilidad, estados de boton y feedback tactil.
   - Referencias:
     - `app/src/main/res/layout/activity_usuario_propiedad_detalle.xml:467`
     - `app/src/main/res/layout/activity_usuario_propiedad_detalle.xml:473`
     - `app/src/main/res/layout/activity_usuario_reserva_pago.xml:303`
     - `app/src/main/res/layout/activity_usuario_notificaciones.xml:88`

5. **Accesibilidad incompleta**
   - Iconos clickeables sin `contentDescription` y algunos objetivos tactiles pequenos.
   - Referencias:
     - `app/src/main/res/layout/activity_usuario_home.xml:59`
     - `app/src/main/res/layout/activity_usuario_notificaciones.xml:24`
     - `app/src/main/res/layout/activity_usuario_chat_detalle.xml:24`
     - `app/src/main/res/values/dimens.xml:21`

6. **Navegacion por tabs con `finish()`**
   - Puede perder estado/scroll y da sensacion de salto brusco.
   - Referencia:
     - `app/src/main/java/com/example/proyecto_iot/usuario/BaseUsuarioActivity.java:64`

7. **CTA principal de pago en placeholder**
   - La accion critica del flujo aun no esta resuelta.
   - Referencia:
     - `app/src/main/java/com/example/proyecto_iot/usuario/UsuarioReservaPagoActivity.java:31`

## P2 - Medio

8. **Sin sistema centralizado de textos**
   - Casi todo esta hardcodeado en layout; dificulta consistencia y cambios.
   - Referencia:
     - `app/src/main/res/values/strings.xml:2`

9. **Inconsistencia de copy y formatos**
   - Moneda, mayusculas, acentos y formato horario no uniformes.
   - Referencias:
     - `app/src/main/res/layout/activity_usuario_home.xml:238`
     - `app/src/main/res/layout/activity_usuario_chat_detalle.xml:188`
     - `app/src/main/res/layout/activity_usuario_reserva_pago.xml:110`
     - `app/src/main/res/layout/activity_usuario_chats.xml:248`

10. **Iconografia con familias mezcladas**
    - Mezcla de `@android:drawable` y assets propios; grosor visual inconsistente.
    - Referencias:
      - `app/src/main/res/layout/activity_usuario_home.xml:66`
      - `app/src/main/res/layout/activity_usuario_reserva_pago.xml:34`
      - `app/src/main/res/layout/activity_usuario_chat_detalle.xml:29`

11. **Layout fragil en timeline de notificaciones**
    - Uso de linea fija + margen negativo, propenso a romperse por densidad/tamano.
    - Referencias:
      - `app/src/main/res/layout/activity_usuario_notificaciones.xml:160`
      - `app/src/main/res/layout/activity_usuario_notificaciones.xml:162`

12. **Flujo dummy repetitivo en interacciones**
    - Multiples items terminan abriendo la misma vista sin variacion contextual.
    - Referencias:
      - `app/src/main/java/com/example/proyecto_iot/usuario/UsuarioHomeActivity.java:20`
      - `app/src/main/java/com/example/proyecto_iot/usuario/UsuarioChatsActivity.java:19`

## Recomendacion de ejecucion

- **Sprint 1 (P0):** corregir escala tipografica, eliminar tamanos rigidos y hacer funcional el input de chat.
- **Sprint 2 (P1):** accesibilidad completa, reemplazo de `TextView` por controles semanticos y mejora de navegacion por tabs.
- **Sprint 3 (P2):** unificar copy/formato, iconografia y limpieza de hacks de layout.
