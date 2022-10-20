package proxy.redis.config

import redis.clients.jedis.JedisPoolConfig

object PoolConfig : JedisPoolConfig() {

    init {
        maxTotal = 10
        blockWhenExhausted = true
        testOnBorrow = true
    }

}