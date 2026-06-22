# Configuración de Google Maps y Places

La aplicación ya integra Maps SDK for Android y Places API (New). La clave no se guarda en Git.

1. En Google Cloud, habilitar para el proyecto:
   - Maps SDK for Android
   - Places API (New)
2. Asociar facturación y restringir la clave al paquete `com.example.proyecto_iot` y a su SHA-1.
3. Agregar al archivo local ignorado `local.properties`:

```properties
MAPS_API_KEY=TU_CLAVE_REAL
```

Sin esta propiedad la aplicación compila, pero muestra un aviso y no activa el mapa ni las búsquedas de Places.
