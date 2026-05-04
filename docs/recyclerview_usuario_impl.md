# RecyclerView implementados en usuario

Este documento resume los `RecyclerView` que ya existen en el flujo `usuario`, qué clases usan, qué campos tiene cada modelo y a qué pantalla navega cada item.

## Vista general

Los `RecyclerView` implementados son estos:

1. `UsuarioPropiedadesListadoActivity`
2. `UsuarioChatsActivity`
3. `UsuarioActividadActivity`
   - `recyclerAppointments`
   - `recyclerTramites`
   - `recyclerHistory`
4. `UsuarioNotificacionesActivity`

---

## 1. Listado de propiedades

**Screen**

- Activity: [UsuarioPropiedadesListadoActivity.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioPropiedadesListadoActivity.java)
- Layout: [activity_usuario_propiedades_listado.xml](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/res/layout/activity_usuario_propiedades_listado.xml)
- RecyclerView ID: `recyclerPropertyList`

**Clases principales**

- Modelo: [UsuarioPropertyListItem.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioPropertyListItem.java)
- Adapter: [UsuarioPropertyListAdapter.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioPropertyListAdapter.java)
- Item layout: [item_usuario_propiedad_listado.xml](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/res/layout/item_usuario_propiedad_listado.xml)
- Fuente de datos: [UsuarioPropertyCatalog.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioPropertyCatalog.java)

**Campos del modelo `UsuarioPropertyListItem`**

- `propertyId`
- `label`
- `title`
- `location`
- `price`
- `imageResId`

**Qué hace el adapter**

`UsuarioPropertyListAdapter` hace bind de:

- `tvPropertyItemLabel`
- `tvPropertyItemTitle`
- `tvPropertyItemLocation`
- `tvPropertyItemPrice`
- `ivPropertyItemImage`

y el click del item llama a `OnPropertyClickListener`.

**De dónde salen los datos**

En `buildPropertyItems()`, la activity recorre `UsuarioPropertyCatalog.getExploreProperties()` y convierte cada `PropertyDetail` en un `UsuarioPropertyListItem`.

**Navegación**

Cada item abre:

- [UsuarioPropiedadDetalleActivity.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioPropiedadDetalleActivity.java)

Extras enviados:

- `EXTRA_PROPERTY_ID`
- `EXTRA_PROPERTY_TITLE`
- `EXTRA_PROPERTY_PRICE`
- `EXTRA_PROPERTY_LOCATION`

---

## 2. Lista de chats

**Screen**

- Activity: [UsuarioChatsActivity.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioChatsActivity.java)
- Layout: [activity_usuario_chats.xml](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/res/layout/activity_usuario_chats.xml)
- RecyclerView ID: `recyclerChats`

**Clases principales**

- Modelo: [UsuarioChatListItem.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioChatListItem.java)
- Adapter: [UsuarioChatListAdapter.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioChatListAdapter.java)
- Item layout: [item_usuario_chat.xml](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/res/layout/item_usuario_chat.xml)

**Campos del modelo `UsuarioChatListItem`**

- `name`
- `message`
- `time`
- `avatarResId`
- `initials`
- `usesInitials`
- `unread`
- `favorite`

**Qué hace el adapter**

`UsuarioChatListAdapter` hace bind de:

- `ivChatAvatar`
- `chatInitialsContainer`
- `tvChatInitials`
- `tvChatName`
- `tvChatTime`
- `tvChatMessage`

Además:

- si `usesInitials == true`, oculta la imagen y muestra iniciales
- si `usesInitials == false`, muestra avatar real
- tiene `submitItems()` para refrescar la lista cuando cambian filtros

**Filtros implementados**

En `UsuarioChatsActivity` existen 3 filtros:

- `FILTER_ALL`
- `FILTER_UNREAD`
- `FILTER_FAVORITES`

Se aplican sobre la lista `allItems` usando:

- `item.isUnread()`
- `item.isFavorite()`

**Navegación**

Cada item abre:

- [UsuarioChatDetalleActivity.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioChatDetalleActivity.java)

Extra enviado:

- `EXTRA_CONTACT_NAME`

---

## 3. Mi Actividad

**Screen**

- Activity: [UsuarioActividadActivity.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioActividadActivity.java)
- Layout: [activity_usuario_actividad.xml](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/res/layout/activity_usuario_actividad.xml)

Esta screen tiene **3 RecyclerView separados**:

1. `recyclerAppointments`
2. `recyclerTramites`
3. `recyclerHistory`

Todos usan `LinearLayoutManager` y:

- `setNestedScrollingEnabled(false)`

para convivir dentro del scroll general de la screen.

### 3.1 Citas programadas

**Clases**

- Modelo: [UsuarioAppointmentItem.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioAppointmentItem.java)
- Adapter: [UsuarioAppointmentAdapter.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioAppointmentAdapter.java)
- Item layout: [item_usuario_actividad_cita.xml](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/res/layout/item_usuario_actividad_cita.xml)

**Campos del modelo `UsuarioAppointmentItem`**

- `title`
- `status`
- `dateTime`
- `advisor`
- `imageResId`
- `location`
- `note`
- `confirmed`

**Qué hace el adapter**

Hace bind de:

- `ivAppointmentImage`
- `tvAppointmentTitle`
- `tvAppointmentStatus`
- `tvAppointmentDateTime`
- `tvAppointmentAdvisor`

**Navegación**

Cada cita abre:

- [UsuarioCitaDetalleActivity.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioCitaDetalleActivity.java)

Extras enviados:

- `EXTRA_APPOINTMENT_TITLE`
- `EXTRA_APPOINTMENT_STATUS`
- `EXTRA_APPOINTMENT_DATE`
- `EXTRA_APPOINTMENT_ADVISOR`
- `EXTRA_APPOINTMENT_LOCATION`
- `EXTRA_APPOINTMENT_NOTE`
- `EXTRA_APPOINTMENT_CONFIRMED`

### 3.2 Separaciones / trámites

**Clases**

- Modelo: [UsuarioTramiteItem.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioTramiteItem.java)
- Adapter: [UsuarioTramiteAdapter.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioTramiteAdapter.java)
- Item layout: [item_usuario_actividad_tramite.xml](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/res/layout/item_usuario_actividad_tramite.xml)

**Campos del modelo `UsuarioTramiteItem`**

- `title`
- `id`
- `status`
- `note`
- `due`
- `canPay`

**Qué hace el adapter**

Hace bind de:

- `tvTramiteItemTitle`
- `tvTramiteItemId`
- `tvTramiteItemStatus`
- `tvTramiteItemNote`
- `tvTramiteItemDue`
- `btnTramiteItemDetails`

Además cambia comportamiento visual según `canPay`:

- si `canPay == true`
  - `status` usa `app_accent_gold`
  - oculta `tramiteProgressContainer`
  - muestra `tramiteDivider`
  - muestra `tramiteFooter`
- si `canPay == false`
  - `status` usa `app_text_secondary`
  - muestra `tramiteProgressContainer`
  - oculta `tramiteDivider`
  - oculta `tramiteFooter`

El click funciona:

- en toda la card
- y en `btnTramiteItemDetails`

**Navegación**

Cada trámite abre:

- [UsuarioTramiteDetalleActivity.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioTramiteDetalleActivity.java)

Extras enviados:

- `EXTRA_TRAMITE_TITLE`
- `EXTRA_TRAMITE_ID`
- `EXTRA_TRAMITE_STATUS`
- `EXTRA_TRAMITE_NOTE`
- `EXTRA_TRAMITE_DUE`
- `EXTRA_TRAMITE_CAN_PAY`

### 3.3 Historial reciente

**Clases**

- Modelo: [UsuarioHistoryItem.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioHistoryItem.java)
- Adapter: [UsuarioHistoryAdapter.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioHistoryAdapter.java)
- Item layout: [item_usuario_actividad_historial.xml](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/res/layout/item_usuario_actividad_historial.xml)

**Campos del modelo `UsuarioHistoryItem`**

- `badge`
- `title`
- `date`
- `summary`
- `status`
- `code`
- `amount`

**Qué hace el adapter**

Hace bind de:

- `tvHistoryItemBadge`
- `tvHistoryItemTitle`
- `tvHistoryItemDate`
- `tvHistoryItemSummary`

**Navegación**

Cada item de historial abre:

- [UsuarioHistorialDetalleActivity.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioHistorialDetalleActivity.java)

Extras enviados:

- `EXTRA_HISTORY_TITLE`
- `EXTRA_HISTORY_DATE`
- `EXTRA_HISTORY_STATUS`
- `EXTRA_HISTORY_CODE`
- `EXTRA_HISTORY_AMOUNT`
- `EXTRA_HISTORY_SUMMARY`

---

## 4. Notificaciones

**Screen**

- Activity: [UsuarioNotificacionesActivity.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioNotificacionesActivity.java)
- Layout: [activity_usuario_notificaciones.xml](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/res/layout/activity_usuario_notificaciones.xml)
- RecyclerView ID: `recyclerNotifications`

**Clases principales**

- Modelo: [UsuarioNotificationItem.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioNotificationItem.java)
- Adapter: [UsuarioNotificationAdapter.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioNotificationAdapter.java)
- Item layout: [item_usuario_notificacion.xml](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/res/layout/item_usuario_notificacion.xml)

**Constantes del modelo**

- `TYPE_APPROVAL = 1`
- `TYPE_VISIT = 2`
- `ACTION_PAYMENT = 1`
- `ACTION_APPOINTMENT = 2`

**Campos del modelo `UsuarioNotificationItem`**

- `type`
- `time`
- `title`
- `body`
- `ctaLabel`
- `actionType`

**Qué hace el adapter**

El item layout soporta **2 variantes visuales** dentro del mismo XML:

- `notificationApprovalCard`
- `notificationVisitCard`

El adapter decide cuál mostrar según:

- `item.getType() == TYPE_APPROVAL`

También maneja:

- `notificationTimeline`
- `notificationItemCityPhoto`
- `btnNotificationItemCta`

Comportamiento:

- si es aprobación:
  - muestra card de aprobación
  - muestra CTA
  - oculta card de visita
- si es visita:
  - muestra card de visita
  - oculta CTA de aprobación

**Navegación**

La activity decide según `actionType`:

- `ACTION_PAYMENT` -> [UsuarioReservaPagoActivity.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioReservaPagoActivity.java)
- `ACTION_APPOINTMENT` -> [UsuarioCitaDetalleActivity.java](C:/Users/ACER/Downloads/projectIot/Proyecto_-Servicios-y-Aplicaciones-para-IoT/app/src/main/java/com/example/proyecto_iot/usuario/UsuarioCitaDetalleActivity.java)

En el caso de cita, la activity arma el `Intent` con extras de cita mock antes de abrir el detalle.

---

## 5. Resumen de relaciones

### Propiedades

- Activity: `UsuarioPropiedadesListadoActivity`
- Modelo: `UsuarioPropertyListItem`
- Adapter: `UsuarioPropertyListAdapter`
- Layout item: `item_usuario_propiedad_listado.xml`
- Destino: `UsuarioPropiedadDetalleActivity`

### Chats

- Activity: `UsuarioChatsActivity`
- Modelo: `UsuarioChatListItem`
- Adapter: `UsuarioChatListAdapter`
- Layout item: `item_usuario_chat.xml`
- Destino: `UsuarioChatDetalleActivity`

### Actividad

- Activity: `UsuarioActividadActivity`
- Recycler 1:
  - Modelo: `UsuarioAppointmentItem`
  - Adapter: `UsuarioAppointmentAdapter`
  - Layout item: `item_usuario_actividad_cita.xml`
  - Destino: `UsuarioCitaDetalleActivity`
- Recycler 2:
  - Modelo: `UsuarioTramiteItem`
  - Adapter: `UsuarioTramiteAdapter`
  - Layout item: `item_usuario_actividad_tramite.xml`
  - Destino: `UsuarioTramiteDetalleActivity`
- Recycler 3:
  - Modelo: `UsuarioHistoryItem`
  - Adapter: `UsuarioHistoryAdapter`
  - Layout item: `item_usuario_actividad_historial.xml`
  - Destino: `UsuarioHistorialDetalleActivity`

### Notificaciones

- Activity: `UsuarioNotificacionesActivity`
- Modelo: `UsuarioNotificationItem`
- Adapter: `UsuarioNotificationAdapter`
- Layout item: `item_usuario_notificacion.xml`
- Destinos:
  - `UsuarioReservaPagoActivity`
  - `UsuarioCitaDetalleActivity`

---

## 6. Punto importante para entenderlos

En este proyecto, los `RecyclerView` no están conectados a backend. Hoy funcionan con:

- listas mock construidas dentro de las activities
- o con catálogo local (`UsuarioPropertyCatalog`) en el caso de propiedades

Eso significa que, si quieres cambiar textos, imágenes o cantidad de items, normalmente debes revisar:

1. la activity que arma la lista (`build...Items()`)
2. el modelo correspondiente
3. el adapter
4. el item XML

Si quieres, el siguiente paso útil es hacer un segundo `.md` solo con un **diagrama de flujo** tipo:

`screen -> recycler -> adapter -> model -> detail screen`

para que lo entiendas todavía más rápido.
