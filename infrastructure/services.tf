resource "google_project_service" "artifact_registry" {
  project = var.gcp_project_id
  service = "artifactregistry.googleapis.com"
  disable_dependent_services = false
  disable_on_destroy = false
}

resource "google_project_service" "iam_credentials" {
  project = var.gcp_project_id
  service = "iamcredentials.googleapis.com"
}

resource "google_project_service" "iam_api" {
  project = var.gcp_project_id
  service = "iam.googleapis.com"
  disable_dependent_services = false
  disable_on_destroy = false
}

resource "google_project_service" "cloudresourcemanager" {
  project = var.gcp_project_id
  service = "cloudresourcemanager.googleapis.com"
  disable_dependent_services = false
  disable_on_destroy = false
}