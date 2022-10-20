package proxy.redis.tasks

import com.velocitypowered.api.plugin.Plugin
import proxy.VelocityPlugin
import proxy.contexts.PluginContext
import proxy.redis.config.RedisConfig
import proxy.redis.util.RedisUtil
import proxy.redis.util.RedisUtil.isRedisVersionRight
import redis.clients.jedis.Protocol
import redis.clients.jedis.UnifiedJedis
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

object InitialUtils {

    context(PluginContext)
    fun checkRedisVersion() {
        object : RedisTask<Unit>() {
            override fun unifiedJedisTask(unifiedJedis: UnifiedJedis) {
                // This is more portable than INFO <section>
                val info = String((unifiedJedis!!.sendCommand(Protocol.Command.INFO) as ByteArray))
                for (s in info.split("\r\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                    if (s.startsWith("redis_version:")) {
                        val version = s.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                        plugin.logInfo("$version <- redis version")
                        if (!isRedisVersionRight(version)) {
                            plugin.logFatal("Your version of Redis ($version) is not at least version 3.0 RedisBungee requires a newer version of Redis.")
                            throw RuntimeException("Unsupported Redis version detected")
                        }
                        val uuidCacheSize = unifiedJedis.hlen("uuid-cache")
                        if (uuidCacheSize > 750000) {
                            plugin.logInfo("Looks like you have a really big UUID cache! Run https://www.spigotmc.org/resources/redisbungeecleaner.8505/ as soon as possible.")
                        }
                        break
                    }
                }
            }
        }.execute()
    }

}