resource "google_iam_workload_identity_pool" "github_pool" {
  project = var.gcp_project_id
  workload_identity_pool_id = "gh-actions-identity-pool-${var.env}"
  display_name = "GitHub Actions Pool (${var.env})"
}

resource "google_iam_workload_identity_pool_provider" "github_provider" {
  project                 = var.gcp_project_id
  workload_identity_pool_id = google_iam_workload_identity_pool.github_pool.workload_identity_pool_id
  workload_identity_pool_provider_id = "github-provider-${var.env}"
  display_name            = "GitHub Actions Provider (${var.env})"

  oidc {
    issuer_uri = "https://token.actions.githubusercontent.com"
  }

  attribute_mapping = {
    "google.subject"       = "assertion.repository"
    "attribute.actor"      = "assertion.actor"
    "attribute.repository" = "assertion.repository"
    "attribute.branch"     = "assertion.ref"
  }
  attribute_condition = "attribute.repository == \"${var.git_repository}\""
}

resource "google_service_account" "github_pusher_sa" {
  project      = var.gcp_project_id
  account_id   = "github-pusher-sa-${var.env}"
  display_name = "Service account for GitHub Actions pushing to Artifact Registry (${var.env})"
}

resource "google_project_iam_member" "sa_artifact_registry_writer" {
  project = var.gcp_project_id
  role    = "roles/artifactregistry.writer"
  member  = "serviceAccount:${google_service_account.github_pusher_sa.email}"
}

resource "google_service_account_iam_member" "github_identity_binding" {
  service_account_id = google_service_account.github_pusher_sa.name
  role               = "roles/iam.workloadIdentityUser"
  member             = "principalSet://iam.googleapis.com/${google_iam_workload_identity_pool.github_pool.name}/attribute.repository/${var.git_repository}"
}