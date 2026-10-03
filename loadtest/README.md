# 부하테스트 (k6)

수강신청 시스템의 한계 처리량과 정원 정합성을 측정한다.

## 사전 조건
1. 앱이 **loadtest 프로파일**로 떠 있어야 함 (userId 헤더 인증)
   - EC2 `.env`에 `SPRING_PROFILES_ACTIVE=prod,loadtest` 설정 후 재배포
2. 시드 데이터 생성됨 (회원 user1~user2500, 과목 SUBJ001~SUBJ030)
3. k6 설치 (`k6 version`)
4. ⚠️ 정확한 측정은 **같은 리전(ap-northeast-2)의 부하생성용 EC2**에서 실행 (로컬은 대역폭 왜곡)

## 시나리오

### 1. 처리량 baseline (`throughput-baseline.js`)
30과목에 분산, VU 100→500→1000→2000 램프 → 한계 처리량(무릎) 탐색
```bash
k6 run -e BASE_URL=http://<EC2-IP> loadtest/throughput-baseline.js
```

### 2. 동시성 경합 (`capacity-contention.js`)
인기과목에 집중 → 정원 초과/중복 차단 검증. 핫과목 1개/2개 각각 실행.
```bash
# 핫과목 1개 (최대 경합)
k6 run -e BASE_URL=http://<EC2-IP> -e HOT_SUBJECTS=1 -e VUS=300 loadtest/capacity-contention.js
# 핫과목 2개
k6 run -e BASE_URL=http://<EC2-IP> -e HOT_SUBJECTS=2 -e VUS=300 loadtest/capacity-contention.js
```
> 경합 테스트는 **매 실행 전 DB를 깨끗한 상태**(destroy→apply)로 두어야 정원 검증이 정확함.

## 측정 지표 (k6 외)
부하 도는 동안 별도 터미널에서:
```bash
# CPU/메모리 (컨테이너별)
ssh -i ~/.ssh/id_ed25519 ubuntu@<EC2-IP> "docker stats"

# 스레드풀 / 커넥션풀 (1초마다 샘플링)
while true; do
  curl -s http://<EC2-IP>/actuator/prometheus | grep -E 'hikaricp_connections_active|tomcat_threads_busy|process_cpu_usage'
  sleep 1
done
```

## 검증 포인트 (경합 테스트 후)
- `enroll_success` == `30 * HOT_SUBJECTS` (정원만큼만 성공)
- 각 과목 `registeredNum <= limitedNum(30)` → **정원 초과 0**
- 같은 유저 중복 성공 0

## 실패 원인 추적 (중요)
`http_req_failed`는 5xx/네트워크만 집계(비즈니스 400 제외)하고,
요약에 **5xx가 어느 단계에서 났는지**(apply/try/confirm)가 표시된다:
```
실제 실패율(5xx)   : 11.12 %
└ 5xx 발생 단계    : apply=0 / try=0 / confirm=25156
```
→ "어디서" 터졌는지 확인한 뒤, **서버 로그로 "왜"를 확정**한다 (ground truth):
```bash
ssh -i ~/.ssh/id_ed25519 ubuntu@<EC2-IP> \
  "sudo docker logs springboot-app 2>&1 | grep -iE 'exception|timeout|HikariPool|Connection is not available' | tail -40"
```
예: `HikariPool-1 - Connection is not available, request timed out` →
"confirm 단계 5xx = 커넥션풀 고갈로 인한 타임아웃(500)" 으로 원인 확정.

## 시계열(무릎 지점) 분석
램프 중 어느 VU에서 깨지는지 보려면 시계열 출력 + 라이브 모니터링:
```bash
# k6 시계열 저장
k6 run --out csv=loadtest/results/ts.csv -e BASE_URL=http://<IP> loadtest/throughput-baseline.js
# (동시에 다른 터미널) 커넥션풀 pending 실시간
while true; do clear; date +%T; curl -s http://<IP>/actuator/prometheus | grep -E "^hikaricp_connections_(active|pending|max)"; sleep 1; done
```
