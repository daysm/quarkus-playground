resource "google_service_account" "cloud_run_sa" {
  project      = var.gcp_project_id
  account_id   = "cloud-run-sa-${var.env}"
  display_name = "Service account for Cloud Run service (${var.env})"
}

resource "google_cloud_run_v2_service" "default" {

  name     = "quarkus-playground-${var.env}"
  location = var.gcp_region

  template {
    containers {
      image = "${var.gcp_region}-docker.pkg.dev/${var.gcp_project_id}/images-${var.env}/app:${var.image_tag}"
      ports {
        container_port = 8080
      }
      env {
        name = "DATABASE_URL"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.db_url.secret_id
            version = "latest"
          }
        }
      }
      env {
        name = "DATABASE_USER"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.db_username.secret_id
            version = "latest"
          }
        }
      }
      env {
        name = "DATABASE_PASSWORD"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.db_password.secret_id
            version = "latest"
          }
        }
      }
    }
    scaling {
      min_instance_count = 0
      max_instance_count = 1
    }
    service_account = google_service_account.cloud_run_sa.email
  }

  ingress = "INGRESS_TRAFFIC_ALL"

  depends_on = [
    google_project_service.cloud_run,
    google_artifact_registry_repository.docker_repo
  ]
}

resource "google_cloud_run_v2_service_iam_member" "allow_all_users" {
  provider = google

  location = google_cloud_run_v2_service.default.location
  name     = google_cloud_run_v2_service.default.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}
