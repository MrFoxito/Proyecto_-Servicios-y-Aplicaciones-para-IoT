# 📚 Stack Tecnológico para RecyclerView en Android (IoT - Servicios y Aplicaciones)

**Documento único de referencia** – Basado en la presentación "Clase 05.1 - RecyclerView_25años.pdf"  
No se deben usar otras tecnologías, librerías o patrones fuera de los aquí listados.

---

## 🧱 Componentes obligatorios del RecyclerView

| Componente | Rol |
|------------|-----|
| `Data` | Información a mostrar (lista de objetos Java) |
| `Item Layout` | Layout separado para cada elemento (ej. `item_rv.xml`) |
| `Adapter` | Clase que hereda de `RecyclerView.Adapter<ViewHolder>` |
| `ViewHolder` | Clase interna que hereda de `RecyclerView.ViewHolder` |
| `RecyclerView` | Contenedor principal en el layout de la actividad |

---

## 🌐 Endpoint y servicio web (única fuente de datos)

- **URL real**  
  `https://3dkvh9b90.execute-api.us-east-1.amazonaws.com/prod/`
- **Método** `GET`
- **Header obligatorio**  
  `api-key: EaQiblyUqcoCAyeILnDwUAxR1OX6AH`

> Si el webservice no funciona, se debe usar el repositorio local de respaldo:  
> `https://github.com/2022-2-1TEL05-Servicios-y-Apps-IoT/clase6ws.git`

---

## 📦 Modelado de datos (JSON → Java)

1. Copiar la estructura del JSON de ejemplo (empleados).
2. Generar las dos clases Java necesarias usando **exclusivamente** la herramienta:  
   👉 [https://www.jsonschema2pojo.org/](https://www.jsonschema2pojo.org/)
3. Las clases deben mapear campos como: `id`, `firstName`, `lastName`, `email`, `phoneNumber`, `hireDate`, `jobId`, `salary`, `commissionPct`, `managerId`, `departmentId`, `estado`.

Ejemplo de fragmento JSON:
```json
{
  "id": "205_AC",
  "firstName": "Shelley",
  "lastName": "Higgins",
  "email": "SHIGGINS",
  "phoneNumber": "515.123.8080",
  "hireDate": "1994-06-07T05:00:00Z",
  "jobId": "AC_MGR",
  "salary": 12000.00,
  "commissionPct": null,
  "managerId": "101_EX",
  "departmentId": 110,
  "estado": "ok"
}