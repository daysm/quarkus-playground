variable "gcp_project_id" {
  description = "The GCP project ID"
  type        = string
}

variable "gcp_region" {
  description = "The GCP region"
  type        = string
}

variable "env" {
  description = "The environment"
  type        = string
}

variable "git_repository" {
  description = "Git repository"
  type        = string
}

variable "image_tag" {
  description = "The Docker image tag to deploy to Cloud Run"
  type        = string
}

variable "additional_images_to_keep_count" {
  description = "Number of most recent image versions to keep per package (e.g., for 'app' images)."
  type        = number
  validation {
    condition     = var.additional_images_to_keep_count >= 0
    error_message = "The number of additional images to keep must be non-negative."
  }
}

variable "delete_images_older_than" {
  description = "Delete image versions older than this duration."
  type        = string
  default     = "1d"
}