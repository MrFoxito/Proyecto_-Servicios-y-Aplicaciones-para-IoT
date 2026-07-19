# Análisis de costos

## Alcance y supuestos

Los valores se revisaron el 17 de julio de 2026 en páginas oficiales. Los precios reales dependen de la región de Firestore, cantidad de documentos devueltos, reconexiones de listeners, tamaño de imágenes y uso del mapa.

Para comparar escenarios se usa una carga académica moderada:

- 20 lecturas de Firestore por usuario activo al día.
- 2 escrituras por usuario activo al día.
- 30 días al mes.
- 0.5 MB por imagen y un promedio acumulado de una imagen por usuario, más imágenes compartidas de proyectos.
- 20 visualizaciones de imágenes por usuario al mes.
- Una llamada de Edge Function por cada imagen subida.

La región de Firestore no está declarada en el repositorio. El cálculo ilustrativo usa la tarifa oficial mostrada para Iowa `us-central1`: USD 0.03 por 100 000 lecturas y USD 0.09 por 100 000 escrituras después de la cuota gratuita. No es una cotización contractual.

## Firebase

### Servicios que usa el proyecto

- Authentication con correo/contraseña y Google.
- Cloud Firestore para datos y listeners.
- Firebase Storage está configurado como dependencia, pero no se usa en el flujo actual.
- FCM y Cloud Functions no están implementados.

Firestore ofrece diariamente 50 000 lecturas, 20 000 escrituras y 20 000 eliminaciones sin costo, además de 1 GiB de almacenamiento y 10 GiB mensuales de salida. FCM es gratuito, aunque en este proyecto no se usa. Authentication con Identity Platform en Blaze incluye hasta 50 000 usuarios activos mensuales sin costo; el límite de Spark para esa modalidad es 3 000 usuarios activos diarios.

| Usuarios activos | Lecturas/día estimadas | Escrituras/día | Costo Firestore estimado | Comentario |
|---:|---:|---:|---:|---|
| 100 | 2 000 | 200 | USD 0 | Dentro de Spark. |
| 500 | 10 000 | 1 000 | USD 0 | Dentro de Spark. |
| 1 000 | 20 000 | 2 000 | USD 0 | Dentro de Spark con margen limitado para listeners. |
| 5 000 | 100 000 | 10 000 | aprox. USD 0.45/mes | Requiere Blaze por superar lecturas gratuitas diarias; Auth sigue bajo 50 000 MAU. |

La cifra de 5 000 usuarios puede ser mayor porque las reglas usan `get`, `exists` y `getAfter`, operaciones que también generan lecturas facturables. Los listeners de agenda y chat vuelven a leer documentos al conectarse y cada vez que cambian.

## Supabase

El plan Free incluye 1 GB de archivos, 5 GB de egress sin caché, 5 GB de egress con caché y 500 000 invocaciones de Edge Functions. Los proyectos gratuitos pueden pausarse después de una semana de inactividad. El plan Pro comienza en USD 25 mensuales e incluye 100 GB de archivos, 250 GB de egress, 250 GB de egress en caché y dos millones de invocaciones de funciones.

| Usuarios | Almacenamiento ilustrativo | Descarga de imágenes/mes | Plan técnico posible | Costo Supabase |
|---:|---:|---:|---|---:|
| 100 | 0.10 GB | 1 GB | Free | USD 0 |
| 500 | 0.30 GB | 5 GB | Free, justo en el límite de egress | USD 0 |
| 1 000 | 0.55 GB | 10 GB | Pro recomendado | USD 25 |
| 5 000 | 2.55 GB | 50 GB | Pro | USD 25 |

En producción se recomienda Pro incluso con pocos usuarios para evitar pausas y contar con copias diarias y soporte. El bucket usa caché de una hora, por lo que parte del tráfico podría entrar en la cuota de egress cacheado.

## Estimación combinada

| Usuarios | Escenario de desarrollo | Escenario recomendado de producción |
|---:|---:|---:|
| 100 | USD 0/mes | USD 25/mes por Supabase Pro |
| 500 | USD 0/mes | USD 25/mes |
| 1 000 | USD 25/mes | USD 25/mes más posibles excedentes de mapas |
| 5 000 | aprox. USD 25.45/mes | desde USD 25.45/mes más mapas, Places y mayor actividad real |

## Otros costos externos

Google Maps SDK para Android figura con uso ilimitado sin cargo en la lista actual, pero Places y Autocomplete se facturan por evento después de su cuota gratuita. Se deben configurar presupuestos y restricciones de API aunque no formen parte del cálculo Firebase/Supabase solicitado.

## Cuándo cambiar de plan

- Activar Firebase Blaze antes de superar 50 000 lecturas o 20 000 escrituras diarias, usar funciones pagadas, exportaciones administradas o exceder cuotas.
- Cambiar Supabase a Pro antes de producción estable, al superar 1 GB de imágenes o 5 GB de salida, o cuando una pausa por inactividad no sea aceptable.
- Crear alertas de presupuesto y revisar métricas de Firestore, Supabase y Google Maps cada mes.
