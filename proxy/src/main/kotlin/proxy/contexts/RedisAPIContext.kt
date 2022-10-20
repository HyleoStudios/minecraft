package proxy.contexts

import proxy.redis.RedisAPI

data class RedisAPIContext(val redisAPI: RedisAPI)