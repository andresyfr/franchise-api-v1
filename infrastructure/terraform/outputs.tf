output "project_id" {
  description = "Atlas project ID."
  value       = mongodbatlas_project.this.id
}

output "cluster_name" {
  description = "Atlas cluster name."
  value       = mongodbatlas_advanced_cluster.this.name
}

output "standard_srv" {
  description = "SRV host of the cluster, without credentials."
  value       = mongodbatlas_advanced_cluster.this.connection_strings.standard_srv
}

# Value for SPRING_DATA_MONGODB_URI in Render.
# Read with: terraform output -raw spring_data_mongodb_uri
output "spring_data_mongodb_uri" {
  description = "Full connection URI for franchise-service."
  sensitive   = true
  value = format(
    "mongodb+srv://%s:%s@%s/%s?retryWrites=true&w=majority",
    mongodbatlas_database_user.app.username,
    random_password.database.result,
    trimprefix(mongodbatlas_advanced_cluster.this.connection_strings.standard_srv, "mongodb+srv://"),
    var.database_name
  )
}