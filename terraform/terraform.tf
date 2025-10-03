terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.92"
    }
  }

  required_version = ">= 1.2"

  backend "s3" {
    bucket = "garm-tf-state"
    key    = "waes/rdc"
    region = "eu-west-1"
  }
}

variable "region" {
  type    = string
  default = "eu-west-3"
}

variable "db_username" {
  type    = string
  default = "rdc_user"
}

variable "db_password" {
  type = string
}

variable "subnets_cidr_blocks" {
  type    = list(string)
  default = ["10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24"]
}

provider "aws" {
  region = var.region

  default_tags {
    tags = {
      Project = "waes-rdc"
    }
  }
}

data "aws_availability_zones" "az" {
}

data "aws_route53_zone" "domain" {
  name = "gabryel.com.br"
}

// VPC
resource "aws_vpc" "rdc" {
  cidr_block = "10.0.0.0/16"

  tags = {
    Name = "RDC"
  }
}

resource "aws_subnet" "rdc" {
  for_each = { for index, value in data.aws_availability_zones.az.names : index => value }

  vpc_id = aws_vpc.rdc.id

  availability_zone = each.value
  cidr_block        = var.subnets_cidr_blocks[each.key]

  tags = {
    Name = "rdc-${each.value}"
  }
}

resource "aws_security_group" "rdc_db" {
  name   = "rdc-db-sg"
  vpc_id = aws_vpc.rdc.id
}

resource "aws_security_group" "rdc_app" {
  name   = "rdc-app-sg"
  vpc_id = aws_vpc.rdc.id

  // I would normally expect a Application Load Balancer, but for simplicity, direct access will be given
  ingress {
    description = "Allows Access from outside"
    cidr_blocks = ["0.0.0.0/0"]

    protocol  = "tcp"
    from_port = 8080
    to_port   = 8080
  }

  ingress {
    description = "Allows Access from outside(SSH)"
    cidr_blocks = ["0.0.0.0/0"]

    protocol  = "tcp"
    from_port = 22
    to_port   = 22
  }

  egress {
    description     = "Allows Access to DB"
    security_groups = [aws_security_group.rdc_db.id]

    protocol  = "tcp"
    from_port = 5432
    to_port   = 5432
  }

  egress {
    description = "Allows Access to outside world"
    cidr_blocks = ["0.0.0.0/0"]

    protocol  = "tcp"
    from_port = 0
    to_port   = 65535
  }
}

resource "aws_security_group_rule" "db_from_app" {
  description = "Allows Access from App"

  type      = "ingress"
  protocol  = "tcp"
  from_port = 5432
  to_port   = 5432

  source_security_group_id = aws_security_group.rdc_app.id
  security_group_id        = aws_security_group.rdc_db.id
}

resource "aws_internet_gateway" "rdc" {
  vpc_id = aws_vpc.rdc.id

  tags = {
    Name = "rdc"
  }
}

resource "aws_route_table" "rdc" {
  vpc_id = aws_vpc.rdc.id

  route {
    gateway_id = aws_internet_gateway.rdc.id
    cidr_block = "0.0.0.0/0"
  }

  tags = {
    Name = "rdc"
  }
}

resource "aws_route_table_association" "subnet" {
  for_each = aws_subnet.rdc

  route_table_id = aws_route_table.rdc.id
  subnet_id      = each.value.id
}

// DB
resource "aws_db_subnet_group" "rdc" {
  subnet_ids = [for k, v in aws_subnet.rdc : v.id]

  tags = {
    Name = "RDC"
  }
}

resource "aws_db_instance" "rdc" {
  db_name        = "rdc"
  engine         = "postgres"
  engine_version = "17"

  instance_class    = "db.t4g.micro"
  allocated_storage = 20

  multi_az               = false
  db_subnet_group_name   = aws_db_subnet_group.rdc.name
  vpc_security_group_ids = [aws_security_group.rdc_db.id]
  availability_zone      = data.aws_availability_zones.az.names[0]

  username = var.db_username
  password = var.db_password

  tags = {
    Name = "RDC"
  }
}

// Server
// Using an EC2 for simplicity, ElasticBeanstalk and EKS both needed uploading files/images and be more costly
resource "aws_instance" "rdc" {
  ami           = "ami-01d488cf20ce2c208" # Ubuntu Server 24.04 AMI for eu-west-3 (Paris), update if needed
  instance_type = "t4g.small"
  user_data = base64encode(
    <<-EOF
      #!/bin/bash

      sudo apt update
      sudo apt -y install openjdk-21-jdk

      git clone https://github.com/Megamiun/waes-rdc.git
      cd waes-rdc

      echo "DB_URL=jdbc:postgresql://${aws_db_instance.rdc.address}:5432/rdc" >> .env
      echo "DB_USERNAME=${var.db_username}" >> .env
      echo "DB_PASSWORD=${var.db_password}" >> .env

      sudo ./gradlew bootRun --no-daemon
    EOF
  )
  user_data_replace_on_change = true

  availability_zone           = data.aws_availability_zones.az.names[0]
  associate_public_ip_address = true

  subnet_id              = aws_subnet.rdc[0].id
  vpc_security_group_ids = [aws_security_group.rdc_app.id]

  tags = {
    Name = "RDC"
  }
}

resource "aws_route53_record" "subdomain" {
  name = "waes-rdc"
  type = "A"

  zone_id = data.aws_route53_zone.domain.zone_id
  records = [aws_instance.rdc.public_ip]
  ttl     = 1200
}
