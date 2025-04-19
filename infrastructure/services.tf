resource "google_project_service" "artifact_registry" {
  service = "artifactregistry.googleapis.com"
}

resource "google_project_service" "iam_credentials" {
  service = "iamcredentials.googleapis.com"
}
