-- KEYS[1] = appliedKey      ("course:{id}:applied")
-- KEYS[2] = userSetKey      ("course:{id}:users")
-- KEYS[3] = holdKey         ("reservation:{id}:{userId}")
-- ARGV[1] = userId
-- ARGV[2] = limit 인원
-- ARGV[3] = holdTtlSeconds

-- 이미 신청한 과목인지 체크
-- SISMEMBER : Set 안에 주어진 member가 있는지 확인하는 명령
local already = redis.call('SISMEMBER', KEYS[2], ARGV[1])
if already == 1 then
    return "ALREADY_ENROLLED"
end

-- 현재 신청 인원수와 limit 인원 수 비교
local appliedStr = redis.call('GET', KEYS[1])
local applied = tonumber(appliedStr or "0")
local limit = tonumber(ARGV[2])

if (applied >= limit) then
    return "CAPACITY_FULL"
end

-- 원자적으로 신청 수 증가, 사용자 집합 추가, 임시 예약 홀드
redis.call('INCR', KEYS[1])
redis.call('SADD', KEYS[2], ARGV[1])
redis.call('SET', KEYS[3], "PENDING")
redis.call('EXPIRE', KEYS[3], tonumber(ARGV[3]))

return "OK"

