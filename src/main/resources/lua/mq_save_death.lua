-- KEYS: hash@data-death
-- ARGS: id data

redis.call('HSET', KEYS[1], ARGV[1], ARGV[2])

return 0