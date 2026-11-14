# API de reserva de salas de reuniones

API REST hecha con **Java 17** y **Spring Boot 3** para crear salas y reservarlas,
evitando que dos reservas ocupen la misma sala a la vez.

## Cómo ejecutarlo

Requisitos: **Java 17 o superior** y **Maven 3.9+**. No hace falta instalar base de datos:
se usa H2 en memoria y Flyway crea las tablas al arrancar.

```bash
# Arrancar la aplicación (queda en http://localhost:8080)
mvn spring-boot:run

# Ejecutar los tests
mvn test
```

Usuario para los endpoints protegidos: **admin / admin123**
(se puede cambiar con las variables `SPRING_SECURITY_USER_NAME` y `SPRING_SECURITY_USER_PASSWORD`).

> Los datos están en memoria, así que se pierden al parar la aplicación.

## Endpoints

| Método | Ruta | Autenticación | Qué hace |
|--------|------|---------------|----------|
| POST   | `/api/salas` | Sí | Crea una sala |
| GET    | `/api/salas` | No | Lista las salas |
| GET    | `/api/salas/{id}` | No | Devuelve una sala |
| POST   | `/api/reservas` | Sí | Crea una reserva (409 si la sala está ocupada) |
| GET    | `/api/reservas` | No | Lista reservas con filtros y paginación |
| DELETE | `/api/reservas/{id}` | Sí | Cancela una reserva |

Parámetros del listado de reservas (todos opcionales):

- `salaId`: solo las reservas de esa sala
- `fecha` (formato `2030-01-15`): solo las reservas que ocupan algún momento de ese día
- `pagina` (empieza en 0, por defecto 0) y `tamano` (entre 1 y 100, por defecto 10)

### Ejemplos con curl

```bash
# Crear una sala
curl -u admin:admin123 -X POST localhost:8080/api/salas \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Sala Azul", "capacidad": 8}'

# Reservarla
curl -u admin:admin123 -X POST localhost:8080/api/reservas \
  -H "Content-Type: application/json" \
  -d '{"salaId": 1, "responsable": "Ana García", "inicio": "2030-01-15T10:00:00", "fin": "2030-01-15T11:00:00"}'

# Repetir la misma reserva -> 409 Conflict

# Listar las reservas de la sala 1 ese día
curl "localhost:8080/api/reservas?salaId=1&fecha=2030-01-15&pagina=0&tamano=10"

# Cancelar la reserva 1 -> 204 No Content
curl -u admin:admin123 -X DELETE localhost:8080/api/reservas/1
```

### Errores

Todos los errores devuelven el mismo formato:

```json
{
  "estado": 400,
  "mensaje": "Hay datos no válidos",
  "errores": { "capacidad": "La capacidad debe ser al menos 1" }
}
```

| Código | Cuándo |
|--------|--------|
| 400 | Faltan campos, formato incorrecto, fin no posterior al inicio, inicio en el pasado, paginación fuera de rango |
| 401 | Se intenta crear o cancelar sin usuario/contraseña |
| 404 | La sala o la reserva no existen |
| 409 | La sala ya está ocupada en ese horario, ya existe una sala con ese nombre o la reserva ya estaba cancelada |

## Estructura del código

```
src/main/java/com/ejemplo/reservas
├── sala/        Entidad, repositorio, servicio y controlador de salas
├── reserva/     Lo mismo para reservas, más la respuesta paginada
├── error/       Excepciones propias y el manejador que las convierte en 400/404/409
└── seguridad/   Configuración de Spring Security
src/main/resources/db/migration   Migraciones de Flyway (V1 salas, V2 reservas)
```

Cada parte sigue el mismo esquema: **Controller** (recibe la petición HTTP) →
**Service** (reglas de negocio) → **Repository** (base de datos).

## Decisiones que tomé

- **H2 en memoria** en lugar de PostgreSQL: así el proyecto se arranca con un solo comando,
  sin instalar nada. Las migraciones de Flyway usan SQL estándar, por lo que pasar a
  PostgreSQL sería cambiar la URL y añadir el driver.
- **Autenticación Basic** en lugar de JWT: el enunciado permite las dos y Basic es mucho más
  simple de configurar y de probar (`curl -u`). Spring Boot crea el usuario a partir del
  `application.yml`.
- **Cancelar no borra la reserva**: la marca como `cancelada`. Así se conserva el histórico y
  la reserva cancelada deja de bloquear la sala. Cancelar dos veces devuelve 409.
- **Cómo se detecta el solapamiento**: dos reservas chocan si una empieza antes de que termine
  la otra (`inicio < finOtra` y `fin > inicioOtra`). Por eso una reserva de 10:00 a 11:00 y otra
  de 11:00 a 12:00 **sí** se permiten: se pueden encadenar.
- **El filtro por fecha** devuelve las reservas que ocupan cualquier momento de ese día, también
  las que empiezan el día anterior y terminan ese día.
- **Las fechas no llevan zona horaria** (`LocalDateTime`): se asume que todas las salas están en
  la misma zona horaria.
- **No se permiten reservas en el pasado** (`@Future` en la fecha de inicio).
- **Formato de error propio y sencillo** (`estado`, `mensaje`, `errores`) para que el cliente
  sepa exactamente qué campo corregir.
- **Respuesta paginada propia** (`PaginaResponse`) en vez de devolver el `Page` de Spring tal cual,
  para controlar el JSON y que los nombres estén en español.

## Qué haría con más tiempo

- **Concurrencia**: ahora mismo, si dos peticiones intentan reservar el mismo hueco exactamente
  a la vez, las dos podrían pasar la comprobación. Lo solucionaría con un bloqueo de la fila de
  la sala (`SELECT ... FOR UPDATE`) o, en PostgreSQL, con una restricción `EXCLUDE` sobre el rango
  de fechas, que lo impide directamente en la base de datos.
- **PostgreSQL con Docker Compose** y tests de integración con **Testcontainers** contra
  PostgreSQL real.
- **JWT y usuarios en base de datos**, con roles (por ejemplo, que solo el que creó la reserva o
  un administrador puedan cancelarla) y guardar quién creó cada reserva.
- **Documentación con OpenAPI/Swagger** (springdoc) para probar la API desde el navegador.
- Más reglas de negocio: duración máxima de una reserva, horario de apertura, capacidad frente
  a número de asistentes.
- Evitar el problema N+1 al listar reservas (ahora se carga la sala de cada reserva por separado).
- Zonas horarias (`OffsetDateTime`) si las salas estuvieran en distintos países.

## Cómo usé la IA

Usé **Claude** (Anthropic) como asistente para escribir el proyecto.

**Qué le pedí:**
- Le pasé el enunciado completo y le pedí que generara el proyecto con commits progresivos.
- Después de ver la primera versión, le pedí que **todo estuviera en español** (clases, métodos,
  mensajes de error, commits y README) y que el código fuera **más sencillo y legible**.

**Qué acepté:**
- La estructura por paquetes (sala, reserva, error, seguridad) y el esquema Controller → Service → Repository.
- La consulta de solapamiento y la idea de cancelar marcando la reserva en lugar de borrarla.
- Los tests: uno unitario con Mockito y uno de integración con MockMvc.

**Qué corregí o cambié:**
- La primera versión era demasiado compleja para lo que pide el enunciado: tenía bloqueos
  pesimistas, `ProblemDetail` (RFC 7807) y `Specification` de JPA.
  Pedí simplificarla: ahora el filtro es una única consulta JPQL, el error es un JSON propio
  muy simple y el bloqueo quedó como mejora futura (explicada arriba).
- Estaba en inglés; pedí pasarlo todo a español.
- Revisé que el código compila y que los tests pasan con `mvn test`.
