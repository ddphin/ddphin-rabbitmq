-- KEYS: zset@id-prepare hash@data-normal hash@data-death
-- ARGS: timestamp

local data = redis.call('ZRANGEBYSCORE', KEYS[1], 0, ARGV[1])
if (data) then
    local msg
	for k, id in pairs(data) do
		redis.call('ZREM', KEYS[1], id)
        msg = redis.call('HGET', KEYS[2], id)
        if (msg) then
            redis.call('HDEL', KEYS[2], id)
            redis.call('HSET', KEYS[3], id, msg)
        end
	end
end

return 0