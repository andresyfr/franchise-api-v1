# Infraestructura: MongoDB Atlas con Terraform

Aprovisiona la persistencia de `franchise-service`:

| Recurso | Descripción |
|---|---|
| `mongodbatlas_project` | Proyecto de Atlas |
| `mongodbatlas_advanced_cluster` | Cluster gratuito **M0** |
| `mongodbatlas_database_user` | Usuario `readWrite` solo sobre `franchisedb` |
| `mongodbatlas_project_ip_access_list` | Lista de acceso de red |

## Requisitos

- Terraform 1.7 o superior.
- Cuenta de MongoDB Atlas (gratuita) y su **Organization ID**.
- Una **Service Account** de la organización con rol *Organization Owner* (Atlas > Organization > Access Manager > Applications > Service Accounts).

## Uso

```bash
cd infrastructure/terraform

export TF_VAR_atlas_client_id="<client-id>"
export TF_VAR_atlas_client_secret="<client-secret>"

cp terraform.tfvars.example terraform.tfvars   # completa atlas_org_id

terraform init
terraform fmt -check
terraform validate
terraform plan -out=tfplan      # verifica: instance_size = "M0", provider_name = "TENANT"
terraform apply tfplan

terraform output -raw spring_data_mongodb_uri
```

El último comando imprime el valor de `SPRING_DATA_MONGODB_URI` para Render. No lo agreguen en archivos versionados para efectos de este test agrego la guía cómo ejemplo de replicación estimados entrevistadores.

## Costos

El cluster M0 es gratuito. Si el plan muestra `FLEX`, `M10` o superior, **no apliquen esto es automático**.

## Seguridad

- `0.0.0.0/0` se permite porque Render Free no tiene IP de salida fija. El acceso sigue protegido por usuario, contraseña y TLS. En producción se usaría IP fija o conectividad privada.
- El estado local (`terraform.tfstate`) contiene la contraseña del usuario: no se versiona.

## Eliminar la infraestructura

```bash
terraform destroy
```