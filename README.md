## 성능 최적화 과정 (2026-01-29까지)

### 1. 문제 정의
- 수강신청 대기열 진입(`/apply`) API의 Average Latency가 8,214ms, TPS 70으로 측정됨.
- 사용자 수(800명) 대비 비정상적으로 느린 속도로 인해 병목 지점을 파악하기로 함.

-----
### 2. 가설 검증

**가설1) DB 조회가 병목일 것이다.❌**

조치)
- Member/Subject 중복 조회 최적화
- JPA N+1 문제 발생으로 Join fetch 도입
- DB 조회하는 부분을 비활성화하고 부하테스트 진행

결과)
- DB 쿼리 시간: 11ms 로 JPA 개선 시도 전후 똑같음
- DB 조회를 비활성화하더라도 전체 응답 시간도 여전히 8초대
- DB는 병목이 아님을 확인

-----

**가설2) DB의 Connection Pool이 부족할 것이다.❌**

조치)
- 최범균님의 유튜브를 통해서 목표 TPS와 쿼리 시간으 바탕으로 필요 Connection을 계산함

결과)
- 목표 TPS: 100, 쿼리시간 11ms일 때, 필요한 Conenction Pool은 100 * 0.011 = 1.1개로, HikariCP 기본값으로도 충분히 도달할 수 있는 TPS임을 확인
- 실제로 DB Connection time out 은 발생한 적 없기 때문에 DB Connection Pool의 문제가 아님

-----
**가설 3) 대기열 토큰 발급 스케줄러가 계속해서 Redis를 호출하기 때문에 병목이 발생한 것이다.❌**

조치)
- 토큰 발급 스케줄러 비활성화

결과)
- TPS, Latency 모두 변화 없음
- Scheduler는 병목이 아님.
-----

**가설 4) CPU Context Switching에서 병목이 발생한 것이다.❌**

조치)
- t2.micro(vCPU 1개, 메모리 1024MB) 에서 t3.medium(vCPU 2개, 메모리 4096MB)로 확장함.

결과)
- 부하테스트 시 `htop`로 확인했을 때, CPU 사용량이 30%로 t2.micro보다 여유로워진 걸 확인했으나, 어떠한 성능 개선도 없었음.
- CPU 는 병목이 아님.

-----

**가설 5) JMeter 클라이언트(로컬 PC)가 1초에 70개 이상 요청을 보내지 못하는 것 아닌가?❌**

조치)
- 부하 수준을 800명에서 100명으로 축소함

결과)
- TPS가 239/sec가 나오는 것으로 확인함.
- 클라이언트는 병목이 아님
  
-----
**가설 6) Redis 명령어의 실행 속도가 느릴 것이다. ❌**

조치)
- `redis-cli slowlog get 10` 을 확인함

결과)
- `empty array`로 Redis의 명령어 실행 시간이 문제가 아님을 확인
-----

**가설 7) Redis Conenction pool 이 부족할 것이다❌**

조치)
- lettuce 의 pool을 200개로 늘려봄

결과)
- 개선 X

-----
**가설 8) Redis를 가져오고 실행하고 다시 반납하는 데에 시간이 오래걸릴 것이다.⚠️**

조치)
-globalEnque에서 `redisTemplate.opsForZSet().add(queueKey, value, nowMillis);` 전후로 시간을 측정함

결과)
- 1ms에서 100ms까지 부하테스트 도중에 치솟는 것을 확인함. 원인을 아직 밝히지 못함.
-----
가설 9: Tomcat 스레드 풀 부족 → 스레드 800으로 늘려도 1000ms 지속 → 아님
가설 10: Kafka 연결 시도 타임아웃 → 의존성 제거해도 동일 → 아님
현재 상태: enqueueCourseRequest 단계별 시간 측정 필요, Pipeline 적용 검토 중
