![Barra superior](plan_proyecto_md_assets/page1_image1.png)

![Imagen de portada](plan_proyecto_md_assets/page1_image2.jpeg)

# Plan de proyecto

Sistema para la gestión de reservas y citas de proyectos inmobiliarios vía aplicación móvil

16.03.2026

─

Oscar Díaz  
Profesor del Curso  
1TEL05 - Servicios y Aplicaciones para IoT  
Especialidad de Ingeniería de las Telecomunicaciones  
Pontificia Universidad Católica del Perú

![Logo Ingeniería de las Telecomunicaciones](plan_proyecto_md_assets/page1_image3.png)

---

## Título del proyecto

Sistema para la gestión de reservas y citas de proyectos inmobiliarios vía aplicación móvil

## Coordinador del proyecto

Oscar Díaz

## Clientes

- Jefes de práctica y profesor del curso

## Supervisores

Jefes de práctica del curso:

**Laboratorio 0892**

- 20160679 GUEVARA HERMOZA, ALONSO SEBASTIAN
- 20161056 MINAYA ORIHUELA, CARLOS GUILLERMO
- 20191566 ROSALES ANTUNEZ, ALONSO DABOR

## Objetivo del proyecto

Desarrollar un sistema que permita la gestión de reservas y citas de proyectos inmobiliarios mediante una aplicación móvil.

## Alcance

El alcance del trabajo incluye las fases de planificación, diseño, ejecución e implementación del sistema. Cada cierre de fase requerirá la verificación del jefe de práctica y en caso lo requiera, la validación con el profesor.

---

## Fases principales

| Entregable | Hito | Sesión | Fecha |
|---:|---|---|---|
| 1 | Formación de grupos | Lab 1 | 23/mar/26 |
| 2 | Lista de requerimientos y 100% de Mockups de la aplicación. | Lab 2 | 06/abr/26 |
| 3 | Implementación de mockups en Android(Uso de navigation, menús y elementos UI) | Lab 3 | 20/abr/26 |
| 4 | Uso RecyclerView: Listado de elementos (data estática) | Lab 4 | 04/may/26 |
|  | EXÁMENES PARCIALES |  |  |
| 5 | Implementación de Storage Local y notificaciones | Lab 5 | 25/may/26 |
| 6 | Implementación de Firebase Authentication y de Firebase Database | Lab 6 | 08/jun/26 |
| 7 | Presentación prefinal(90%) | Lab 7 | 22/jun/26 |
|  | Presentación Final (100%) |  |  |

## Tiempo de Implementación estimado

El proyecto tiene un tiempo estimado aprox. 4 meses, empezando el 23 de marzo del 2026 y finalizando el 22 de junio del 2026 (tiempo final estimado).

---

## Composición del equipo de trabajo

**Laboratorio 0892**

Serán 3 grupos de 4 alumnos. Indicar delegados correspondientes.

**<u>Grupo 1: ABCDEF</u>**

|  |  |  |
|---|---|---|
|  |  | — |
|  |  | — |
|  |  | — |
|  |  | — |

**<u>Grupo 2: GHIJKL</u>**

|  |  |  |
|---|---|---|
|  |  | — |
|  |  | — |
|  |  | — |
|  |  | — |

**<u>Grupo 3: MNPQRS</u>**

|  |  |  |
|---|---|---|
|  |  | — |
|  |  |  |
|  |  | — |
|  |  | — |

---

## Estrategia de control de versiones

Se utilizará cualquier plataforma git (github, gitlab, bitbucket, etc) como herramienta para el control de versiones e integración del proyecto grupal.

## Entregables

- El sistema debe estar desplegado en la nube.
- Un repositorio con:
  - Código de cada elemento del sistema
  - Esquema de la base de datos
- Archivo de arquitectura
- Costo total de la solución, OPEX
- Manual de instalación
- Manual de usuario

## Descripción del proyecto

**Gestión de reservas y citas de proyectos inmobiliarios**

El proyecto consiste en el desarrollo de un sistema de gestión de reservas de inmuebles. Lo que se desea es tener un sistema que permita mediante una aplicación móvil:

- El registro de empresas inmobiliarias.
- La gestión de citas presenciales y separaciones (reservas) de inmuebles vía online por la empresa inmobiliaria.
- Cada proyecto ofrecido (por ejemplo, edificios orientados a inversión en Santa Catalina o condominios residenciales en Pueblo Libre) debe mostrar su ubicación exacta y los puntos de interés cercanos en un mapa.
- El cliente debe registrar la separación del inmueble vía la aplicación, al momento de realizar este paso, debe colocar una valoración y observaciones sobre la atención del asesor.
- Los asesores de ventas, así como los administradores de las inmobiliarias, deben ser registrados o habilitados por el administrador del sistema.
- Reporte de separaciones y ventas diario, mensual y anual por empresa inmobiliaria.
- Se tiene un chat por inmobiliaria para la atención al cliente y seguimiento.
- Cada proyecto inmobiliario debe tener un QR que, al escanearse en la aplicación móvil, redirija al proyecto ofrecido.

---

## Actores - Roles

En el sistema, se definen los siguientes actores o roles:

- **Superadmin:** Es el administrador del sistema.
- **Administrador de la Inmobiliaria:** Es el representante de la empresa desarrolladora en el sistema.
- **Asesor de Ventas:** Es el encargado de guiar la visita en el piloto, informar al cliente y cerrar la separación.
- **Cliente:** El usuario que busca propiedades, agenda la visita y hace la reserva/separación en la inmobiliaria.

## Funcionalidades por Actor

- **Superadmin**
  - Registra a los administradores de las inmobiliarias.
  - El superadmin debe ver únicamente la interfaz de gestión de usuarios.
  - El superadmin habilita a los asesores de ventas.
  - Puede activar y desactivar a los usuarios administradores, asesores y clientes.
  - Reporte global de reservas de inmuebles en las distintas empresas.
  - Puede ver los logs de los eventos sucedidos en el sistema.

- **Administrador de la inmobiliaria**
  - Registra la ubicación de sus oficinas, correo y teléfono de contacto.
  - Registra fotos promocionales de la empresa (mínimo 2).
  - Registra los proyectos inmobiliarios, indicando:
    - La ubicación del proyecto, como un edificio estilo Catalina Sky, que se debe poder ver en un mapa.
    - Tipología de los departamentos (metraje, número de cuartos).
    - El costo de separación y precio total del inmueble.
    - Las áreas comunes que ofrece (coworking, piscina, parrillas).
    - Si el proyecto se encuentra en:
      - En planos: El proyecto no se ha empezado a construir.
      - En preventa: La obra ya inició o está avanzada, pero no ha finalizado.
      - En venta: El proyecto está terminado, con recepción de obra y listo para habitarse.
    - Fechas estimadas de entrega del proyecto.
  - Datos del servicio (proyecto):
    - Nombre,
    - Descripción,
    - Precio,
    - Imágenes (minimo 2)
  - Asigna a los asesores de ventas a proyectos específicos.
  - Recibe una notificación de las “separaciones” registradas por el asesor de ventas.
  - Aprueba el monto de las “separaciones” registradas por el asesor de ventas.
  - Recibe una notificación de pago de check out (separación) de un cliente; a partir de este punto se procesa el cobro a la tarjeta previamente registrada.
  - Reporte de ventas/separaciones realizadas por proyecto y por asesor.

- **Asesor de ventas**
  - Se auto registra al sistema y es aprobado (habilitado) por el administrador del Sistema.
  - Datos de un usuario:
    - Nombres
    - Apellidos
    - Tipo de Documento de identidad(DNI, Pasaporte, Carnet de extranjería)
    - Número de Documento de identidad
    - Fecha de nacimiento
    - Correo electrónico
    - Teléfono
    - Domicilio
    - Foto
  - Visualiza las citas agendadas por los clientes para visitar el proyecto que tiene asignado.
  - Registrar las "separaciones" especificando el proyecto, el cliente y el valor propuesto.
  - Ve el historial de sus citas

- **Cliente**
  - Se auto registra al sistema y es habilitado automáticamente.
  - Datos de un usuario:
    - Nombres
    - Apellidos
    - Tipo de Documento de identidad(DNI, Pasaporte, Carnet de extranjería)
    - Número de Documento de identidad
    - Fecha de nacimiento
    - Correo electrónico
    - Teléfono
    - Domicilio
    - Foto
  - Visualiza las inmobiliarias, sus proyectos ofrecidos en el mapa y las valoraciones de otros usuarios.
  - Registra sus citas para visitar pilotos, una por vez, se debe validar que no existan superposiciones en los horarios de las citas programadas.
  - Recibe notificación de “separacion” aprobada, debe proceder a pagar en un periodo no mayor a 10 minutos.
  - Para poder habilitar la opción de separar un inmueble, se debe ingresar una tarjeta de crédito o débito.
  - Ve el historial de sus citas y separaciones.

## Requerimientos no funcionales

- La aplicación debe ser implementada en Android nativo.
- Debe ser implementada utilizando el lenguaje de programación Java.
- La compatibilidad mínima debe ser Android 14.0
- La base de datos debe ser NoSQL
