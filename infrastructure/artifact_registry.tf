resource "google_artifact_registry_repository" "docker_repo" {
  provider = google

  location      = var.gcp_region
  repository_id = "images-${var.env}"
  description   = "Docker repository for container images"
  format        = "DOCKER"

  cleanup_policies {
    id     = "keep-deployed-versions"
    action = "KEEP"
    condition {
      tag_prefixes          = [var.image_tag]
      tag_state             = "TAGGED"
      package_name_prefixes = ["app"]
    }
  }

  cleanup_policies {
    id     = "keep-most-recent-versions"
    action = "KEEP"
    most_recent_versions {
      keep_count            = var.additional_images_to_keep_count
      package_name_prefixes = ["app"]
    }
  }

  cleanup_policies {
    id     = "delete-old-versions"
    action = "DELETE"
    condition {
      # Delete versions of 'app' images older than the specified duration
      # IF AND ONLY IF they were NOT kept by the preceding 'KEEP' policies.
      older_than            = var.delete_images_older_than
      package_name_prefixes = ["app"]
      tag_state             = "ANY"
    }
  }
}