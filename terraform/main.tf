# 현재 공인 IP 자동 감지 (SSH 접근을 내 IP로만 제한하기 위함)
data "http" "myip" {
  url = "https://checkip.amazonaws.com"
}

locals {
  my_cidr = var.ssh_ingress_cidr != "" ? var.ssh_ingress_cidr : "${chomp(data.http.myip.response_body)}/32"

  # EC2에 전달할 .env 내용
  env_content = <<-EOT
    DOCKER_USERNAME=${var.docker_username}
    MYSQL_ROOT_PASSWORD=${var.mysql_root_password}
    DB_NAME=${var.db_name}
    DB_USERNAME=${var.db_username}
    DB_PASSWORD=${var.db_password}
  EOT
}

# ---- 기본 VPC / 서브넷 (새로 만들지 않고 조회) ----
data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

# ---- Ubuntu 22.04 최신 AMI (Canonical) ----
data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"]

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }
  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# ---- SSH 키페어 등록 (기존 id_ed25519.pub 재사용) ----
resource "aws_key_pair" "this" {
  key_name   = var.key_name
  public_key = file(pathexpand(var.public_key_path))
}

# ---- 보안 그룹: 22(내 IP만), 80(전체) ----
resource "aws_security_group" "app" {
  name        = "${var.project_name}-sg"
  description = "course-registration app security group"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "SSH (my ip only)"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [local.my_cidr]
  }

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.project_name}-sg" }
}

# ---- EC2 인스턴스 ----
resource "aws_instance" "app" {
  ami                         = data.aws_ami.ubuntu.id
  instance_type               = var.instance_type
  key_name                    = aws_key_pair.this.key_name
  subnet_id                   = data.aws_subnets.default.ids[0]
  vpc_security_group_ids      = [aws_security_group.app.id]
  associate_public_ip_address = true

  user_data = templatefile("${path.module}/user_data.sh.tftpl", {
    compose_b64 = base64encode(file("${path.module}/../docker-compose.prod.yml"))
    env_b64     = base64encode(local.env_content)
  })

  root_block_device {
    volume_size = 30
    volume_type = "gp3"
  }

  tags = { Name = "${var.project_name}-app" }
}
