provider "yandex" {
  token     = "<YOUR_YANDEX_CLOUD_API_TOKEN>"
  cloud_id  = "<YOUR_CLOUD_ID>"
  folder_id = "<YOUR_FOLDER_ID>"
  zone      = "ru-central1-a"  # Зона
}

resource "yandex_storage_bucket" "my_bucket" {
  name        = "my-unsafe-bucket"
  access_key  = "<YOUR_ACCESS_KEY>"
  secret_key  = "<YOUR_SECRET_KEY>"
  acl         = "public-read"  # Уязвимость: публичный доступ к S3 бакету
  storage_class = "standard"
}

resource "yandex_vpc_security_group" "my_sg" {
  name        = "my-unsafe-sg"
  description = "Security group with open ports"

  ingress {
    description = "Allow all inbound traffic"
    protocol    = "TCP"
    from_port   = 0
    to_port     = 65535
    cidr_blocks = ["0.0.0.0/0"]  # Уязвимость: открытые порты для всех
  }

  egress {
    description = "Allow all outbound traffic"
    protocol    = "TCP"
    from_port   = 0
    to_port     = 65535
    cidr_blocks = ["0.0.0.0/0"]  # Уязвимость: открытые порты для всех
  }
}
