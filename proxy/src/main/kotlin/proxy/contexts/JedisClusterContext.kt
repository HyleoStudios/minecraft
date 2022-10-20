package proxy.contexts

import redis.clients.jedis.JedisCluster

data class JedisClusterContext(val jedisCluster: JedisCluster)