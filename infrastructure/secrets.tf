resource "google_secret_manager_secret" "db_url" {
  project   = var.gcp_project_id
  secret_id = "quarkus-playground-db-url-${var.env}"

  replication {
    auto {}
  }

  depends_on = [google_project_service.secret_manager]
}

resource "google_secret_manager_secret" "db_username" {
  project   = var.gcp_project_id
  secret_id = "quarkus-playground-db-username-${var.env}"

  replication {
    auto {}
  }

  depends_on = [google_project_service.secret_manager]
}

resource "google_secret_manager_secret" "db_password" {
  project   = var.gcp_project_id
  secret_id = "quarkus-playground-db-password-${var.env}"

  replication {
    auto {}
  }

  depends_on = [google_project_service.secret_manager]
}
