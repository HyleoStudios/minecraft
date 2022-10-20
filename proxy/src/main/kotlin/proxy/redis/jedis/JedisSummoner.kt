package proxy.redis.jedis

import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import proxy.redis.config.GenericPoolConfig
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.JedisCluster

object JedisSummoner :
    JedisCluster(
        mutableSetOf(
            HostAndPort("0.0.0.0", 6001),
            HostAndPort("0.0.0.0", 6002),
            HostAndPort("0.0.0.0", 6003),
        ), 5000, 5000, 60, GenericPoolConfig
    )

