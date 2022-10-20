package proxy.redis.tasks

import proxy.contexts.JedisClusterContext
import proxy.contexts.LoggerContext
import proxy.contexts.PluginContext
import proxy.contexts.RedisAPIContext
import proxy.redis.GlobalPayerCount
import proxy.redis.config.RedisConfig
import redis.clients.jedis.exceptions.JedisConnectionException

context(PluginContext, LoggerContext, JedisClusterContext, RedisAPIContext)
class HeartbeatTask : Runnable {

    override fun run() {
        try {
            val redisTime = plugin.getRedisTime()
            jedisCluster.hset(
                "heartbeats",
                RedisConfig.proxyId,
                redisTime.toString()
            )
        } catch (e: JedisConnectionException) {
            // Redis server has disappeared!
            plugin.logFatal("Unable to update heartbeat - did your Redis server go away?")
            e.printStackTrace()
        }
        try {
            plugin.updateProxiesIds()
            GlobalPayerCount.set(plugin.currentCount.toInt())
        } catch (e: Throwable) {
           plugin.logFatal("Unable to update data - did your Redis server go away?")
            e.printStackTrace()
        }
    }
}