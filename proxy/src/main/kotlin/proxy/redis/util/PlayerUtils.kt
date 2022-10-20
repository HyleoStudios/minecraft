package proxy.redis.util

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ServerConnection
import proxy.contexts.RedisAPIContext
import proxy.redis.RedisAPI
import redis.clients.jedis.UnifiedJedis

object PlayerUtils {

    context (RedisAPIContext)
    fun createPlayer(player: Player, unifiedJedis: UnifiedJedis, fireEvent: Boolean) {
        val server = player.currentServer

        server.ifPresent { serverConnection: ServerConnection ->
            unifiedJedis.hset(
                "player:" + player.uniqueId.toString(),
                "server",
                serverConnection.serverInfo.name
            )
        }

        val playerData: MutableMap<String, String> = HashMap(4)

        playerData["online"] = "0"
        playerData["ip"] = player.remoteAddress.hostName
        playerData["proxy"] = redisAPI.proxyId
        unifiedJedis.sadd("proxy:" + redisAPI.proxyId + ":usersOnline", player.uniqueId.toString())
        unifiedJedis.hmset("player:" + player.uniqueId.toString(), playerData)

        if (fireEvent) {
            PayloadUtils.playerJoinPayload(player.uniqueId, unifiedJedis, player.remoteAddress.address)
        }
    }


    context (RedisAPIContext)
    fun cleanUpPlayer(uuid: String, rsc: UnifiedJedis, firePayload: Boolean) {
        rsc.srem("proxy:" + redisAPI.proxyId + ":usersOnline", uuid)
        rsc.hdel("player:$uuid", "server", "ip", "proxy")
        val timestamp = System.currentTimeMillis()
        rsc.hset("player:$uuid", "online", timestamp.toString())
        if (firePayload) {
            PayloadUtils.playerQuitPayload(uuid, rsc, timestamp)
        }
    }
}