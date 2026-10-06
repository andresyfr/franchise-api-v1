# Franchise Platform

Monorepo con los servicios de backend y las aplicaciones de frontend para la gestión de franquicias, sucursales y productos.

## Estructura

```text
.
├── backend/                  # Servicios Spring Boot (Gradle multi-proyecto)
│   └── franchise-service/      # API reactiva de franquicias
├── frontend/                 # Aplicaciones web (una carpeta por app)
├── infrastructure/terraform/ # Infraestructura como código (MongoDB Atlas)
├── docs/                     # Diseño y arquitectura
├── docker-compose.yml        # Entorno local completo
└── .github/workflows/        # CI independiente para backend y frontend
```

## Ejecución local

```bash
docker compose up --build
```

| URL | Descripción |
|---|---|
| http://localhost:8080/swagger-ui.html | Documentación de la API |
| http://localhost:8080/actuator/health | Health check |
| http://localhost:8080/actuator/prometheus | Métricas |

Observabilidad local (Grafana en http://localhost:3000):

```bash
OTLP_EXPORT_ENABLED=true docker compose --profile observability up --build
```

## Documentación

- [Backend](backend/README.md)
- [franchise-service](backend/franchise-service/README.md)
- [Frontend](frontend/README.md)
- [Arquitectura y diseño](docs/ARCHITECTURE.md)
