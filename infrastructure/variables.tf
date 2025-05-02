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