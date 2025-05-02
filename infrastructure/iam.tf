resource "google_iam_workload_identity_pool" "github_pool" {
  depends_on                = [google_project_service.iam_api]
  project                   = var.gcp_project_id
  workload_identity_pool_id = "gh-actions-identity-pool-${var.env}"
  display_name              = "GitHub Actions Pool (${var.env})"
}

resource "google_iam_workload_identity_pool_provider" "github_provider" {
  project                            = var.gcp_project_id
  workload_identity_pool_id          = google_iam_workload_identity_pool.github_pool.workload_identity_pool_id
  workload_identity_pool_provider_id = "github-provider-${var.env}"
  display_name                       = "GitHub Actions Provider (${var.env})"

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

resource "google_project_iam_member" "github_sa_permissions" {
  for_each = toset([
    "roles/serviceusage.serviceUsageAdmin",
    "roles/iam.serviceAccountUser",
    "roles/iam.workloadIdentityUser",
    "roles/artifactregistry.writer",
    "roles/iam.securityAdmin",
    "roles/resourcemanager.projectIamAdmin",
    "roles/iam.workloadIdentityPoolAdmin",
    "roles/storage.admin",
    "roles/run.admin",
    "roles/secretmanager.secretAccessor"
  ])

  project = var.gcp_project_id
  role    = each.key
  member  = "serviceAccount:${google_service_account.github_pusher_sa.email}"
}

resource "google_service_account_iam_member" "github_identity_binding" {
  service_account_id = google_service_account.github_pusher_sa.name
  role               = "roles/iam.workloadIdentityUser"
  member             = "principalSet://iam.googleapis.com/${google_iam_workload_identity_pool.github_pool.name}/attribute.repository/${var.git_repository}"
}

resource "google_storage_bucket_iam_member" "terraform_state_access" {
  bucket = "terraform-state-xfb0phm2"
  role   = "roles/storage.admin"
  member = "serviceAccount:${google_service_account.github_pusher_sa.email}"
}

resource "google_storage_bucket_iam_member" "terraform_state_list_access" {
  bucket = "terraform-state-xfb0phm2"
  role   = "roles/storage.objectViewer"
  member = "serviceAccount:${google_service_account.github_pusher_sa.email}"
}

resource "google_secret_manager_secret_iam_member" "db_url_accessor" {
  secret_id = google_secret_manager_secret.db_url.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.cloud_run_sa.email}"
}

resource "google_secret_manager_secret_iam_member" "db_username_accessor" {
  secret_id = google_secret_manager_secret.db_username.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.cloud_run_sa.email}"
}

resource "google_secret_manager_secret_iam_member" "db_password_accessor" {
  secret_id = google_secret_manager_secret.db_password.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.cloud_run_sa.email}"
}