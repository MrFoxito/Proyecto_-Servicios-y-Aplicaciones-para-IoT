# Flujos funcionales por rol y feedback del proyecto

## Contexto general

La aplicacion propuesta es un sistema movil Android nativo para gestionar inmobiliarias, proyectos inmobiliarios, citas de visita, separaciones de inmuebles, pagos, reportes, chat, notificaciones y visualizacion de proyectos en mapa.

El sistema se organiza alrededor de cuatro roles principales:

- Superadmin
- Administrador de inmobiliaria
- Asesor de ventas
- Cliente

Estos roles no funcionan de manera aislada. Cada accion importante de un rol genera informacion, validaciones o notificaciones para otro rol. Por eso, el flujo central del sistema puede entenderse asi:

1. El superadmin habilita y controla usuarios del sistema.
2. El administrador de inmobiliaria registra la empresa, proyectos, asesores y condiciones comerciales.
3. El cliente explora proyectos, agenda citas y solicita separaciones.
4. El asesor atiende citas, registra separaciones y da seguimiento comercial.
5. El administrador aprueba separaciones, valida pagos y revisa reportes.
6. El superadmin audita usuarios, eventos y reportes globales.

## Flujo del Superadmin

### Objetivo del rol

Administrar el sistema completo a nivel global, controlar usuarios y supervisar eventos importantes.

### Flujo funcional principal

1. Inicia sesion con credenciales de superadmin. x
2. Accede a una interfaz enfocada principalmente en gestion de usuarios. x
3. Crea y registra administradores de inmobiliarias. x
4. Revisa solicitudes o registros de asesores de ventas. x
5. Habilita o deshabilita asesores de ventas. x
6. Activa o desactiva usuarios administradores, asesores y clientes. (pendiente)
7. Consulta reportes globales de reservas o separaciones. x
8. Revisa logs de eventos del sistema. x

### Interconexion con otros roles

- Con administrador de inmobiliaria: el superadmin crea o habilita al administrador que luego gestionara su empresa inmobiliaria. 
- Con asesor de ventas: el superadmin puede habilitar asesores registrados, permitiendo que luego sean asignados a proyectos.
- Con cliente: puede activar o desactivar clientes si se requiere control administrativo. 
- Con todo el sistema: los logs permiten auditar acciones hechas por administradores, asesores y clientes.   

### Datos clave que gestiona

- Usuarios administradores
- Usuarios asesores
- Usuarios clientes
- Estados de cuenta: activo, inactivo, pendiente o bloqueado
- Logs de eventos
- Reportes globales

## Flujo del Administrador de inmobiliaria

### Objetivo del rol

Gestionar la informacion comercial de su inmobiliaria, sus proyectos, asesores asignados, separaciones, pagos y reportes.

### Flujo funcional principal

1. Inicia sesion como administrador de inmobiliaria.
2. Completa o actualiza los datos de la empresa:
   - Ubicacion de oficinas
   - Correo de contacto
   - Telefono
   - Fotos promocionales
3. Registra proyectos inmobiliarios:
   - Nombre
   - Descripcion
   - Ubicacion exacta en mapa
   - Imagenes
   - Tipologias de departamentos
   - Metraje
   - Numero de habitaciones
   - Precio total
   - Costo de separacion
   - Areas comunes
   - Estado del proyecto: en planos, preventa o venta
   - Fecha estimada de entrega
4. Asigna asesores de ventas a proyectos especificos.
5. Recibe notificaciones cuando un asesor registra una separacion.
6. Revisa y aprueba el monto de la separacion.
7. Recibe notificacion del pago de checkout del cliente.
8. Supervisa ventas y separaciones por proyecto y por asesor.

### Interconexion con otros roles

- Con superadmin: depende de que su cuenta sea creada o habilitada por el superadmin.
- Con asesor de ventas: asigna asesores a proyectos y revisa separaciones registradas por ellos.
- Con cliente: los proyectos publicados por el administrador son visibles para el cliente; tambien recibe informacion indirecta cuando el cliente agenda citas o paga separaciones.
- Con reportes: alimenta reportes por proyecto, asesor, ventas y separaciones.

### Datos clave que gestiona

- Datos de la inmobiliaria
- Proyectos inmobiliarios
- Imagenes promocionales
- Ubicaciones en mapa
- Tipologias y precios
- Asignacion de asesores
- Separaciones pendientes o aprobadas
- Reportes de ventas

## Flujo del Asesor de ventas

### Objetivo del rol

Atender al cliente, guiar visitas, hacer seguimiento comercial y registrar separaciones de inmuebles.

### Flujo funcional principal

1. Se auto registra en la aplicacion.
2. Ingresa sus datos personales:
   - Nombres
   - Apellidos
   - Tipo de documento
   - Numero de documento
   - Fecha de nacimiento
   - Correo electronico
   - Telefono
   - Domicilio
   - Foto
3. Espera aprobacion o habilitacion.
4. Una vez habilitado, visualiza los proyectos a los que fue asignado.
5. Consulta citas agendadas por clientes para visitar proyectos asignados.
6. Atiende la visita o seguimiento del cliente.
7. Registra una separacion indicando:
   - Proyecto
   - Cliente
   - Valor propuesto
8. Consulta el historial de sus citas.

### Interconexion con otros roles

- Con superadmin: su usuario debe ser habilitado para poder operar.
- Con administrador de inmobiliaria: puede ser asignado a uno o mas proyectos y sus separaciones deben ser aprobadas por el administrador.
- Con cliente: atiende citas, registra separaciones y participa en la experiencia de venta.
- Con reportes: sus acciones alimentan reportes por asesor, proyecto y periodo.

### Datos clave que gestiona

- Perfil del asesor
- Citas asignadas
- Clientes atendidos
- Separaciones registradas
- Historial de citas

## Flujo del Cliente

### Objetivo del rol

Explorar proyectos inmobiliarios, agendar visitas, separar inmuebles, pagar separaciones y consultar su historial.

### Flujo funcional principal

1. Se auto registra en la aplicacion. x 
2. Su cuenta se habilita automaticamente. x
3. Ingresa sus datos personales: x
   - Nombres
   - Apellidos
   - Tipo de documento
   - Numero de documento
   - Fecha de nacimiento
   - Correo electronico
   - Telefono
   - Domicilio
   - Foto
4. Visualiza inmobiliarias disponibles. x 
5. Explora proyectos ofrecidos en el mapa. (pendiente)
6. Revisa ubicacion, informacion del proyecto, imagenes y valoraciones de otros usuarios. x 
7. Agenda una cita para visitar un piloto. x 
8. El sistema valida que no existan superposiciones con otras citas del cliente. (pendiente)
9. Asiste o coordina la visita con el asesor asignado. (pendiente)
10. Solicita o confirma una separacion de inmueble. x 
11. Para habilitar la separacion, registra una tarjeta de credito o debito. (pendiente preguntar JP)
12. Recibe notificacion de separacion aprobada. (pendiente - asesor)
13. Realiza el pago en un periodo no mayor a 10 minutos. (pendiente - JP)
14. Registra valoracion y observaciones sobre la atencion del asesor. (pendiente)
15. Consulta historial de citas y separaciones. x

### Interconexion con otros roles

- Con administrador de inmobiliaria: consume la informacion de proyectos registrada por la inmobiliaria y genera citas, reservas y pagos.
- Con asesor de ventas: agenda visitas, recibe atencion y evalua al asesor.
- Con superadmin: puede ser activado o desactivado desde la administracion global.
- Con sistema de pagos y notificaciones: el cliente debe recibir alertas y completar el pago dentro del tiempo definido.

### Datos clave que gestiona

- Perfil del cliente
- Citas programadas
- Proyectos favoritos o consultados
- Tarjeta registrada
- Separaciones
- Pagos
- Valoraciones y observaciones

## Flujo transversal de una cita

1. El administrador registra un proyecto y asigna asesores.
2. El cliente visualiza el proyecto en el mapa.
3. El cliente agenda una cita.
4. El sistema valida disponibilidad y evita cruces de horario.
5. El asesor asignado visualiza la cita.
6. El asesor atiende al cliente.
7. La cita queda registrada en el historial del cliente y del asesor.

### Roles involucrados

- Administrador de inmobiliaria
- Cliente
- Asesor de ventas

### Puntos de control recomendados

- Validar disponibilidad del asesor.
- Validar horario de atencion del proyecto o sala de ventas.
- Evitar que un cliente tenga dos citas al mismo tiempo.
- Confirmar, cancelar o reprogramar citas.
- Notificar al cliente y al asesor ante cambios.

## Flujo transversal de separacion de inmueble

1. El cliente visita o selecciona un proyecto.
2. El asesor registra una separacion con el proyecto, cliente y valor propuesto.
3. El administrador recibe una notificacion de separacion.
4. El administrador aprueba o rechaza el monto.
5. Si se aprueba, el cliente recibe una notificacion.
6. El cliente debe pagar en un maximo de 10 minutos.
7. El sistema registra el resultado del pago.
8. El cliente coloca valoracion y observaciones sobre la atencion del asesor.
9. La separacion se incorpora a los reportes del administrador y del superadmin.

### Roles involucrados

- Cliente
- Asesor de ventas
- Administrador de inmobiliaria
- Superadmin, como auditor o visor global

### Puntos de control recomendados

- Definir estado de la separacion: pendiente, aprobada, rechazada, expirada, pagada o cancelada.
- Bloquear temporalmente el inmueble durante los 10 minutos de pago.
- Liberar el inmueble si el pago expira.
- Registrar evidencia del pago o transaccion.
- Notificar a todos los actores involucrados.

## Flujo transversal de proyecto inmobiliario con QR

1. El administrador registra el proyecto.
2. El sistema genera o asocia un QR unico al proyecto.
3. El QR se muestra o descarga para uso comercial.
4. El cliente escanea el QR desde la aplicacion movil.
5. La aplicacion redirige al detalle del proyecto.
6. El cliente puede ver informacion, mapa, fotos, agendar cita o iniciar el flujo de separacion.

### Roles involucrados

- Administrador de inmobiliaria
- Cliente

### Puntos de control recomendados

- Cada QR debe apuntar a un identificador unico de proyecto.
- Si el proyecto esta inactivo, el QR debe mostrar un mensaje claro.
- Registrar metricas de escaneo puede ayudar a reportes comerciales.

## Flujo transversal de chat

1. El cliente abre el chat de una inmobiliaria.
2. El sistema identifica la inmobiliaria asociada.
3. El cliente envia consultas.
4. La inmobiliaria o asesor responde.
5. El historial queda asociado al cliente y a la inmobiliaria.

### Roles involucrados

- Cliente
- Administrador de inmobiliaria
- Asesor de ventas

### Puntos de control recomendados

- Definir si responde el administrador, el asesor asignado o ambos.
- Definir si el chat es por inmobiliaria, por proyecto o por cita.
- Mostrar estado de lectura o respuesta.
- Permitir adjuntar informacion basica del proyecto consultado.

## Feedback: flujos que faltan o requieren mayor definicion

### 1. Flujo de login y recuperacion de cuenta

El documento menciona Firebase Authentication, pero no detalla:

- Inicio de sesion por rol.    x
- Recuperacion de contrasena. (pendiente)
- Verificacion de correo. (pendiente)  
- Manejo de usuario bloqueado o deshabilitado. x
- Redireccion automatica segun rol. x

Este flujo es necesario porque toda la aplicacion depende de permisos distintos para superadmin, administrador, asesor y cliente.

### 2. Flujo de aprobacion de asesores

Se indica que el asesor se auto registra y debe ser aprobado, pero hay una posible ambiguedad:

- En una parte se menciona que el superadmin habilita asesores.
- En otra parte el asesor es aprobado por el administrador del sistema.
- Tambien el administrador de inmobiliaria asigna asesores a proyectos.

Se recomienda definir claramente si el asesor:

- Es aprobado por el superadmin.
- Es aprobado por el administrador de inmobiliaria.
- Es aprobado por superadmin y luego asignado por administrador.

Aclaraciòn de la ambiguedad

El superadmin aprueba al usuario cuando se quiere unir como "asesor" , pero el administrador de la inmobiliaria aprueba la solicitud del asesor cuando este va unirse a la proyecto.

### 3. Flujo de disponibilidad de citas

El documento indica que el cliente agenda citas y que no debe haber superposiciones, pero falta definir:

- Horarios disponibles por proyecto.
- Duracion de cada cita.
- Capacidad por horario.
- Disponibilidad del asesor.
- Reprogramacion.
- Cancelacion.
- Confirmacion de asistencia.

Este flujo es importante para evitar reservas imposibles o conflictos de agenda.

*coordinar cliente y asesor para evitar confusiones

### 4. Flujo de inventario de inmuebles

El documento habla de proyectos, tipologias, precios y separaciones, pero no detalla si se gestionan unidades inmobiliarias especificas.

Falta definir si el cliente separa:

- Un proyecto completo.
- Una tipologia.
- Un departamento/unidad concreta.

Para un sistema inmobiliario real, seria recomendable manejar unidades con estados como disponible, reservado, separado, vendido o no disponible.

### 5. Flujo de pagos

Se menciona que el cliente debe registrar tarjeta y pagar en 10 minutos, pero falta definir:

- Pasarela de pago.
- Validacion de tarjeta.
- Estado de transaccion.
- Pago fallido.
- Reintento de pago.
- Comprobante.
- Reembolso o anulacion.
- Expiracion automatica de separacion.

Aunque para el curso puede simularse, el flujo debe estar representado para que la logica sea consistente.

*Pendiente preguntar al JP

### 6. Flujo de valoraciones

El documento indica que el cliente debe colocar valoracion y observaciones sobre la atencion del asesor al separar un inmueble.

Falta definir:

- Si la valoracion es obligatoria.
- En que momento exacto aparece.
- Si se puede editar.
- Si el asesor o administrador puede responder. (no responde)
- Si afecta reportes del asesor.

### 7. Flujo de notificaciones

Se mencionan notificaciones para separaciones y pagos, pero conviene definir eventos concretos:

- Cita creada.
- Cita cancelada.
- Cita reprogramada.
- Separacion registrada.
- Separacion aprobada.
- Separacion rechazada.
- Pago pendiente.
- Pago exitoso.
- Pago expirado.
- Nuevo mensaje de chat.

### 8. Flujo de logs

El superadmin puede ver logs, pero falta definir que eventos se registran:

- Creacion, edicion o desactivacion de usuarios. (x)
- Registro o modificacion de proyectos. (x)
- Asignacion de asesores. (x)
- Creacion o cancelacion de citas.
- Aprobacion o rechazo de separaciones.
- Intentos de pago.
- Cambios de estado relevantes.

### 9. Flujo de reportes

El documento pide reportes diarios, mensuales y anuales, pero falta precisar:

- Filtros por inmobiliaria, proyecto, asesor y rango de fechas.
- Diferencia entre reserva, separacion y venta.
- Exportacion de reportes.
- Graficos o tablas.
- Indicadores principales: total vendido, total separado, cantidad de citas, conversion por asesor.

### 10. Flujo de administracion de proyectos inactivos

No se detalla que pasa cuando un proyecto:

- Se elimina.
- Se desactiva.
- Cambia de estado.
- Ya no tiene unidades disponibles.
- Tiene citas futuras asociadas.

Se recomienda no eliminar proyectos directamente, sino manejarlos con estados.

*Se encarga el administrador

## Recomendacion general de estados

Para que la aplicacion sea mas ordenada, se recomienda manejar estados claros en las entidades principales.

### Usuario

- Pendiente
- Activo
- Inactivo
- Bloqueado

### Proyecto

- Borrador
- Publicado
- Inactivo
- Finalizado

### Cita

- Pendiente
- Confirmada
- Reprogramada
- Cancelada
- Atendida
- No asistio

### Separacion

- Pendiente de aprobacion
- Aprobada
- Rechazada
- Pendiente de pago
- Pagada
- Expirada
- Cancelada

### Pago

- Pendiente
- Exitoso
- Fallido
- Expirado
- Reembolsado

## Conclusiones

El documento base define bien los roles principales y las funcionalidades esperadas, pero todavia necesita mayor detalle en los flujos transversales: autenticacion, aprobacion de usuarios, disponibilidad de citas, inventario de unidades, pagos, notificaciones, logs y reportes.

La conexion mas importante del sistema es la siguiente:

Cliente agenda cita -> Asesor atiende y registra separacion -> Administrador aprueba -> Cliente paga -> Sistema genera reportes y logs.

Si esta cadena queda bien implementada, la aplicacion cubrira el nucleo funcional del proyecto inmobiliario.
