locals {
  api_key_assignments = {
    "developer_dayyan" = "API key for Dayyan"
  }
}

resource "random_string" "api_key_parts" {
  for_each = local.api_key_assignments

  length  = 22
  upper   = true
  lower   = true
  numeric = true
  special = false
}

locals {
  developer_api_keys_json_content = jsonencode({
    for logical_name, description in local.api_key_assignments :
    "sk-${random_string.api_key_parts[logical_name].result}" => description
  })
}

resource "google_secret_manager_secret" "developer_api_keys" {
  project   = var.gcp_project_id
  secret_id = "developer-api-keys-${var.env}"

  replication {
    auto {}
  }
  depends_on = [google_project_service.secret_manager]
}

resource "null_resource" "api_key_tracker" {
  # This resource will be replaced whenever the api_key_assignments change.
  # The replacement of google_secret_manager_secret_version cannot be triggered by a hash, e.g. in locals, directly.
  triggers = {
    api_key_assignments_hash = md5(jsonencode(local.api_key_assignments))
  }
}

resource "google_secret_manager_secret_version" "developer_api_keys_version" {
  secret         = google_secret_manager_secret.developer_api_keys.id
  secret_data_wo = local.developer_api_keys_json_content

  lifecycle {
    replace_triggered_by = [
      null_resource.api_key_tracker.id
    ]
  }
}
