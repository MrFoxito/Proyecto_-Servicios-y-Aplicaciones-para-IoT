# Manual de usuario

## Acceso general

Al abrir la aplicación se muestra la pantalla de inicio de sesión. Se puede ingresar con correo y contraseña o con Google si el proyecto tiene configurado ese proveedor. El enlace de recuperación envía un correo al email escrito. La opción de registro crea una cuenta con nombres, apellidos, teléfono, correo y una contraseña de al menos seis caracteres.

Después del acceso, la aplicación abre automáticamente el módulo correspondiente al rol guardado en Firestore.

## Cliente

### Explorar proyectos

La pantalla principal muestra proyectos inmobiliarios obtenidos de Firestore. El cliente puede abrir el listado completo, usar el mapa o tocar una tarjeta para entrar al detalle. El detalle presenta nombre, precio, ubicación, estado comercial, descripción, galería, tipologías y amenidades disponibles.

### Ver mapa y ubicación

Desde el detalle se puede abrir el mapa. La aplicación usa las coordenadas guardadas en el proyecto. Desde el listado también se puede cambiar a la exploración geográfica.

### Contactar a un asesor

En el detalle de un proyecto se puede abrir el chat. La aplicación busca asesores activos asignados a ese proyecto. Si hay más de uno, puede solicitar una selección. La conversación se crea o reutiliza y los mensajes aparecen en tiempo real.

En la sección Chats se pueden ver conversaciones, buscar asesores y filtrar mensajes no leídos o favoritos. El filtro favorito es visual y depende de la información del elemento mostrado.

### Agendar una cita

1. Abrir un proyecto.
2. Presionar la opción para agendar cita.
3. Elegir el asesor asignado cuando sea necesario.
4. Seleccionar una fecha futura.
5. Elegir uno de los horarios disponibles.
6. Revisar contacto y nota.
7. Confirmar.

Al confirmar, la aplicación valida asignación, día laboral, horario y capacidad. La reserva aparece en “Mi actividad” y en la agenda del asesor. Si otro usuario ocupa el último cupo antes de confirmar, se muestra un error y se debe elegir otro horario.

### Escanear QR

Desde Explorar o el listado se puede abrir el escáner. Se debe conceder permiso de cámara y apuntar al QR del proyecto. Los códigos válidos usan `app://proyecto/{id}` o referencias compatibles reconocidas por el parser. Al encontrar el proyecto se abre su detalle.

### Separar un inmueble

La separación está disponible cuando el proyecto está en preventa o venta. El cliente revisa proyecto y monto, selecciona un asesor si corresponde y confirma el proceso de pago mostrado. Se crea una separación en Firestore y una notificación para el administrador vinculado al proyecto.

La pantalla representa el flujo de la aplicación, pero no se encontró integración con una pasarela bancaria real. No se debe interpretar el botón como un cobro financiero en producción.

### Mi actividad

Esta sección agrupa citas, separaciones y eventos recientes. Al tocar un elemento se abre su detalle. Parte del historial de trámites todavía utiliza almacenamiento local, por lo que puede no sincronizarse entre dispositivos.

### Perfil

El cliente puede consultar y editar datos personales, avatar y cerrar sesión. Las pantallas de preferencias y seguridad contienen acciones limitadas o demostrativas; no se encontró un panel real de sesiones activas.

## Asesor

### Agenda

La agenda es la pantalla principal. Muestra un calendario y citas de proyectos que continúan asignados al asesor. Los cambios llegan mediante listeners de Firestore. Se pueden filtrar fechas y abrir el detalle de cada cita.

### Gestionar una cita

Desde el detalle se puede:

- añadir o editar una nota;
- conversar con el cliente;
- marcar la visita como atendida o no asistida;
- reprogramar usando horarios disponibles;
- cancelar la cita y liberar su horario;
- registrar una separación relacionada.

Una reprogramación libera el horario anterior y ocupa el nuevo dentro de una transacción. La cancelación también libera el slot.

### Separaciones

El asesor consulta separaciones asociadas a su UID, abre solicitudes, registra separaciones desde una cita y actualiza estados permitidos por la interfaz. Cuando registra una separación se puede generar una notificación para el administrador del proyecto.

### Chats y perfil

Chats muestra conversaciones del asesor en tiempo real. El asesor puede abrir una conversación y responder. En Perfil consulta su empresa, proyectos asignados, cierres, avatar y datos personales.

## Administrador

### Resumen

El inicio administrativo reúne indicadores, notificaciones y accesos rápidos. Los reportes se calculan consultando proyectos, asignaciones, citas y separaciones de la cuenta actual.

### Proyectos

El administrador puede listar, crear, editar y revisar proyectos de su empresa. El formulario maneja datos generales, ubicación, fecha de entrega, estado comercial, tipologías, amenidades y galería. Las imágenes se suben a Supabase y sus URLs se guardan en Firestore.

El administrador solo debe modificar proyectos cuyo `adminId` y `empresaId` coincidan con su perfil.

### Asesores y asignaciones

Puede consultar asesores de su empresa, abrir detalles, revisar solicitudes y asignar o desasignar proyectos. La asignación activa determina qué proyectos, citas y chats aparecen al asesor.

### Notificaciones y separaciones

Las notificaciones informan pagos simulados, separaciones registradas por asesores, cambios de proyecto y solicitudes. Desde el detalle se puede aprobar o rechazar una separación y confirmar el estado de un pago representado por la aplicación.

### Empresa, perfil y reportes

El administrador edita sus datos y datos básicos de la empresa, revisa reportes y consulta reseñas. Si ingresa por invitación con perfil incompleto, la aplicación obliga a completar la información antes de abrir el inicio.

## Superadministrador

### Resumen y reportes globales

El módulo presenta indicadores generales, reportes de usuarios y reportes globales. También dispone de filtros por rango para los datos mostrados.

### Gestión de usuarios

Permite consultar usuarios, cambiar estados y revisar accesos desde las pantallas disponibles. Las acciones sensibles se registran mediante el sistema de logs, aunque actualmente existe una inconsistencia entre las colecciones `logs` y `logs_sistema`.

### Registrar administrador

El superadministrador crea una empresa y una invitación para un correo. El destinatario abre el enlace de invitación, crea o vincula su cuenta Firebase y completa su perfil. La invitación pasa de pendiente a aceptada.

### Solicitudes y mantenimiento

Puede aprobar solicitudes de asesor, revisar logs y ejecutar la migración de metadatos e imágenes cuando la pantalla correspondiente lo invoque. La función de migración de imágenes en Supabase exige que el perfil tenga rol superadmin.

## Cierre de sesión

La opción de salir cierra Firebase Authentication, limpia la sesión local y devuelve al login. En equipos compartidos se debe cerrar sesión al terminar.
