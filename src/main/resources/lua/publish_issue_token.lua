-- KEYS[1] = "apply:queue"
-- ARGV[1] = permits, ARGV[2] = "apply:token:", ARGV[3] = tokenTtlSec

local permits = tonumber(ARGV[1])
local tokenPrefix = ARGV[2]
local ttl = tonumber(ARGV[3])

local members = {}
if permits > 0 then
  members = redis.call('ZRANGE', KEYS[1], 0, permits - 1)
end
if #members == 0 then
  return 0
end

redis.call('ZREM', KEYS[1], unpack(members))

for i = 1, #members do
  local val = members[i]                 -- "memberId:subjectId"
  local delim = string.find(val, ":", 1, true)
  if delim ~= nil then
    local memberId = string.sub(val, 1, delim - 1)
    local subjectId = string.sub(val, delim + 1)
    local tokenKey = tokenPrefix .. memberId
    redis.call('SET', tokenKey, subjectId, "EX", ttl, "NX")
  end
end

return #members