variable "atlas_client_id" {
  description = "MongoDB Atlas Service Account client ID (TF_VAR_atlas_client_id)."
  type        = string
  sensitive   = true
}

variable "atlas_client_secret" {
  description = "MongoDB Atlas Service Account client secret (TF_VAR_atlas_client_secret)."
  type        = string
  sensitive   = true
}

variable "atlas_org_id" {
  description = "MongoDB Atlas organization ID."
  type        = string
}

variable "project_name" {
  description = "Atlas project name."
  type        = string
  default     = "franchise-platform"
}

variable "cluster_name" {
  description = "Atlas cluster name."
  type        = string
  default     = "franchise-cluster"
}

variable "backing_provider" {
  description = "Cloud provider that hosts the free cluster (AWS, GCP or AZURE)."
  type        = string
  default     = "AWS"

  validation {
    condition     = contains(["AWS", "GCP", "AZURE"], var.backing_provider)
    error_message = "backing_provider must be AWS, GCP or AZURE."
  }
}

variable "region" {
  description = "Atlas region for the free cluster. US_EAST_1 is close to Render's Virginia region."
  type        = string
  default     = "US_EAST_1"
}

variable "database_name" {
  description = "Database used by franchise-service."
  type        = string
  default     = "franchisedb"
}

variable "database_username" {
  description = "Database user for franchise-service."
  type        = string
  default     = "franchise_app"
}

variable "allowed_cidr_blocks" {
  description = <<-EOT
    CIDR blocks allowed to reach the cluster.
    Render Free has no static outbound IP, so the default allows any address;
    access is still protected by user/password and TLS.
  EOT
  type        = list(string)
  default     = ["0.0.0.0/0"]
}