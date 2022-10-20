package proxy.redis.jedis

import proxy.VelocityPlugin
import proxy.contexts.JedisClusterContext
import proxy.contexts.JedisPubSubHandlerContext
import proxy.contexts.PluginContext
import proxy.redis.config.RedisConfig
import proxy.redis.tasks.RedisTask
import redis.clients.jedis.UnifiedJedis
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

context (PluginContext, JedisClusterContext, JedisPubSubHandlerContext)
class JedisListener : Runnable {

    private val addedChannels: MutableSet<String> = HashSet()

    override fun run() {
        try {
            addedChannels.add("redisbungee-" + RedisConfig.proxyId)
            addedChannels.add("redisbungee-allservers")
            addedChannels.add("redisbungee-data")
            jedisCluster.subscribe(jedisPubSubHandler, *addedChannels.toTypedArray())
        } catch (e: Exception) {
            plugin.logWarn("PubSub error, attempting to recover in 5 secs.")
            plugin.executeAsyncAfter(this, TimeUnit.SECONDS, 5)
        }
    }

    fun addChannel(vararg channel: String) {
        addedChannels.addAll(listOf(*channel))
        jedisPubSubHandler.subscribe(*channel)
    }

    fun removeChannel(vararg channel: String) {
        listOf(*channel).forEach(Consumer { o: String -> addedChannels.remove(o) })
        jedisPubSubHandler.unsubscribe(*channel)
    }

    fun poison() {
        addedChannels.clear()
        jedisPubSubHandler.unsubscribe()
    }
}