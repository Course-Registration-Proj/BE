# 🚀 배포 가이드 (단일 EC2 + Docker + Terraform)

> 리전: **ap-northeast-2 (서울)** / 구조: 단일 EC2 안에 `app` + `mysql` + `redis` 컨테이너
> 운영 방식: **테스트할 때만 `terraform apply`로 올리고, 끝나면 `terraform destroy`로 내림** (임시 환경)
>
> 최종 목표: 단일 서버로 배포하되 scale-out(다중 서버) 성능 차이를 측정할 수 있게 만드는 것.
> 이 문서는 첫 배포 기준 체크리스트다.

---

## 아키텍처

```
        EC2 (t3.medium, Ubuntu, ap-northeast-2)
   ┌──────────────────────────────────────────┐
   :80  app (Spring Boot)
          ├── mysql   (컨테이너, EBS 볼륨)
          └── redis   (컨테이너)
   └──────────────────────────────────────────┘
   Terraform로 프로비저닝 · user-data로 부트스트랩
```

---

## Phase 0 — 사전 준비 (코드 + 계정)

배포 3단계 전에 반드시 끝내야 하는 준비.

### 코드
- [ ] `docker-compose.prod.yml`에 **MySQL 컨테이너 추가** + `app`이 `DB_HOST=mysql`로 연결
- [ ] **앱 이미지 재빌드 & Docker Hub push** — ⚠️ 최신 리팩터링 반영본으로 (기존 이미지는 옛 코드)
      ```bash
      ./gradlew clean bootJar
      docker build -t <DOCKER_USERNAME>/docker-springboot:latest .
      docker push <DOCKER_USERNAME>/docker-springboot:latest
      ```
- [ ] **시드 데이터** — ⚠️ 과목/회원이 없으면 부하테스트에서 신청할 대상이 없음
      → `DataInitializer`(CommandLineRunner)로 과목 + 테스트 회원 N명 생성 (BCrypt 해시 처리)
- [ ] `.env` 작성 (DB 비번 등) + `.gitignore` 확인 (커밋 금지)

### 계정 / 도구
- [ ] AWS 계정, **IAM 사용자 + 액세스키** 발급 (Terraform용, 최소권한)
- [ ] 로컬에 **AWS CLI** 설치 + `aws configure` (region: `ap-northeast-2`)
- [ ] 로컬에 **Terraform** 설치
- [ ] **SSH 키페어** 생성 (`ssh-keygen -t ed25519`)

---

## Phase 1 — 프로비저닝 (Terraform)

코드로 AWS 리소스를 정의하고 `apply`.

- [ ] Terraform 프로젝트 구조
  - `provider.tf` — AWS provider, `region = "ap-northeast-2"`
  - `main.tf` — 리소스 정의
  - `variables.tf` / `terraform.tfvars`(**gitignore**) — 변수·비밀값
  - `outputs.tf` — 완료 후 퍼블릭 IP 출력
- [ ] 리소스
  - 기본 VPC/서브넷 (`data`로 참조, 새로 안 만듦)
  - **Security Group**: 22(SSH, 내 IP만), 80(HTTP, 전체)
  - **Key Pair** (Phase 0 공개키 등록)
  - **EC2**: `t3.medium`, Ubuntu 22.04 (AMI는 `data`로 최신 조회)
  - (선택) **Elastic IP** — 재시작 시 IP 고정 (안 쓰면 생략)
- [ ] `terraform init` → `plan` → `apply` → 퍼블릭 IP 획득

---

## Phase 2 — 부트스트랩 (user-data)

EC2가 부팅하며 자동 실행하는 스크립트.

- [ ] `user-data.sh`
  - Docker + docker-compose plugin 설치
  - 앱 파일 배치: `docker-compose.prod.yml`, `.env` (git clone 또는 scp)
  - `.env` 비밀값 주입 (학습 단계는 scp, 이후 SSM Parameter Store 권장)
- [ ] lua 등 리소스는 jar(classpath)에 포함되는지 확인 → 포함되면 별도 전송 불필요

---

## Phase 3 — 배포 & 검증

- [ ] `docker compose -f docker-compose.prod.yml up -d`
- [ ] MySQL 스키마: `ddl-auto: update`가 테이블 자동 생성 → 시드로 과목 데이터 확인
- [ ] **스모크 테스트**: `http://<IP>` 접속 → 로그인 → 수강신청 플로우 1회 (리팩터링 후 첫 실동작 검증)
- [ ] 로그 확인: `docker compose logs -f`

---

## Phase 4 — 운영 (올리기/내리기)

- [ ] 내릴 때: `terraform destroy` → 과금 리소스 0
- [ ] ⚠️ destroy 시 MySQL 컨테이너 데이터도 삭제됨 → 부하테스트는 매번 깨끗한 상태가 좋고, 시더가 다시 채움
- [ ] 다시 올릴 때: `terraform apply`

---

## 나중에 (scale-out 실험 단계)

- [ ] 세션 외부화: `spring-session-data-redis` (다중 인스턴스 로그인 유지)
- [ ] nginx + app replica 다중화, 이후 EC2 다중 + ALB
- [ ] 스케줄러 중복 실행 문제 발견 → `ShedLock`으로 단일 인스턴스 실행 보장
- [ ] 단일 vs 다중 서버 부하테스트(k6) 비교
