package proxy.redis.config

import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import redis.clients.jedis.Connection

object GenericPoolConfig : GenericObjectPoolConfig<Connection>() {
    init {
        maxTotal = 10
        blockWhenExhausted = true
    }
}