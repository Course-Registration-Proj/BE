# 📚 모의 수강신청 시스템

대학 수강신청 기간의 서버 오류를 직접 재현하고 분석하기 위해 개발한 **Redis 대기열 기반 모의 수강신청 시스템**입니다.
<br>

## 🔗 링크
- **시연 영상**: https://youtu.be/UQwpJS6AIpw
<br>

## 🛠 기술 스택
 
| 분류 | 기술 |
|------|------|
| Backend | Spring Boot, Spring Security |
| Database | MySQL, Redis |
| Infra | AWS EC2, Docker |
| Test | JMeter |

<br>

## 🎯 프로젝트 목적
 
매 학기 반복되는 수강신청 서버 오류의 원인을 직접 분석하고,  
**동시 접속자가 많은 환경에서도 안정적으로 동작하는 수강신청 시스템**을 설계하는 것이 목표입니다.
<br>

## 🏗 시스템 아키텍처
 
```
클라이언트
    ↓ 수강신청 요청
Spring Boot (Enqueue)
    ↓ Redis Sorted Set에 삽입
Redis 대기열
    ↓ 순차적으로 Dequeue + 토큰 발급
Spring Boot (수강신청 처리)
    ↓ Lua 스크립트로 Redis 선점 → 원자적 DB UPDATE
MySQL
```
<br>

## ⚙️ 핵심 구현
 
### Redis 대기열 (Sorted Set)
- 요청이 들어온 시각을 score로 저장하여 **선착순 처리** 보장
- `rank()` 명령어로 현재 대기 순번을 O(log N)에 조회
- 같은 사용자의 **중복 요청 자동 차단**

### 멱등성 보장
- 멱등키로 중복 클릭 방지
- Rate Limit으로 과도한 요청 차단

### 클라이언트 Polling
- 클라이언트가 상태를 요청 (WAITING / ALLOWED / FAIL)
- 대기 순번을 실시간으로 화면에 표시

## 🔍 성능 분석 및 병목 해결
### 문제 상황
800명 동시 접속 부하테스트에서 평균 응답 지연 **6초** 발생, 에러율 **80%** 급증
 
### 원인 분석 과정
 
**1단계 - 로그 기반 분석**
- Redis enqueue 처리 시간이 100ms → 860ms로 급증 확인
- Redis 명령어 자체는 빠르지만 통신 구간에서 병목 추정
 
**2단계 - IntelliJ Profiler 분석**
- Fetch Join 쿼리가 동시 요청 상황에서 평균 **299ms** 차지
- 단일 요청 기준 11ms로 빨라 보였으나, 동시 요청 시 커넥션 경합으로 급증

<br>
 
## 📌 향후 개선 방향
 
- [ ] Enqueue 단계 검증을 Dequeue로 이동하여 DB 부하 최소화
- [ ] Polling → SSE 전환으로 불필요한 요청 제거
- [ ] Spring WebFlux 도입으로 논블로킹 구조 전환
- [ ] Redis 캐싱으로 Enqueue 단계 DB 조회 제거
