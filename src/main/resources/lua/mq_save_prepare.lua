-- KEYS: zset@id-prepare hash@data-normal
-- ARGS: id data timestamp

redis.call('ZADD', KEYS[1], ARGV[3], ARGV[1])
redis.call('HSET', KEYS[2], ARGV[1], ARGV[2])

return 0