resource "google_artifact_registry_repository" "docker_repo" {
  provider = google

  location      = var.gcp_region
  repository_id = "images-${var.env}"
  description   = "Docker repository for container images"
  format        = "DOCKER"
}