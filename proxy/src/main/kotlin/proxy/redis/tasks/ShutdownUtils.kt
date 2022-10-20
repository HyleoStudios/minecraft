package proxy.redis.tasks

import proxy.contexts.JedisClusterContext
import proxy.contexts.RedisAPIContext
import proxy.redis.config.RedisConfig
import proxy.redis.util.PlayerUtils.cleanUpPlayer
import redis.clients.jedis.JedisCluster
import redis.clients.jedis.UnifiedJedis

object ShutdownUtils {

    context(JedisClusterContext, RedisAPIContext)
    fun shutdownCleanup() {
        jedisCluster.hdel("heartbeats", RedisConfig.proxyId)
        if (jedisCluster.scard("proxy:${RedisConfig.proxyId}:usersOnline") <= 0) return

        val players = jedisCluster.smembers("proxy:" + RedisConfig.proxyId + ":usersOnline")
        players.forEach { cleanUpPlayer(it, jedisCluster, true) }

    }

}