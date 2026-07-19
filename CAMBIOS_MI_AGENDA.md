# Corrección de Mi agenda del asesor

Fecha: 18 de julio de 2026

## Cambios realizados

- La agenda y el historial del asesor consumen una fuente única en tiempo real.
- Solo se muestran las citas de proyectos con una asignación activa para ese asesor.
- Las altas, cancelaciones, reprogramaciones y desasignaciones actualizan la agenda automáticamente.
- Se admiten referencias heredadas de asesor y proyecto (`advisorId`, `projectId`, `proyectoId` y `propertyId`).
- Las fechas y horas heredadas se normalizan; los datos sin fecha u hora válida no se colocan en el calendario y quedan registrados para revisión.
- Las nuevas citas guardan el ID canónico del proyecto en `propertyId`, `projectId` y `proyectoId`.
- Las reglas de Firestore restringen la lectura y gestión de citas a proyectos actualmente asignados al asesor.

## Archivos principales

- `app/src/main/java/com/example/proyecto_iot/data/AdvisorAgendaRepository.java`
- `app/src/main/java/com/example/proyecto_iot/data/FirebaseAppointmentRepository.java`
- `app/src/main/java/com/example/proyecto_iot/asesor/AsesorMiAgendaActivity.java`
- `app/src/main/java/com/example/proyecto_iot/asesor/AsesorHistorialCitasActivity.java`
- `firestore.rules`
- `firestore.indexes.json`

## Verificación

- `testDebugUnitTest`: correcto.
- `assembleDebug`: correcto.
- `npm run test:rules`: 19 pruebas correctas con Firestore Emulator.

## APK generado

El APK debug generado se encuentra en:

`app/build/outputs/apk/debug/app-debug.apk`

> Este archivo Markdown conserva el registro de los cambios. Para evitar perder el APK, también debe guardarse una copia fuera de la carpeta `build`, ya que Gradle puede reemplazarla en compilaciones posteriores.
