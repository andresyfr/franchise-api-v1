terraform {
  required_version = ">= 1.7.0"

  required_providers {
    mongodbatlas = {
      source  = "mongodb/mongodbatlas"
      version = "~> 2.2"
    }

    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }
}