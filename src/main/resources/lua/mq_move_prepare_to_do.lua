-- KEYS: zset@id-prepare zset@id-do
-- ARGS: id timestamp

redis.call('ZREM', KEYS[1], ARGV[1])
redis.call('ZADD', KEYS[2], ARGV[2], ARGV[1])

return 0