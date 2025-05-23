output "workload_identity_provider_id" {
  description = "The full ID of the Workload Identity Provider for GitHub Actions"
  value       = google_iam_workload_identity_pool_provider.github_provider.name
}

output "service_account_email" {
  description = "The email of the Service Account for GitHub Actions"
  value       = google_service_account.github_sa.email
}

output "github_principal_set" {
  description = "The principal set identifier for the GitHub repository identity"
  value       = "principalSet://iam.googleapis.com/${google_iam_workload_identity_pool_provider.github_provider.name}/subject/repo:${var.git_repository}:*"
}

output "cloud_run_service_url" {
  description = "The URL of the deployed Cloud Run service"
  value       = google_cloud_run_v2_service.default.uri
}