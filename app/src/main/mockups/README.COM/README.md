# Cambios recientes: esquema local y RecyclerViews

Este documento resume los ultimos cambios implementados para probar el storage local de la aplicacion.

## Objetivo

Se agrego una capa local de datos para que los RecyclerViews de los distintos roles ya no dependan de listas hardcodeadas dentro de cada pantalla. La implementacion permite probar el comportamiento de una base de datos local tipo NoSQL antes de migrar a una solucion remota como Firestore.

## Archivo principal

La fuente local centralizada esta en:

`app/src/main/java/com/example/proyecto_iot/data/LocalSchemaStorage.java`

Esta clase usa `SharedPreferences` y guarda colecciones en formato JSON. Tambien expone metodos listos para alimentar los adapters actuales de cada rol.

## Colecciones locales creadas

- `usuarios`
- `proyectos`
- `proyectos_tipologias`
- `proyectos_amenidades`
- `proyectos_imagenes`
- `solicitudes_asesor`
- `citas`
- `eventos_cita`
- `separaciones`
- `conversaciones`
- `mensajes`
- `notificaciones`
- `resenas`
- `logs_sistema`
- `tramites`
- `historial_usuario`

## Pantallas conectadas

### Superadmin

- Gestion de usuarios
- Aprobacion de asesores
- Logs del sistema
- Resumen y control de acceso

### Admin

- Listado de proyectos
- Listado de asesores
- Asignacion de proyectos a asesores
- Detalle de asesor
- Detalle de proyecto
- Solicitudes de asesores
- Resenas de asesor
- Notificaciones
- Crear proyecto: material visual, tipologias y amenidades
- Editar proyecto: material visual, tipologias y amenidades

### Asesor

- Agenda
- Historial de citas
- Detalle de cita
- Chats
- Mensajes de chat
- Separaciones

### Usuario

- Actividad
- Tramites
- Historial
- Chats
- Notificaciones
- Listado de propiedades

## Que deberia verse al probar

Visualmente la app debe verse muy parecida a los mockups anteriores, porque se mantuvieron datos semilla coherentes con las pantallas existentes. La diferencia importante es interna: las listas ahora se alimentan desde `LocalSchemaStorage`.

Al probar en emulador o APK, revisar:

- Admin: filtros de proyectos, asesores, solicitudes y notificaciones.
- Admin: detalle de proyecto con galeria, tipologias y amenidades.
- Admin: crear/editar proyecto con datos semilla desde storage local.
- Asesor: agenda, historial, chats y separaciones.
- Usuario: actividad, chats, notificaciones y propiedades.
- Superadmin: usuarios, solicitudes, resumen y logs.

## Nota sobre datos anteriores

El seed local usa la clave interna `initialized_v3`. Esto fuerza la carga del nuevo esquema local aunque existiera una version previa inicializada.

Si se observan datos antiguos en el emulador, desinstalar la app y volver a instalar el APK para limpiar `SharedPreferences`.

## Verificacion realizada

Se compilo el proyecto con:

```bash
./gradlew.bat assembleDebug --no-daemon
```

Resultado:

```text
BUILD SUCCESSFUL
```

El APK debug se genera en:

`app/build/outputs/apk/debug/app-debug.apk`
