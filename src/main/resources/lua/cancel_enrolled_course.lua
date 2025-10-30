-- KEYS[1] = appliedKey      ("course:{id}:applied")
-- KEYS[2] = userSetKey      ("course:{id}:users")
-- KEYS[3] = holdKey         ("reservation:{id}:{userId}") -- 있을 수도, 없을 수도
-- ARGV[1] = userId

local inSet = redis.call('SISMEMBER', KEYS[2], ARGV[1])
if inSet == 0 then
    return "NOT_ENROLLED"
end

local curStr = redis.call('GET', KEYS[1])
local cur = tonumber(curStr or "0")
if cur > 0 then
    redis.call('DECR', KEYS[1])

    if newCount <= 0 then
        redis.call('DEL', KEYS[1])
    end
end

redis.call('SREM', KEYS[2], ARGV[1])
redis.call('DEL', KEYS[3])
return "CANCELED"