terraform {
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "6.30.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "3.7.2"
    }
  }

  backend "gcs" {
    bucket = "terraform-state-xfb0phm2"
    prefix = "quarkus-playground"
  }
}

provider "google" {
  project = var.gcp_project_id
  region  = var.gcp_region
}
