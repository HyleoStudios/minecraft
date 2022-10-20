package proxy.contexts

import proxy.redis.jedis.JedisListener

data class JedisListenerContext(val jedisListener: JedisListener)