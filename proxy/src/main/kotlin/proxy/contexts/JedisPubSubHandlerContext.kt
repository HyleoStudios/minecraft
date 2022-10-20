package proxy.contexts

import proxy.redis.jedis.JedisPubSubHandler

data class JedisPubSubHandlerContext(val jedisPubSubHandler: JedisPubSubHandler)