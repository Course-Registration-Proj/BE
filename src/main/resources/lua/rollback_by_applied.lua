-- KEYS[1] = appliedKey
-- KEYS[2] = userSetKey
-- KEYS[3] = holdKey
-- ARGV[1] = userId

-- 신청 취소
local inSet = redis.call('SISMEMBER', KEYS[2], ARGV[1])
if inSet == 1 then
    local curStr = redis.call('GET', KEYS[1])
    local cur = tonumber(curStr or "0")
    if cur > 0 then
        redis.call('DECR', KEYS[1])
    end
    redis.call('SREM', KEYS[2], ARGV[1])
end

-- 홀드 제거
redis.call('DEL', KEYS[3])

return "CANCELED"