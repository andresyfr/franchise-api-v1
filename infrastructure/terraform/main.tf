resource "mongodbatlas_project" "this" {
  name   = var.project_name
  org_id = var.atlas_org_id
}

# Free cluster (M0). Before 'apply', check in the plan:
#   instance_size = "M0" y provider_name = "TENANT".
resource "mongodbatlas_advanced_cluster" "this" {
  project_id   = mongodbatlas_project.this.id
  name         = var.cluster_name
  cluster_type = "REPLICASET"

  replication_specs = [
    {
      region_configs = [
        {
          electable_specs = {
            instance_size = "M0"
          }
          provider_name         = "TENANT"
          backing_provider_name = var.backing_provider
          region_name           = var.region
          priority              = 7
        }
      ]
    }
  ]
}

# Without special characters to avoid having to encode it in the URI.
resource "random_password" "database" {
  length  = 32
  special = false
}

# User with minimum permissions: read/write only on the application database.
resource "mongodbatlas_database_user" "app" {
  project_id         = mongodbatlas_project.this.id
  username           = var.database_username
  password           = random_password.database.result
  auth_database_name = "admin"

  roles {
    role_name     = "readWrite"
    database_name = var.database_name
  }

  scopes {
    name = mongodbatlas_advanced_cluster.this.name
    type = "CLUSTER"
  }
}

resource "mongodbatlas_project_ip_access_list" "allowed" {
  for_each = toset(var.allowed_cidr_blocks)

  project_id = mongodbatlas_project.this.id
  cidr_block = each.value
  comment    = "franchise-service access (Render)"
}