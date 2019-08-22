-- KEYS: zset@id-do set@id-redo
-- ARGS: id

redis.call('ZREM', KEYS[1], ARGV[1])
redis.call('SADD', KEYS[2], ARGV[1])

return 0