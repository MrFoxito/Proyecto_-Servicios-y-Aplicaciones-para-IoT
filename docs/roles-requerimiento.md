# Requerimiento de roles y estructura de pantallas

## Redaccion mejorada del requerimiento
En este proyecto habra cuatro roles de acceso:
- Usuario
- Asesor
- Administrador
- Super Admin

Al hacer clic en **Iniciar Sesion**, en lugar de entrar directamente a una sola pantalla, debe abrirse un dialogo pequeno con estas opciones:
- Entrar como usuario
- Entrar como asesor
- Entrar como admin
- Entrar como super admin

Comportamiento esperado inicial:
- Si elige **Entrar como super admin**, debe navegar a la pantalla **SuperadminResumenActivity**.
- Si elige **Entrar como usuario**, debe navegar a una pantalla que el cliente compartira despues.

Tambien se requiere reorganizar el proyecto por modulo/rol:
- Crear una carpeta para las pantallas del **Super Admin**.
- Mover ahi todas las pantallas que ya se crearon antes (las del flujo actual de super admin).
- Crear otra carpeta para las pantallas del resto de roles (por ejemplo Usuario; luego Asesor/Admin segun se definan).

## Lo que entiendo tecnicamente
1. El login pasa de flujo fijo a flujo por seleccion de rol mediante dialog.
2. Se implementara en esta etapa la navegacion asegurada para Super Admin.
3. Para Usuario se deja preparado el punto de navegacion, pendiente de la pantalla destino cuando se entregue.
4. Se hara una reorganizacion de paquetes/layouts para separar pantallas por rol y facilitar escalabilidad.

## Propuesta de estructura (Java + layouts)
- `java/com/example/proyecto_iot/superadmin/...`
- `java/com/example/proyecto_iot/usuario/...`
- `java/com/example/proyecto_iot/asesor/...` (placeholder)
- `java/com/example/proyecto_iot/admin/...` (placeholder)

- `res/layout/superadmin/...` (si deseas separar por recursos)
- `res/layout/usuario/...`

Nota: En Android `res/layout` no permite subcarpetas arbitrarias por nombre de modulo de forma directa (solo calificadores como `layout-land`, `layout-sw600dp`). Por eso la separacion principal recomendada es por **paquetes Java/Kotlin** y convencion de nombres en XML (ej: `sa_...`, `user_...`).

## Pregunta puntual para continuar
Cuando dices "crear un folder para cada rol", quieres que la separacion sea:
1. **Por paquetes Java/Kotlin** (recomendado en Android), manteniendo XML en `res/layout` con prefijos por rol, o
2. Intentamos una separacion fisica tambien para XML con otra estrategia de modulos?
