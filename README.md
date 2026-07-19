# ProyectoIot-ForkLeo

## Configuración de mapas

Para que Administrador pueda seleccionar, editar y previsualizar ubicaciones con Mapbox,
copia `local.properties.example` como `local.properties` y define tokens de Mapbox:

```properties
MAPBOX_ACCESS_TOKEN=pk.tu_token_publico
MAPBOX_DOWNLOADS_TOKEN=sk.tu_token_secreto_de_descargas
```

El token público se incorpora al APK y el secreto solo se usa para descargar la dependencia
durante la compilación; ninguno debe subirse a Git. El explorador de proyectos de Cliente y
la búsqueda opcional de direcciones siguen usando `MAPS_API_KEY` de Google Maps/Places.
