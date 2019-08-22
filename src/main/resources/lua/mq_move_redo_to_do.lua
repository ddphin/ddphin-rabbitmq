-- KEYS: set@id-redo zset@id-do hash@data-normal
-- ARGS: timestamp

local id = redis.call('SPOP', KEYS[1])
if (id) then
    redis.call('ZADD', KEYS[2], ARGV[1], id)
    return redis.call('HGET', KEYS[3], id)
end
return nil