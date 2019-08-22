-- KEYS: zset@id-prepare hash@data-normal
-- ARGS: id

redis.call('ZREM', KEYS[1], ARGV[1])
redis.call('HDEL', KEYS[2], ARGV[1])

return 0