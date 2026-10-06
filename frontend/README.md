# Frontend

Cada aplicación web vive en su propia carpeta, con su `package.json` y su `Dockerfile`.

```text
frontend/
├── franchise-web/      # ejemplo: Angular o React
└── backoffice-web/     # ejemplo: otra app independiente
```

Convenciones:

- Una carpeta por app, en `kebab-case`.
- Cada app expone sus scripts `build` y `test` en `package.json`.
- Cada app tiene su `Dockerfile` y se agrega a `docker-compose.yml`.
- El CI (`frontend-ci.yml`) detecta automáticamente cada carpeta con `package.json`.
- Las apps consumen el backend mediante una variable de entorno (por ejemplo `API_BASE_URL`), nunca con URLs fijas.
