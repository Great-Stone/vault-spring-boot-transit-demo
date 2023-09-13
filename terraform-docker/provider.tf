terraform {
  required_providers {
    docker = {
      source = "kreuzwerker/docker"
      version = "~> 3.0"
    }
    vault = {
      source = "hashicorp/vault"
      version = "~> 3.0"
    }
  }
}
