# Authentication with Atlas Service Account.
# The credentials arrive via environment variables TF_VAR_*; they are never versioned.
provider "mongodbatlas" {
  client_id     = var.atlas_client_id
  client_secret = var.atlas_client_secret
}