# Migraciones administrativas

`migrate-project-conversations.mjs` normaliza los chats antiguos al esquema de
proyecto. Requiere credenciales de una cuenta de servicio con acceso de
administrador a Firestore; nunca debe ejecutarse desde la aplicación Android.

Primero se revisa sin cambios:

```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS = "C:\ruta\cuenta-servicio.json"
npm run migrate:project-conversations
```

Después de validar el resultado, se aplica explícitamente:

```powershell
npm run migrate:project-conversations -- --apply
```

La migración conserva el historial más antiguo por cliente y proyecto,
reubica sus mensajes en el identificador canónico y marca los documentos de
origen. Los chats sin proyecto verificable se conservan como legado (`unlinked`).
