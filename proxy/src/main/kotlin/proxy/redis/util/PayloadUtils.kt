package proxy.redis.util

import com.google.gson.Gson
import proxy.contexts.RedisAPIContext
import proxy.redis.RedisDataManager
import proxy.redis.RedisAPI
import redis.clients.jedis.UnifiedJedis
import java.net.InetAddress
import java.util.*

object PayloadUtils {
    private val gson = Gson()


    context (RedisAPIContext)
    fun playerJoinPayload(uuid: UUID, unifiedJedis: UnifiedJedis, inetAddress: InetAddress?) {
        unifiedJedis.publish(
            "redisbungee-data", gson.toJson(
                RedisDataManager.DataManagerMessage(
                    uuid, redisAPI.proxyId, RedisDataManager.DataManagerMessage.Action.JOIN,
                    RedisDataManager.LoginPayload(inetAddress!!)
                )
            )
        )
    }

    context (RedisAPIContext)
    fun playerQuitPayload(uuid: String, unifiedJedis: UnifiedJedis, timestamp: Long) {
        unifiedJedis.publish(
            "redisbungee-data", gson.toJson(
                RedisDataManager.DataManagerMessage(
                    UUID.fromString(uuid), redisAPI.proxyId, RedisDataManager.DataManagerMessage.Action.LEAVE,
                    RedisDataManager.LogoutPayload(timestamp)
                )
            )
        )
    }


    context (RedisAPIContext)
    fun playerServerChangePayload(uuid: UUID, unifiedJedis: UnifiedJedis, newServer: String, oldServer: String?) {
        unifiedJedis.publish(
            "redisbungee-data", gson.toJson(
                RedisDataManager.DataManagerMessage(
                    uuid, redisAPI.proxyId, RedisDataManager.DataManagerMessage.Action.SERVER_CHANGE,
                    RedisDataManager.ServerChangePayload(newServer, oldServer)
                )
            )
        )
    }

    context (RedisAPIContext)
    fun kickPlayerPayload(uuid: UUID, message: String, unifiedJedis: UnifiedJedis) {
        unifiedJedis.publish(
            "redisbungee-data", gson.toJson(
                RedisDataManager.DataManagerMessage(
                    uuid, redisAPI.proxyId, RedisDataManager.DataManagerMessage.Action.KICK,
                    RedisDataManager.KickPayload(message)
                )
            )
        )
    }
}