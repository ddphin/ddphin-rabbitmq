-- KEYS: zset@id-do set@id-redo
-- ARGS: timestamp

local data = redis.call('ZRANGEBYSCORE', KEYS[1], 0, ARGV[1])
if (data) then
	for k, v in pairs(data) do
		redis.call('ZREM', KEYS[1], v)
		redis.call('SADD', KEYS[2], v)
	end
end

return 0