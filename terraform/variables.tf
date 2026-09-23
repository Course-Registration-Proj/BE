# 입력값 정의 (변수로 분리)

variable "aws_region" {
  description = "AWS 리전"
  type        = string
  default     = "ap-northeast-2"
}

variable "project_name" {
  description = "리소스 이름 접두사"
  type        = string
  default     = "course-registration"
}

variable "instance_type" {
  description = "EC2 인스턴스 타입"
  type        = string
  default     = "t3.medium"
}

variable "key_name" {
  description = "EC2 키페어 이름"
  type        = string
  default     = "course-registration-key"
}

variable "public_key_path" {
  description = "EC2에 등록할 SSH 공개키 경로"
  type        = string
  default     = "~/.ssh/id_ed25519.pub"
}

variable "ssh_ingress_cidr" {
  description = "SSH(22) 허용 CIDR. 비워두면 현재 공인 IP를 자동 감지해 /32로 제한"
  type        = string
  default     = ""
}

# ---- 앱/DB 주입값 (.env 로 EC2에 전달) ----
variable "docker_username" {
  description = "Docker Hub 사용자명 (이미지: <username>/docker-springboot:latest)"
  type        = string
}

variable "db_name" {
  description = "MySQL 데이터베이스명"
  type        = string
  default     = "course_register"
}

variable "db_username" {
  description = "MySQL 앱 사용자명"
  type        = string
  default     = "course"
}

variable "mysql_root_password" {
  description = "MySQL root 비밀번호"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "MySQL 앱 사용자 비밀번호"
  type        = string
  sensitive   = true
}
