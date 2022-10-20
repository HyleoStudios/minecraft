package proxy.listeners

import com.google.common.base.Joiner
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.google.common.io.ByteStreams
import com.google.gson.Gson
import com.velocitypowered.api.event.*
import proxy.redis.util.PlayerUtils.createPlayer
import proxy.redis.util.PayloadUtils.playerServerChangePayload
import proxy.redis.util.PlayerUtils.cleanUpPlayer
import proxy.redis.util.Serializations.serializeMultimap
import proxy.redis.util.Serializations.serializeMultiset
import proxy.events.PubSubMessageEvent
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.event.connection.PluginMessageEvent.ForwardResult
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.event.proxy.ProxyPingEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ServerConnection
import com.velocitypowered.api.proxy.server.RegisteredServer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import proxy.VelocityPlugin
import proxy.contexts.*
import proxy.redis.GlobalPayerCount
import proxy.redis.RedisAPI
import proxy.redis.RedisCommandSource
import proxy.redis.config.RedisConfig
import proxy.redis.tasks.RedisTask
import proxy.redis.util.uuid.UUIDTranslator
import redis.clients.jedis.UnifiedJedis
import java.util.*
import java.util.stream.Collectors

context(ProxyContext, PluginContext, JedisClusterContext, RedisDataManagerContext, RedisAPIContext)
class RedisNetworkListener {

    val ALREADY_LOGGED_IN =
        "§cYou are already logged on to this server. \n\nIt may help to try logging in again in a few minutes.\nIf this does not resolve your issue, please contact staff."

    val ONLINE_MODE_RECONNECT =
        "§cYour account was already logged in. The person using your account was kicked and you may now reconnect.\nChange your password if you believe your account has been compromised."

    private val gson = Gson()

    // Some messages are using legacy characters
    private val serializer = LegacyComponentSerializer.legacySection()

    @Subscribe(order = PostOrder.LAST)
    fun onLogin(event: LoginEvent, continuation: Continuation): Unit = with(receiver = event) {
        plugin.executeAsync(object : RedisTask<Unit>() {
            override fun unifiedJedisTask(unifiedJedis: UnifiedJedis): Unit {
                return try {
                    if (!result.isAllowed) return

                    // We make sure they aren't trying to use an existing player's name.
                    // This is problematic for online-mode servers as they always disconnect old clients.
                    if (plugin.isOnlineMode) {
                        val player = plugin.getPlayer(player.username) as Player?
                        if (player != null) {
                            event.result =
                                ResultedEvent.ComponentResult.denied(serializer.deserialize(ONLINE_MODE_RECONNECT))
                            return
                        }
                    }
                    for (s in plugin.proxiesIds) {
                        if (unifiedJedis.sismember("proxy:$s:usersOnline", player.uniqueId.toString())) {
                            result =
                                ResultedEvent.ComponentResult.denied(serializer.deserialize(ALREADY_LOGGED_IN))
                            return
                        }
                    }
                } finally {
                    continuation.resume()
                }
            }
        })
    }

    @Subscribe
    fun onPostLogin(event: PostLoginEvent): Unit = with(receiver = event) {
        plugin.executeAsync(object : RedisTask<Unit>() {
            override fun unifiedJedisTask(unifiedJedis: UnifiedJedis) {
                UUIDTranslator.persistInfo(player.username, player.uniqueId, unifiedJedis!!)
                createPlayer(player, unifiedJedis, true)
            }
        })
    }


    @Subscribe
    fun onPlayerDisconnect(event: DisconnectEvent): Unit = with(receiver = event) {

        plugin.executeAsync { println() }

        plugin.executeAsync(object : RedisTask<Unit>() {
            override fun unifiedJedisTask(unifiedJedis: UnifiedJedis) {
                cleanUpPlayer(player.uniqueId.toString(), unifiedJedis, true)
            }
        })
    }

    @Subscribe
    fun onServerChange(event: ServerConnectedEvent): Unit = with(receiver = event) {

        val currentServer = server.serverInfo.name
        val oldServer: String? = previousServer.map { serverConnection ->
            serverConnection.serverInfo.name
        }.orElse(null)

        plugin.executeAsync(object : RedisTask<Unit>() {
            override fun unifiedJedisTask(unifiedJedis: UnifiedJedis) {
                unifiedJedis.hset("player:${player.uniqueId}", "server", currentServer)
                playerServerChangePayload(player.uniqueId, unifiedJedis, currentServer, oldServer)
            }
        })

    }

    @Subscribe(order = PostOrder.EARLY)
    fun onPing(event: ProxyPingEvent) {
        if (RedisConfig.exemptAddresses.contains(event.connection.remoteAddress.address)) return

        val ping = event.ping.asBuilder()
        ping.onlinePlayers(GlobalPayerCount.get())
        event.ping = ping.build()
    }

    @Subscribe
    fun onPluginMessage(event: PluginMessageEvent): Unit = with(receiver = event) {

        if (event.source !is ServerConnection || plugin.IDENTIFIERS.contains(event.identifier)) return

        result = ForwardResult.handled()

        plugin.executeAsync {
            val `in` = event.dataAsDataStream()
            val subchannel = `in`.readUTF()
            val out = ByteStreams.newDataOutput()
            val type: String
            when (subchannel) {
                "PlayerList" -> {
                    out.writeUTF("PlayerList")
                    var original: Set<UUID?>? = emptySet<UUID>()
                    type = `in`.readUTF()
                    if (type == "ALL") {
                        out.writeUTF("ALL")
                        original = plugin.players
                    } else {
                        try {
                            original = redisAPI.getPlayersOnServer(type)
                        } catch (ignored: IllegalArgumentException) {
                        }
                    }
                    val players = original!!.stream().map { uuid: UUID? ->
                        UUIDTranslator.getNameFromUuid(
                            uuid!!, false
                        )
                    }.collect(Collectors.toSet())
                    out.writeUTF(Joiner.on(',').join(players))
                }

                "PlayerCount" -> {
                    out.writeUTF("PlayerCount")
                    type = `in`.readUTF()
                    if (type == "ALL") {
                        out.writeUTF("ALL")
                        out.writeInt(GlobalPayerCount.get())
                    } else {
                        out.writeUTF(type)
                        try {
                            out.writeInt(redisAPI.getPlayersOnServer(type).size)
                        } catch (e: IllegalArgumentException) {
                            out.writeInt(0)
                        }
                    }
                }

                "LastOnline" -> {
                    val user = `in`.readUTF()
                    out.writeUTF("LastOnline")
                    out.writeUTF(user)
                    out.writeLong(
                        redisAPI.getLastOnline(
                            Objects.requireNonNull(
                                UUIDTranslator.getTranslatedUuid(
                                    user, true
                                )
                            )!!
                        )
                    )
                }

                "ServerPlayers" -> {
                    val type1 = `in`.readUTF()
                    out.writeUTF("ServerPlayers")
                    val multimap = redisAPI.serverToPlayers
                    val includesUsers: Boolean = when (type1) {
                        "COUNT" -> false
                        "PLAYERS" -> true
                        else ->                             // TODO: Should I raise an error?
                            return@executeAsync
                    }
                    out.writeUTF(type1)
                    if (includesUsers) {
                        val human: Multimap<String?, String?> = HashMultimap.create()
                        for ((key, value) in multimap.entries()) {
                            human.put(key, UUIDTranslator.getNameFromUuid(value, false))
                        }
                        serializeMultimap(human, true, out)
                    } else {
                        serializeMultiset(multimap.keys(), out)
                    }
                }

                "Proxy" -> {
                    out.writeUTF("Proxy")
                    out.writeUTF(RedisConfig.proxyId)
                }

                "PlayerProxy" -> {
                    val username = `in`.readUTF()
                    out.writeUTF("PlayerProxy")
                    out.writeUTF(username)
                    redisAPI.getProxy(
                        Objects.requireNonNull(
                            UUIDTranslator.getTranslatedUuid(
                                username, true
                            )
                        )!!
                    )?.let {
                        out.writeUTF(
                            it
                        )
                    }
                }

                else -> return@executeAsync
            }
            (source as ServerConnection).sendPluginMessage(identifier, out.toByteArray())
        }
    }

    @Subscribe
    fun onPubSubMessage(event: PubSubMessageEvent): Unit = with(receiver = event) {

        if (channel != "redisbungee-allservers" && channel != "redisbungee-" + redisAPI.proxyId) return

        val message = if (!message.startsWith(prefix = "/")) {
            message
        } else {
            message.substring(startIndex = 1)
        }

        plugin.logInfo("Invoking command via PubSub: /$message")

        proxy.commandManager.executeAsync(RedisCommandSource, message)

    }

}