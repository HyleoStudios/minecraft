package proxy.redis.tasks


import proxy.contexts.JedisClusterContext
import proxy.contexts.ProxyContext
import proxy.contexts.PluginContext
import proxy.contexts.RedisAPIContext
import proxy.redis.config.RedisConfig
import proxy.redis.util.PlayerUtils
import proxy.redis.util.PlayerUtils.cleanUpPlayer
import redis.clients.jedis.UnifiedJedis
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashSet

context (ProxyContext, PluginContext, JedisClusterContext, RedisAPIContext)
class IntegrityCheckTask() : RedisTask<Unit>() {


    override fun unifiedJedisTask(unifiedJedis: UnifiedJedis) {
        try {
            val players = plugin.localPlayersAsUuidStrings
            val playersInRedis =
                unifiedJedis.smembers("proxy:" + RedisConfig.proxyId + ":usersOnline")
            val lagged = plugin.getCurrentProxiesIds(true)

            // Clean up lagged players.
            for (s in lagged) {
                val laggedPlayers = unifiedJedis.smembers("proxy:$s:usersOnline")

                unifiedJedis.del("proxy:$s:usersOnline")

                if (laggedPlayers.isEmpty()) {
                    continue
                }

               plugin.logInfo("Cleaning up lagged proxy " + s + " (" + laggedPlayers.size + " players)...")

                laggedPlayers.forEach { cleanUpPlayer(it, unifiedJedis, true) }


            }
            val absentLocally: MutableSet<String> = HashSet(playersInRedis)
            absentLocally.removeAll(players)
            val absentInRedis: MutableSet<String> = HashSet(players)
            absentInRedis.removeAll(playersInRedis)
            for (member in absentLocally) {
                var found = false

                for (proxyId in plugin.proxiesIds) {

                    if (proxyId == RedisConfig.proxyId) continue

                    if (unifiedJedis.sismember("proxy:$proxyId:usersOnline", member)) {
                        // Just clean up the set.
                        found = true
                        break
                    }
                }
                if (!found) {
                    cleanUpPlayer(member!!, unifiedJedis, false)
                    plugin.logWarn("Player found in set that was not found locally and globally: $member")
                } else {
                    unifiedJedis.srem(
                        "proxy:" + RedisConfig.proxyId + ":usersOnline",
                        member
                    )
                    plugin.logWarn("Player found in set that was not found locally, but is on another proxy: $member")
                }
            }
            // due unifiedJedis does not support pipelined.
            //Pipeline pipeline = jedis.pipelined();
            for (player in absentInRedis) {
                // Player not online according to Redis but not BungeeCord.
                handlePlatformPlayer(player, unifiedJedis)
            }
        } catch (e: Throwable) {
            plugin.logFatal("Unable to fix up stored player data")
            e.printStackTrace()
        }
    }

    fun handlePlatformPlayer(player: String, unifiedJedis: UnifiedJedis) {
        val playerProxied = proxy.getPlayer(UUID.fromString(player)).orElse(null) ?: return
        // We'll deal with it later.
        PlayerUtils.createPlayer(playerProxied, unifiedJedis, false)
    }


}