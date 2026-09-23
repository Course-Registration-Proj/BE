# 결과 출력
output "public_ip" {
  description = "EC2 퍼블릭 IP"
  value       = aws_instance.app.public_ip
}

output "app_url" {
  description = "앱 접속 주소 (기동까지 수 분 소요)"
  value       = "http://${aws_instance.app.public_ip}"
}

output "ssh_command" {
  description = "EC2 SSH 접속 명령"
  value       = "ssh -i ~/.ssh/id_ed25519 ubuntu@${aws_instance.app.public_ip}"
}
