resource "google_project_service" "artifact_registry" {
  service = "artifactregistry.googleapis.com"
}