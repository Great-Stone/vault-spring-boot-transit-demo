terraform {
  required_providers {
    docker = {
      source  = "kreuzwerker/docker"
    }
  }
}

provider "docker" {}

# Pulls the image
resource "docker_image" "mysql" {
  name = "mysql:8.0.31"
}

# Create a container
resource "docker_container" "mysql" {
  image = docker_image.mysql.image_id
  name  = "mysql"

  env = [
    "MYSQL_ROOT_PASSWORD=password"
  ]

  ports {
    internal = "3306"
    external = "3306"
  }
}