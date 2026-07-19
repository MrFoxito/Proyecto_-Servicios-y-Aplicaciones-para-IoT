# Documentación técnica del proyecto

Esta carpeta reúne la documentación elaborada a partir del código existente en la rama auditada. No se asumieron componentes que no estén presentes en el repositorio.

## Organización

```text
documentacion_proyecto/
├── 01_auditoria/
│   ├── 01_informe_tecnico.md
│   └── 02_modelo_datos.md
├── 02_documento/
│   ├── 01_descripcion_proyecto.md
│   ├── 02_arquitectura_sistema.md
│   ├── 03_analisis_costos.md
│   └── 04_criterios_seguridad.md
├── 03_manuales/
│   ├── 01_manual_instalacion.md
│   └── 02_manual_usuario.md
├── 04_referencias/
│   └── fuentes_oficiales.md
├── 05_anexos/
│   └── inventario_tecnico.md
└── Documento_Proyecto_Completo.md
```

El archivo `Documento_Proyecto_Completo.md` integra las secciones principales y está preparado para convertirse a PDF con Pandoc, Typora, Obsidian o un editor compatible con Markdown.

## Acceso rápido

- [Documento completo](Documento_Proyecto_Completo.md)
- [Informe técnico de auditoría](01_auditoria/01_informe_tecnico.md)
- [Modelo de datos](01_auditoria/02_modelo_datos.md)
- [Descripción del proyecto](02_documento/01_descripcion_proyecto.md)
- [Arquitectura del sistema](02_documento/02_arquitectura_sistema.md)
- [Análisis de costos](02_documento/03_analisis_costos.md)
- [Criterios de seguridad](02_documento/04_criterios_seguridad.md)
- [Manual de instalación](03_manuales/01_manual_instalacion.md)
- [Manual de usuario](03_manuales/02_manual_usuario.md)
- [Fuentes oficiales](04_referencias/fuentes_oficiales.md)
- [Inventario técnico](05_anexos/inventario_tecnico.md)

## Alcance verificado

- Aplicación Android nativa escrita en Java.
- Un módulo Gradle llamado `app`.
- Cuatro roles: cliente, asesor, administrador y superadministrador.
- Firebase Authentication y Cloud Firestore en uso.
- Dependencia de Firebase Storage declarada, pero sin operaciones activas encontradas en el código actual.
- Supabase Storage y dos Edge Functions para carga y migración de imágenes.
- Google Maps y Places para funciones geográficas.
- ZXing para generación y lectura de códigos QR.
- Notificaciones locales de Android; no existe implementación de FCM.
- No existe Flutter, Firebase Realtime Database ni Firebase Cloud Functions.

## Fecha de revisión

La revisión del código y de los precios oficiales se realizó el 17 de julio de 2026. Los costos pueden cambiar y deben comprobarse nuevamente antes de una entrega comercial.
