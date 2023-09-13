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

provider "vault" {}

resource "vault_mount" "transit" {
  path                      = "transit"
  type                      = "transit"
  description               = "Example description"
  default_lease_ttl_seconds = 3600
  max_lease_ttl_seconds     = 86400
}

resource "vault_transit_secret_backend_key" "key" {
  backend = vault_mount.transit.path
  name    = "ds-poc"
  type = "aes256-gcm96"
}