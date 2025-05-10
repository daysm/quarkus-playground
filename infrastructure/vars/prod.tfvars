gcp_project_id                  = "quarkus-playground-prod"
gcp_region                      = "us-east1"
env                             = "prod"
git_repository                  = "daysm/quarkus-playground"
image_tag                       = "latest" # Placeholder - will be overridden by GitHub Actions
additional_images_to_keep_count = 2
delete_images_older_than        = "1d"