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
- [Dashboard de Observabilidad para importar](docs/dashboard_grafana/franchise-service-overview.json)
```bash
OTLP_EXPORT_ENABLED=true docker compose --profile observability up --build
```

### Por favor, observe el video enviado en el adjunto del email para más detalles del MVP en vivo

## Ejecución nube

Ingresar a:
[https://franchise-service-zl3w.onrender.com/swagger-ui/index.html](https://franchise-service-zl3w.onrender.com/swagger-ui/index.html)

Empezará a cargar lo necesario para la ejecución de la API en la nube, por favor espere unos minutos.

```link
Una vez logren observar la documentación interactiva de la API pueden ingresar a los siguiente links:
```

| URL | Descripción |
|---|---|
| https://franchise-service-zl3w.onrender.com/swagger-ui.html | Documentación de la API |
| https://franchise-service-zl3w.onrender.com/actuator/health | Health check |
| https://franchise-service-zl3w.onrender.com/actuator/prometheus | Métricas |

## Documentación General

- [Backend](backend/README.md)
- [franchise-service](backend/franchise-service/README.md)
- [Frontend](frontend/README.md)
- [Arquitectura y diseño](docs/ARCHITECTURE.md)

## Documentación Técnica Generada
#### Visualizar en un navegador Web

- [Javadoc](docs/docs/javadoc/index.html)
- [Resultados de cobertura con JaCoCo](docs/tests/reports/jacoco/test/html/index.html)
- [Resultados de tests unitarios](docs/tests/reports/tests/test/index.html)

## Estrategia de infraestructura y portabilidad

Por fines prácticos, el MVP fue desplegado utilizando alternativas de bajo costo y recursos gratuitos, con el objetivo de demostrar su funcionamiento sin generar costos operativos innecesarios. La infraestructura fue definida mediante **Terraform**, manteniendo el aprovisionamiento desacoplado del código de negocio y siguiendo el criterio de infraestructura como código solicitado para la solución.

Esta decisión no limita la evolución del proyecto. La aplicación queda preparada para migrarse posteriormente a **AWS**, incluyendo escenarios sobre **EKS**, mediante la incorporación o adaptación de módulos y proveedores de Terraform. De esta forma, pueden conservarse el código de negocio, los contenedores, las pruebas, la observabilidad y los pipelines existentes, mientras la capa de infraestructura evoluciona según las necesidades del entorno.

Para el desarrollo y las validaciones locales, también puede utilizarse **MiniStack**, como alternativa aprobada internamente en el Banco para simular y gestionar servicios de infraestructura desde el equipo del desarrollador asignado. Esto permite probar integraciones y automatizaciones sin depender permanentemente de recursos desplegados en la nube.

En resumen, el enfoque permite:

- demostrar el MVP con costos mínimos;
- mantener la infraestructura versionada y reproducible;
- evitar el acoplamiento directo con un único proveedor;
- conservar los desarrollos al migrar hacia AWS y EKS;
- ejecutar pruebas locales mediante MiniStack o similares;
- evolucionar progresivamente hacia escenarios de rendimiento, observabilidad y FinOps.