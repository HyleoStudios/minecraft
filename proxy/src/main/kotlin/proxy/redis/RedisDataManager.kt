package proxy.redis

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.net.InetAddresses
import com.google.common.reflect.TypeToken
import com.google.common.util.concurrent.UncheckedExecutionException
import com.google.gson.Gson
import com.google.gson.JsonParser
import proxy.events.PubSubMessageEvent
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import proxy.redis.config.RedisConfig
import proxy.contexts.ProxyContext
import proxy.contexts.PluginContext
import proxy.redis.tasks.RedisTask
import redis.clients.jedis.UnifiedJedis
import java.net.InetAddress
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

context(ProxyContext, PluginContext)
class RedisDataManager {

    @Subscribe
    fun onPostLogin(event: PostLoginEvent) = invalidate(event.player?.uniqueId!!)

    @Subscribe
    fun onPlayerDisconnect(event: DisconnectEvent) = invalidate(event.player.uniqueId!!)

    @Subscribe
    fun onPubSubMessage(event: PubSubMessageEvent) = handlePubSubMessage(event.channel, event.message)

    private val serializer = LegacyComponentSerializer.legacySection()

    fun handleKick(target: UUID, message: String): Boolean {
        val player = plugin.getPlayer(target)
        player?.disconnect(serializer.deserialize(message)) ?: return false
        return true
    }


    private val serverCache = createCache<UUID, String>()
    private val proxyCache = createCache<UUID, String>()
    private val ipCache = createCache<UUID, InetAddress>()
    private val lastOnlineCache = createCache<UUID, Long>()
    private val gson = Gson()

    fun getServer(uuid: UUID): String? {
        val player: Player? = plugin.getPlayer(uuid)
        return if (player != null) if (plugin.isPlayerOnAServer(player)) plugin.getPlayerServerName(
            player
        ) else null else try {
            serverCache[uuid, object : RedisTask<String>() {
                override fun unifiedJedisTask(unifiedJedis: UnifiedJedis): String {
                    return Objects.requireNonNull(unifiedJedis!!.hget("player:$uuid", "server"), "user not found")
                }
            }]
        } catch (e: ExecutionException) {
            if (e.cause is NullPointerException && (e.cause as NullPointerException).message == "user not found") return null // HACK
            plugin.logFatal("Unable to get server")
            throw RuntimeException("Unable to get server for $uuid", e)
        } catch (e: UncheckedExecutionException) {
            if (e.cause is NullPointerException && (e.cause as NullPointerException).message == "user not found") return null
            plugin.logFatal("Unable to get server")
            throw RuntimeException("Unable to get server for $uuid", e)
        }
    }

    fun getProxy(uuid: UUID): String? {
        val player: Player? = plugin.getPlayer(uuid)
        return if (player != null) RedisConfig.proxyId else try {
            proxyCache[uuid, object : RedisTask<String>() {
                override fun unifiedJedisTask(unifiedJedis: UnifiedJedis): String {
                    return Objects.requireNonNull(unifiedJedis!!.hget("player:$uuid", "proxy"), "user not found")
                }
            }]
        } catch (e: ExecutionException) {
            if (e.cause is NullPointerException && (e.cause as NullPointerException).message == "user not found") return null // HACK
            plugin.logFatal("Unable to get proxy")
            throw RuntimeException("Unable to get proxy for $uuid", e)
        } catch (e: UncheckedExecutionException) {
            if (e.cause is NullPointerException && (e.cause as NullPointerException).message == "user not found") return null
            plugin.logFatal("Unable to get proxy")
            throw RuntimeException("Unable to get proxy for $uuid", e)
        }
    }

    fun getIp(uuid: UUID): InetAddress? {
        val player: Player? = plugin.getPlayer(uuid)
        return if (player != null) plugin.getPlayerIp(player) else try {
            ipCache[uuid, object : RedisTask<InetAddress>() {
                override fun unifiedJedisTask(unifiedJedis: UnifiedJedis): InetAddress {
                    val result =
                        unifiedJedis.hget("player:$uuid", "ip") ?: throw NullPointerException("user not found")
                    return InetAddresses.forString(result)
                }
            }]
        } catch (e: ExecutionException) {
            if (e.cause is NullPointerException && (e.cause as NullPointerException).message == "user not found") return null // HACK
            plugin.logFatal("Unable to get IP")
            throw RuntimeException("Unable to get IP for $uuid", e)
        } catch (e: UncheckedExecutionException) {
            if (e.cause is NullPointerException && (e.cause as NullPointerException).message == "user not found") return null
            plugin.logFatal("Unable to get IP")
            throw RuntimeException("Unable to get IP for $uuid", e)
        }
    }

    fun getLastOnline(uuid: UUID): Long {
        val player: Player? = plugin.getPlayer(uuid)
        return if (player != null) 0 else try {
            lastOnlineCache[uuid, object : RedisTask<Long>() {
                override fun unifiedJedisTask(unifiedJedis: UnifiedJedis): Long {
                    val result = unifiedJedis.hget("player:$uuid", "online")
                    return result?.toLong() ?: -1
                }
            }]
        } catch (e: ExecutionException) {
            plugin.logFatal("Unable to get last time online")
            throw RuntimeException("Unable to get last time online for $uuid", e)
        }
    }

    private fun invalidate(uuid: UUID) {
        ipCache.invalidate(uuid)
        lastOnlineCache.invalidate(uuid)
        serverCache.invalidate(uuid)
        proxyCache.invalidate(uuid)
    }


    private fun handlePubSubMessage(channel: String, message: String) {
        if (channel != "redisbungee-data") return

        // Partially deserialize the message so we can look at the action
        val jsonObject = JsonParser.parseString(message).asJsonObject
        val source = jsonObject["source"].asString
        if (source == RedisConfig.proxyId) return
        val action = DataManagerMessage.Action.valueOf(
            jsonObject["action"].asString
        )
        when (action) {
            DataManagerMessage.Action.JOIN -> {
                val message1 = gson.fromJson<DataManagerMessage<LoginPayload>>(
                    jsonObject,
                    object : TypeToken<DataManagerMessage<LoginPayload>>() {}.type
                )
                proxyCache.put(message1.target, message1.source)
                lastOnlineCache.put(message1.target, 0L)
                ipCache.put(message1.target, message1.payload.address)
                plugin.executeAsync {
                    val event = plugin.createPlayerJoinedNetworkEvent(message1.target)
                    plugin.fireEvent(event)
                }
            }

            DataManagerMessage.Action.LEAVE -> {
                val message2 = gson.fromJson<DataManagerMessage<LogoutPayload>>(
                    jsonObject,
                    object : TypeToken<DataManagerMessage<LogoutPayload>>() {}.type
                )
                invalidate(message2.target)
                lastOnlineCache.put(message2.target, message2.payload.timestamp)
                plugin.executeAsync {
                    val event = plugin.createPlayerLeftNetworkEvent(message2.target)
                    plugin.fireEvent(event)
                }
            }

            DataManagerMessage.Action.SERVER_CHANGE -> {
                val message3 = gson.fromJson<DataManagerMessage<ServerChangePayload>>(
                    jsonObject,
                    object : TypeToken<DataManagerMessage<ServerChangePayload>>() {}.type
                )
                serverCache.put(message3.target, message3.payload.server)
                plugin.executeAsync {
                    val event = plugin.createPlayerChangedServerNetworkEvent(
                        message3.target,
                        message3.payload.oldServer,
                        message3.payload.server
                    )
                    plugin.fireEvent(event)
                }
            }

            DataManagerMessage.Action.KICK -> {
                val kickPayload = gson.fromJson<DataManagerMessage<KickPayload>>(
                    jsonObject,
                    object : TypeToken<DataManagerMessage<KickPayload>>() {}.type
                )
                plugin.executeAsync {
                    handleKick(
                        kickPayload.target,
                        kickPayload.payload.message
                    )
                }
            }
        }
    }

    class DataManagerMessage<T : Payload>(
        val target: UUID, val source: String, // for future use!
        val action: Action, val payload: T
    ) {

        enum class Action {
            JOIN, LEAVE, KICK, SERVER_CHANGE
        }
    }

    abstract class Payload

    data class KickPayload(val message: String) : Payload()
    data class LoginPayload(val address: InetAddress) : Payload()
    data class ServerChangePayload(val server: String, val oldServer: String?) : Payload()
    data class LogoutPayload(val timestamp: Long) : Payload()

    private fun <K, V> createCache(): Cache<K, V> {
        // TODO: Allow customization via cache specification, ala ServerListPlus
        return CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build()
    }


}