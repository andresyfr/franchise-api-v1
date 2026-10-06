# Backend

Build Gradle multi-proyecto. Cada carpeta con `build.gradle` es un servicio y se incluye automáticamente.

| Servicio | Descripción | Puerto local |
|---|---|---|
| `franchise-service` | API reactiva de franquicias, sucursales y productos | 8080 |

## Comandos

```bash
cd backend
./gradlew build                          # compila y prueba todos los servicios
./gradlew :franchise-service:bootRun       # ejecuta un servicio
./gradlew :franchise-service:test          # pruebas de un servicio
```

## Agregar un servicio nuevo

1. Crear `backend/<nuevo-servicio>/build.gradle` (puedes partir de `franchise-service/build.gradle`).
2. Crear `backend/<nuevo-servicio>/src/main/java/com/andresyfr/<modulo>/`.
3. Crear su `Dockerfile` usando `backend/` como contexto.
4. Agregarlo a `docker-compose.yml` y a la matriz de `.github/workflows/backend-ci.yml`.

Las convenciones comunes (Java 21, JaCoCo, encoding, JUnit) viven en `backend/build.gradle`.
