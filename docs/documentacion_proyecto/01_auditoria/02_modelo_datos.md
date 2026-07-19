# Modelo de datos y relaciones

## Criterio general

Firestore no impone un esquema, por lo que esta estructura se dedujo de los campos escritos y leídos por los repositorios, las entidades Java y las reglas. Las colecciones son planas y se relacionan mediante IDs. No se encontraron subcolecciones operativas.

## Colecciones principales

### `usuarios/{uid}`

Representa la cuenta de aplicación asociada a Firebase Authentication.

Campos observados: `uid`, `id`, `nombre`, `nombres`, `apellidos`, `email`, `correo`, `telefono`, `dni`, `fechaNacimiento`, `rol`, `estado`, `empresaId`, `inmobiliariaId`, `empresaNombre`, `avatarUrl`, `profileNeedsCompletion`, `invitationId`, `createdAt`, `updatedAt`.

Relaciones: el UID se usa como `clienteId`, `asesorId`, `adminId`, participante de chats y destinatario de notificaciones.

### `empresas/{empresaId}`

Contiene la inmobiliaria administrada por una cuenta admin.

Campos: `id`, `empresaId`, `nombre`, `descripcion`, `direccion`, `correo`, `email`, `telefono`, `adminUid`, `adminEmail`, `companyImageUrl`, `companySecondaryImageUrl`, `estado`, `createdAt`, `updatedAt`.

Relaciones: `usuarios.empresaId`, `proyectos.empresaId` y `asignaciones.empresaId`.

### `admin_invitations/{invitationId}`

Invitación creada por el superadministrador para registrar un administrador.

Campos: `id`, `email`, `empresaId`, `empresaNombre`, `estado`, `acceptedByUid`, `createdAt`, `updatedAt`.

### `proyectos/{projectId}`

Entidad central del catálogo inmobiliario.

Campos principales: `id`, `projectId`, `propertyId`, `nombre`, `title`, `descripcion`, `direccion`, `distrito`, `lat`, `lng`, `precioDesde`, `fechaEntrega`, `fechaEntregaISO`, `estado`, `estadoProyecto`, `estadoComercial`, `badge`, `adminId`, `empresaId`, `inmobiliariaId`, `primaryImageUrl`, `imageUrl`, `qrValue`, `createdAt`, `updatedAt`.

Relaciones: tipologías, amenidades e imágenes usan `projectId`; asignaciones, citas, chats, separaciones y notificaciones también lo referencian.

### `proyectos_tipologias/{projectId}_{tipologiaId}`

Campos: `id`, `projectId`, `nombre`, `estado`, `disponible`, `area`, `habitaciones`, `banos`, `montoTotal`, `montoSeparacion`, `updatedAt`.

### `proyectos_amenidades/{projectId}_{amenityId}`

Campos: `id`, `projectId`, `nombre`, `iconKey`, `available`, `updatedAt`.

### `proyectos_imagenes/{imageId}`

Campos: `id`, `projectId`, `url`, `imageUrl`, `storagePath`, `provider`, `position`, `isPrimary`, `createdAt`, `updatedAt`.

La URL apunta normalmente a Supabase Storage. La imagen binaria no se guarda en Firestore.

### `asignaciones/{projectId}_{asesorId}`

Vincula un asesor con un proyecto.

Campos: `id`, `projectId`, `propertyId`, `proyectoId`, `projectName`, `asesorId`, `asesorNombre`, `adminId`, `empresaId`, `estado`, `createdAt`, `updatedAt`.

El documento determinista permite que las reglas validen la asignación sin ejecutar una consulta.

### `asesor_disponibilidad/{asesorId}` y `proyectos_disponibilidad/{projectId}`

Campos: `slotKeys` o `slots`, `diasLaborales`, `durationMinutos`, `capacidadHorario`, `updatedAt`.

La configuración del proyecto tiene prioridad sobre la del asesor. Si ninguna existe, el repositorio usa lunes a viernes y los slots `09:00`, `10:00`, `11:00`, `12:00`, `15:00`, `16:00` y `17:00`, con duración de 60 minutos y capacidad uno.

### `citas/{citaId}`

Campos: `id`, `clienteId`, `clienteNombre`, `asesorId`, `asesorNombre`, `propertyId`, `projectId`, `proyectoId`, `proyectoNombre`, `inmuebleNombre`, `tipologiaId`, `fechaISO`, `fechaTexto`, `hora`, `slotKey`, `slotId`, `durationMinutos`, `capacidadHorario`, `estado`, `meetingPoint`, `nota`, `participantUids`, `hasCierre`, `separacionId`, `cancelReason`, `cancelledAt`, `rescheduleReason`, `attendanceConfirmed`, `attended`, `createdAt`, `updatedAt`.

Estados observados: `Confirmada`, `Reprogramada`, `Cancelada`, `Atendida`, `No asistio` y valores heredados como `Pendiente`.

### `citas_slots/{asesorId}_{fechaISO}_{slotKey}`

Materializa la ocupación de la agenda del asesor.

Campos: `id`, `citaId`, `citaIds`, `clienteId`, `clientIds`, `asesorId`, `propertyId`, `fechaISO`, `hora`, `slotKey`, `durationMinutos`, `capacidadMaxima`, `reservedCount`, `estado`, `participantUids`, `createdAt`, `updatedAt`.

Puede contener varias citas si la capacidad es mayor a uno.

### `cliente_citas_slots/client_{clienteId}_{fechaISO}_{slotKey}`

Bloqueo determinista que evita que el mismo cliente reserve simultáneamente con otro asesor.

Campos: `citaId`, `clienteId`, `asesorId`, `propertyId`, `fechaISO`, `slotKey`, `createdAt`.

### `eventos_cita/{eventId}`

Historial plano de una cita.

Campos: `id`, `citaId`, `clienteId`, `asesorId`, `titulo`, `detalle`, `tipo`, `fechaISO`, `slotKey`, `fechaHora`, `createdAt`.

Tipos: `AGENDADA`, `REPROGRAMADA`, `CANCELADA` y `ASISTENCIA`.

### `conversaciones/{conversationId}`

Campos: `id`, `clienteUid`, `asesorUid`, `clienteNombre`, `asesorNombre`, `participantUids`, `projectId`, `projectName`, `projectLocation`, `projectPrice`, `projectImageUrl`, `lastMessage`, `lastMessageAt`, `unreadForCliente`, `unreadForAsesor`, `active`, `createdAt`, `updatedAt`.

El ID es determinista por participantes y, en chats de proyecto, también por proyecto.

### `mensajes/{messageId}`

Campos: `id`, `conversationId`, `senderUid`, `receiverUid`, `participantUids`, `text`, `createdAt`.

Los mensajes y el resumen de conversación se actualizan en un mismo batch.

### `separaciones/{separacionId}`

Campos: `id`, `clienteId`, `clienteNombre`, `asesorId`, `asesorNombre`, `adminId`, `citaId`, `propertyId`, `projectId`, `tipologiaId`, `formaPago`, `inmuebleNombre`, `montoTexto`, `amount`, `currency`, `estado`, `createdByRole`, `fechaTexto`, `createdAt`, `updatedAt`.

Estados observados: `Pendiente`, `Pagada`, `Aprobada` y `Rechazada`.

### `notificaciones/{notificationId}`

Campos variables: `id`, `recipientId`, `recipientRole`, `tipo`, `titulo`, `body`, `line1`, `line2`, `separationId`, `projectId`, `propertyId`, `clienteId`, `asesorId`, `read`, `createdAt`.

### Otras colecciones

- `solicitudes_asesor`: solicitudes y decisiones para asesores.
- `resenas`: calificaciones de clientes a asesores o proyectos.
- `tramites`: actividad heredada del cliente.
- `historial_usuario`: historial heredado.
- `logs_sistema`: logs que consume el módulo superadmin.
- `logs`: destino actual de `SystemLogger`, inconsistente con las reglas.
- `app_meta`: versión de esquema, migraciones y recordatorios.
- `Inmobiliarias`: colección heredada para dominios de correo.
- `inmobiliarias`: nombre usado por el almacenamiento local antiguo.

## Relaciones

```text
empresa 1 ─── N administradores/proyectos/asesores
proyecto N ─── N asesor        mediante asignaciones
proyecto 1 ─── N tipologías/amenidades/imágenes
cliente 1 ─── N citas          N ─── 1 asesor
cita 1 ─── 1 slot de asesor
cita 1 ─── 1 bloqueo horario de cliente
cita 1 ─── N eventos
cliente N ─── N asesor         mediante conversaciones
conversación 1 ─── N mensajes
cliente 1 ─── N separaciones   N ─── 1 proyecto/asesor/admin
```

## Índices compuestos declarados

- `asignaciones`: `projectId ASC`, `asesorId ASC`.
- `citas_slots`: `asesorId ASC`, `fechaISO ASC`.
- `citas`: `asesorId ASC`, `fechaISO DESC`, `hora DESC`.
- `mensajes`: `conversationId ASC`, `participantUids ARRAY_CONTAINS`, `createdAt ASC`.

Las consultas del `AdvisorAgendaRepository` usan combinaciones de `whereEqualTo` y `whereIn` con varios nombres heredados. El emulador o producción pueden solicitar índices adicionales, por lo que deben registrarse en este archivo y desplegarse.
