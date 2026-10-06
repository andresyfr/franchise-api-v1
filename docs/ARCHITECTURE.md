# Franchise API – Diseño y planeación

Este documento recoge el diseño previo a la implementación: alcance, organización del monorepo, modelo de dominio, arquitectura, flujos principales, persistencia, despliegue y estrategia de trabajo con Git.

> Los diagramas usan Mermaid y GitHub los renderiza directamente.

---

## 1. Alcance y requisitos

| # | Requisito | Tipo | Endpoint |
|---|---|---|---|
| 1 | Proyecto en Spring Boot | Obligatorio | — |
| 2 | Agregar franquicia | Obligatorio | `POST /api/v1/franchises` |
| 3 | Agregar sucursal a una franquicia | Obligatorio | `POST /api/v1/franchises/{franchiseId}/branches` |
| 4 | Agregar producto a una sucursal | Obligatorio | `POST /api/v1/branches/{branchId}/products` |
| 5 | Eliminar producto de una sucursal | Obligatorio | `DELETE /api/v1/branches/{branchId}/products/{productId}` |
| 6 | Modificar stock de un producto | Obligatorio | `PATCH /api/v1/products/{productId}/stock` |
| 7 | Producto con más stock por sucursal de una franquicia | Obligatorio | `GET /api/v1/franchises/{franchiseId}/top-stock-products` |
| 8 | Persistencia en la nube | Obligatorio | MongoDB Atlas |
| E1 | Empaquetado con Docker | Extra | — |
| E2 | Programación reactiva | Extra | WebFlux + Reactive MongoDB |
| E3 | Actualizar nombre de franquicia | Extra | `PATCH /api/v1/franchises/{franchiseId}/name` |
| E4 | Actualizar nombre de sucursal | Extra | `PATCH /api/v1/branches/{branchId}/name` |
| E5 | Actualizar nombre de producto | Extra | `PATCH /api/v1/products/{productId}/name` |
| E6 | Persistencia como IaC | Extra | Terraform (MongoDB Atlas) |
| E7 | Solución desplegada en la nube | Extra | Render + MongoDB Atlas |

### Decisiones y supuestos

| Tema | Decisión |
|---|---|
| Stock | Entero mayor o igual a 0 |
| Nombres | Obligatorios, sin espacios al inicio o al final, únicos dentro de su padre |
| Empate en mayor stock | Se retornan todos los productos empatados de la sucursal |
| Sucursal sin productos | No aparece en el resultado del endpoint 7 |
| Eliminación de producto | Se valida que el producto pertenezca a la sucursal indicada |
| Errores | RFC 9457 (Problem Details) con un `code` estable |

---

## 2. Contexto del sistema

```mermaid
flowchart LR
    client["Cliente API<br/>(Postman, Swagger UI, curl)"]
    web["Apps frontend<br/>frontend/* (futuro)"]
    api["franchise-service<br/>Spring Boot WebFlux"]
    db[("MongoDB Atlas<br/>Free cluster")]
    obs["Stack de observabilidad<br/>(opcional, OTLP)"]

    client -- "HTTPS / JSON" --> api
    web -. "HTTPS / JSON<br/>API_BASE_URL" .-> api
    api -- "Reactive Streams driver" --> db
    api -. "Trazas y métricas OTLP" .-> obs
```

---

## 3. Organización del monorepo

El repositorio separa backend, frontend e infraestructura para que cada parte crezca y se despliegue de forma independiente.

```mermaid
flowchart TB
    root["franchise-api-v1 (monorepo)"]

    subgraph backend["backend/ — Gradle multi-proyecto"]
        bconv["build.gradle<br/>convenciones comunes"]
        bset["settings.gradle<br/>auto-descubrimiento de servicios"]
        fs["franchise-service/<br/>API reactiva"]
        fut1["&lt;nuevo-servicio&gt;/<br/>(futuro)"]
    end

    subgraph frontend["frontend/ — una carpeta por app"]
        fw["franchise-web/<br/>(futuro)"]
        bo["backoffice-web/<br/>(futuro)"]
    end

    subgraph infra["infrastructure/"]
        tf["terraform/<br/>MongoDB Atlas"]
    end

    subgraph ci[".github/workflows/"]
        bci["backend-ci.yml<br/>paths: backend/**"]
        fci["frontend-ci.yml<br/>paths: frontend/**"]
    end

    docs["docs/<br/>ARCHITECTURE.md"]
    compose["docker-compose.yml<br/>entorno local"]

    root --> backend
    root --> frontend
    root --> infra
    root --> ci
    root --> docs
    root --> compose

    bconv -. "aplica a" .-> fs
    bconv -. "aplica a" .-> fut1
    bset -. "incluye" .-> fs
    bset -. "incluye" .-> fut1
    bci -. "valida" .-> backend
    fci -. "valida" .-> frontend
```

```text
franchise-api-v1/
├── backend/
│   ├── build.gradle                  # Java 21, JaCoCo, JUnit para todos los servicios
│   ├── settings.gradle               # incluye toda carpeta con build.gradle
│   ├── gradlew, gradle/
│   └── franchise-service/
│       ├── build.gradle              # dependencias del servicio
│       ├── Dockerfile                # contexto de build: backend/
│       └── src/
├── frontend/                         # una carpeta por app (package.json + Dockerfile)
├── infrastructure/terraform/
├── docs/ARCHITECTURE.md
├── .github/workflows/
│   ├── backend-ci.yml
│   └── frontend-ci.yml
├── docker-compose.yml
└── render.yaml
```

| Necesidad | Cómo se resuelve |
|---|---|
| Agregar un servicio backend | Crear `backend/<servicio>/build.gradle`; Gradle lo incluye automáticamente |
| Agregar una app frontend | Crear `frontend/<app>/package.json`; el CI la detecta automáticamente |
| Configuración común | Centralizada en `backend/build.gradle` |
| CI eficiente | Cada workflow corre solo si cambian sus rutas |
| Despliegue independiente | Cada servicio o app tiene su propio `Dockerfile` |

---

## 4. Modelo de dominio

```mermaid
classDiagram
    direction LR

    class Franchise {
        +String id
        +String name
        +rename(String newName) Franchise
    }

    class Branch {
        +String id
        +String franchiseId
        +String name
        +rename(String newName) Branch
    }

    class Product {
        +String id
        +String branchId
        +String name
        +int stock
        +rename(String newName) Product
        +updateStock(int newStock) Product
    }

    class TopStockProduct {
        +String branchId
        +String branchName
        +String productId
        +String productName
        +int stock
    }

    Franchise "1" --> "0..*" Branch : tiene
    Branch "1" --> "0..*" Product : ofrece
    TopStockProduct ..> Branch : referencia
    TopStockProduct ..> Product : referencia
```

Reglas del dominio:

- `stock` nunca puede ser negativo.
- Las entidades son inmutables: `rename` y `updateStock` retornan una nueva instancia.
- El dominio no depende de Spring ni de MongoDB.

---

## 5. Arquitectura limpia

```mermaid
flowchart TB
    subgraph infrastructure["infrastructure"]
        direction TB
        rest["entrypoint/rest<br/>Controllers, DTOs, GlobalErrorHandler"]
        mongo["persistence/mongodb<br/>Documents, Reactive repositories, Adapters"]
        config["config<br/>OpenAPI, Observabilidad, Clock"]
    end

    subgraph application["application"]
        usecases["usecase<br/>CreateFranchise, AddBranch, AddProduct,<br/>RemoveProduct, UpdateStock, GetTopStockProducts, Rename..."]
    end

    subgraph domain["domain"]
        model["model<br/>Franchise, Branch, Product"]
        ports["port<br/>FranchiseRepository, BranchRepository, ProductRepository"]
        exceptions["exception<br/>DomainException"]
    end

    rest --> usecases
    usecases --> model
    usecases --> ports
    mongo -. "implementa" .-> ports
    mongo --> model
```

Reglas de dependencia (validadas con ArchUnit en `CleanArchitectureTest`):

| Capa | Puede depender de | No puede depender de |
|---|---|---|
| `domain` | Java, Reactor | `application`, `infrastructure`, Spring |
| `application` | `domain` | `infrastructure` |
| `infrastructure.entrypoint` | `application`, `domain` | `infrastructure.persistence` |
| `infrastructure.persistence` | `domain` | `infrastructure.entrypoint` |

### Estructura de paquetes

Ubicación: `backend/franchise-service/src/main/java/`

```text
com.andresyfr.franchise
├── domain
│   ├── model
│   ├── exception
│   └── port
├── application
│   └── usecase
└── infrastructure
    ├── config
    ├── entrypoint/rest
    │   └── error
    └── persistence/mongodb
```

---

## 6. Flujos principales

### 6.1 Crear franquicia

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    participant R as FranchiseController
    participant U as CreateFranchiseUseCase
    participant P as FranchiseRepository (port)
    participant A as MongoFranchiseAdapter
    participant DB as MongoDB

    C->>R: POST /api/v1/franchises {name}
    R->>R: Validar request (@Valid)
    R->>U: execute(name)
    U->>P: existsByName(name)
    P->>A: existsByName(name)
    A->>DB: find
    DB-->>A: resultado
    A-->>U: Mono<Boolean>
    alt Nombre ya existe
        U-->>R: error FRANCHISE_ALREADY_EXISTS
        R-->>C: 409 Conflict (Problem Details)
    else Nombre disponible
        U->>P: save(franchise)
        P->>A: save(franchise)
        A->>DB: insert
        DB-->>A: documento
        A-->>U: Mono<Franchise>
        U-->>R: Mono<Franchise>
        R-->>C: 201 Created {id, name}
    end
```

### 6.2 Modificar stock de un producto

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    participant R as ProductController
    participant U as UpdateProductStockUseCase
    participant P as ProductRepository (port)
    participant DB as MongoDB

    C->>R: PATCH /api/v1/products/{productId}/stock {stock}
    R->>U: execute(productId, stock)
    U->>P: findById(productId)
    P->>DB: find
    DB-->>P: documento o vacío
    alt Producto no existe
        U-->>R: error RESOURCE_NOT_FOUND
        R-->>C: 404 Not Found
    else Stock negativo
        U-->>R: error INVALID_STOCK
        R-->>C: 422 Unprocessable Entity
    else Válido
        U->>P: save(product.updateStock(stock))
        P->>DB: update
        DB-->>P: documento
        U-->>R: Mono<Product>
        R-->>C: 200 OK {id, name, stock}
    end
```

### 6.3 Producto con más stock por sucursal

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    participant R as FranchiseController
    participant U as GetTopStockProductsUseCase
    participant F as FranchiseRepository (port)
    participant B as BranchRepository (port)
    participant P as ProductRepository (port)

    C->>R: GET /api/v1/franchises/{franchiseId}/top-stock-products
    R->>U: execute(franchiseId)
    U->>F: findById(franchiseId)
    alt Franquicia no existe
        U-->>R: error RESOURCE_NOT_FOUND
        R-->>C: 404 Not Found
    else Franquicia existe
        U->>B: findByFranchiseId(franchiseId)
        B-->>U: Flux<Branch>
        loop Por cada sucursal
            U->>P: findTopStockByBranchId(branchId)
            P-->>U: Flux<Product> (incluye empates)
        end
        U-->>R: Flux<TopStockProduct>
        R-->>C: 200 OK [{branchId, branchName, productId, productName, stock}]
    end
```

---

## 7. Persistencia (MongoDB)

Se usan colecciones separadas en lugar de un único documento embebido. Así se evita que el documento de la franquicia crezca sin límite y las actualizaciones de stock o de nombres afectan un solo documento.

```mermaid
erDiagram
    FRANCHISES ||--o{ BRANCHES : "tiene"
    BRANCHES ||--o{ PRODUCTS : "ofrece"

    FRANCHISES {
        ObjectId _id PK
        string name UK
        date createdAt
        date updatedAt
    }

    BRANCHES {
        ObjectId _id PK
        ObjectId franchiseId FK
        string name
        date createdAt
        date updatedAt
    }

    PRODUCTS {
        ObjectId _id PK
        ObjectId branchId FK
        string name
        int stock
        date createdAt
        date updatedAt
    }
```

| Colección | Índice | Propósito |
|---|---|---|
| `franchises` | `{ name: 1 }` único | Evitar franquicias duplicadas |
| `branches` | `{ franchiseId: 1, name: 1 }` único | Listar sucursales y evitar duplicados por franquicia |
| `products` | `{ branchId: 1, name: 1 }` único | Evitar productos duplicados por sucursal |
| `products` | `{ branchId: 1, stock: -1 }` | Obtener rápido el mayor stock por sucursal |

---

## 8. Despliegue

```mermaid
flowchart LR
    dev["Desarrollador"]

    subgraph github["GitHub (repositorio público)"]
        repo["franchise-api-v1<br/>monorepo"]
        bci["backend-ci.yml<br/>build, tests, Docker por servicio"]
        fci["frontend-ci.yml<br/>build y tests por app"]
    end

    subgraph iac["Infraestructura como código"]
        tf["Terraform<br/>infrastructure/terraform"]
        bp["Render Blueprint<br/>render.yaml"]
    end

    subgraph render["Render (Free)"]
        web["Web Service: franchise-service<br/>Dockerfile: backend/franchise-service/Dockerfile<br/>Contexto: backend/"]
        fe["Static Site / Web Service<br/>frontend/* (futuro)"]
    end

    subgraph atlas["MongoDB Atlas (Free)"]
        cluster[("Cluster M0<br/>franchisedb")]
    end

    dev -- "git push / PR" --> repo
    repo -- "cambios en backend/**" --> bci
    repo -- "cambios en frontend/**" --> fci
    tf -- "terraform apply" --> cluster
    bp -- "define" --> web
    bp -. "define (futuro)" .-> fe
    repo -- "auto deploy en main<br/>buildFilter: backend/**" --> web
    web -- "SPRING_DATA_MONGODB_URI" --> cluster
    fe -. "API_BASE_URL" .-> web
```

| Componente | Herramienta | Administrado por |
|---|---|---|
| Base de datos | MongoDB Atlas M0 | Terraform (`infrastructure/terraform`) |
| Backend | Render Free (Docker) | `render.yaml` |
| Frontend (futuro) | Render Static Site | `render.yaml` |
| Secretos | Variables de entorno en Render | Panel de Render (no se versionan) |
| CI | GitHub Actions | `backend-ci.yml` y `frontend-ci.yml` |
