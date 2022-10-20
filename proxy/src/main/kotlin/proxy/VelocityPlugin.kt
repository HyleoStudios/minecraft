package proxy

import com.google.common.base.Preconditions
import com.google.common.cache.CacheBuilder
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Multimap
import com.google.inject.Inject
import com.velocitypowered.api.command.Command
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.ServerConnection
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import com.velocitypowered.api.scheduler.ScheduledTask
import org.slf4j.Logger
import org.springframework.boot.SpringApplication
import proxy.redis.tasks.HeartbeatTask
import proxy.redis.tasks.IntegrityCheckTask
import proxy.redis.tasks.ShutdownUtils
import proxy.redis.util.PayloadUtils
import proxy.redis.util.RedisUtil
import proxy.redis.util.uuid.UUIDTranslator
import proxy.commands.PlayCommand
import proxy.commands.FindCommand
import proxy.commands.IpCommand
import proxy.commands.LastSeenCommand
import proxy.contexts.*
import proxy.events.PlayerChangedServerNetworkEvent
import proxy.events.PlayerJoinedNetworkEvent
import proxy.events.PlayerLeftNetworkEvent
import proxy.events.PubSubMessageEvent
import proxy.listeners.ChooseInitialServerListener
import proxy.redis.MojangHttpClient
import proxy.redis.RedisAPI
import proxy.redis.RedisDataManager
import proxy.listeners.RedisNetworkListener
import proxy.redis.config.GenericPoolConfig
import proxy.redis.config.RedisConfig
import proxy.redis.jedis.JedisListener
import proxy.redis.jedis.JedisPubSubHandler
import proxy.redis.jedis.JedisSummoner
import proxy.redis.tasks.InitialUtils.checkRedisVersion
import proxy.redis.tasks.RedisTask
import proxy.restful.RestfulAPI
import redis.clients.jedis.*
import redis.clients.jedis.exceptions.JedisConnectionException
import java.io.IOException
import java.net.InetAddress
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class VelocityPlugin @Inject constructor(
    private val proxy: ProxyServer,
    private val logger: Logger,
) {

    private val proxyContext = ProxyContext(proxy = proxy)
    private val pluginContext = PluginContext(plugin = this)
    private val loggerContext = LoggerContext(logger = logger)

    private val jedisClusterContext = JedisClusterContext(
        jedisCluster = JedisCluster(
            mutableSetOf(
                HostAndPort("0.0.0.0", 6001),
                HostAndPort("0.0.0.0", 6002),
                HostAndPort("0.0.0.0", 6003),
            ), 5000, 5000, 60, GenericPoolConfig
        )
    )

    private val redisDataManagerContext = run {
        with(receiver = proxyContext) {
            with(receiver = pluginContext) {
                RedisDataManagerContext(redisDataManager = RedisDataManager())
            }
        }
    }

    private val redisAPIContext = RedisAPIContext(redisAPI = RedisAPI())
    private val jedisPubSubHandlerContext = run {
        with(receiver = pluginContext) {
            JedisPubSubHandlerContext(jedisPubSubHandler = JedisPubSubHandler())
        }
    }

    private val jedisListenerContext = run {
        with(receiver = pluginContext) {
            with(receiver = jedisClusterContext) {
                with(receiver = jedisPubSubHandlerContext) {
                    JedisListenerContext(jedisListener = JedisListener())
                }
            }
        }
    }

    private lateinit var integrityCheckTask: ScheduledTask

    private lateinit var heartbeatTask: ScheduledTask


    private val SERVER_TO_PLAYERS_KEY = Any()

    val IDENTIFIERS = listOf(
        MinecraftChannelIdentifier.create("legacy", "redisbungee"),
        LegacyChannelIdentifier("RedisBungee"),  // This is needed for clients before 1.13
        LegacyChannelIdentifier("legacy:redisbungee")
    )

    @Volatile
    lateinit var proxiesIds: List<String>
        private set


    @Subscribe
    fun proxyInit(event: ProxyInitializeEvent) {

        with(receiver = pluginContext) { checkRedisVersion() }

        with(receiver = jedisClusterContext) {
            with(receiver = redisAPIContext) {
                updateProxiesIds()
            }
        }

        buildTasks()

        registerCommands()
        registerEvents()

        registerPluginMessages()

        //pl startAPI()

    }

    @Subscribe
    fun proxyShutdownEvent(event: ProxyShutdownEvent) {

        with(receiver = jedisListenerContext) { jedisListener.poison() }

        integrityCheckTask.cancel()
        heartbeatTask.cancel()

        with(receiver = jedisClusterContext) {
            with(receiver = redisAPIContext) {
                ShutdownUtils.shutdownCleanup()
            }
        }

        try {
            JedisSummoner.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        with(receiver = MojangHttpClient.dispatcher.executorService) {

            shutdown()

            try {
                awaitTermination(20, TimeUnit.SECONDS)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    fun buildTasks() = with(receiver = proxyContext) {
        with(receiver = pluginContext) {
            with(receiver = proxy.scheduler) {

                with(receiver = jedisListenerContext) {
                    buildTask(plugin, jedisListener).schedule()
                }


                with(receiver = jedisClusterContext) {
                    with(receiver = redisAPIContext) {

                        integrityCheckTask =
                            buildTask(plugin, Runnable { IntegrityCheckTask().execute() }).repeat(30, TimeUnit.SECONDS)
                                .schedule()

                        with(receiver = loggerContext) {
                            heartbeatTask = buildTask(plugin, HeartbeatTask())
                                .repeat(1, TimeUnit.SECONDS).schedule()
                        }
                    }
                }
            }
        }
    }

    fun registerEvents() = with(receiver = proxyContext) {
        with(receiver = pluginContext) {

            with(receiver = redisDataManagerContext) {

                redisDataManager.registerAsListener()

                with(receiver = jedisClusterContext) {
                    with(receiver = redisAPIContext) {
                        RedisNetworkListener().registerAsListener()
                    }
                }
            }

            ChooseInitialServerListener().registerAsListener()
        }
    }

    context(PluginContext)
    fun Any.registerAsListener() = proxy.eventManager.register(plugin, this@Any)

    fun registerCommands() = with(receiver = proxyContext) {
        with(receiver = pluginContext) {

            PlayCommand.BRIGADIER.register("play")

            with(receiver = redisDataManagerContext) {
                with(receiver = redisAPIContext) {
                    FindCommand().register("find")
                }
            }

            with(receiver = redisDataManagerContext) {
                with(receiver = redisAPIContext) {
                    LastSeenCommand().register("lastseen", "ls")
                    IpCommand().register("ip")
                }
            }
        }
    }

    context(ProxyContext)
    fun Command.register(alias: String, vararg otherAliases: String) =
        proxy.commandManager.register(alias, this@Command, *otherAliases)

    private val serverToPlayersCache =
        CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS).build<Any, Multimap<String, UUID>>()

    val localPlayersAsUuidStrings: Set<String>
        get() {
            val builder = ImmutableSet.builder<String>()
            for (player in proxy.allPlayers) {
                builder.add(player.uniqueId.toString())
            }
            return builder.build()
        }


    context (JedisClusterContext)
    fun serverToPlayersCache() = try {
        serverToPlayersCache[SERVER_TO_PLAYERS_KEY, { serversToPlayers() }]
    } catch (e: ExecutionException) {
        throw RuntimeException(e)
    }

    fun executeAsync(runnable: Runnable) = proxy.scheduler.buildTask(this, runnable).schedule()

    fun executeAsyncAfter(runnable: Runnable, timeUnit: TimeUnit, time: Int) =
        proxy.scheduler.buildTask(this, runnable).delay(time.toLong(), timeUnit).schedule()

    fun fireEvent(event: Any) = proxy.eventManager.fireAndForget(event)

    val isOnlineMode: Boolean
        get() = proxy.configuration.isOnlineMode

    fun logInfo(msg: String) = logger.info(msg)

    fun logWarn(msg: String) = logger.warn(msg)

    fun logFatal(msg: String) = logger.error(msg)

    fun getPlayer(uuid: UUID): Player? = proxy.getPlayer(uuid).orElse(null)

    fun getPlayer(name: String): Player? = proxy.getPlayer(name).orElse(null)

    fun getPlayerUUID(player: String): UUID? = proxy.getPlayer(player).map { obj: Player -> obj.uniqueId }.orElse(null)

    fun getPlayerName(player: UUID?): String? = proxy.getPlayer(player).map { obj: Player -> obj.username }.orElse(null)

    fun getPlayerServerName(player: Player): String? =
        player.currentServer.map { serverConnection: ServerConnection -> serverConnection.serverInfo.name }.orElse(null)

    fun isPlayerOnAServer(player: Player) = player.currentServer.isPresent

    fun getPlayerIp(player: Player): InetAddress = player.remoteAddress.address

    private fun registerPluginMessages() = IDENTIFIERS.forEach {
        proxy.channelRegistrar.register(it)
    }

    private fun startAPI() {
        val currentThread = Thread.currentThread()
        val oldClassLoader = currentThread.contextClassLoader
        try {
            currentThread.contextClassLoader = RestfulAPI::class.java.classLoader
            SpringApplication.run(RestfulAPI::class.java)
        } finally {
            currentThread.contextClassLoader = oldClassLoader
        }
    }

    context (JedisClusterContext, RedisAPIContext)
    fun updateProxiesIds() {
        proxiesIds = getCurrentProxiesIds(false)
    }

    fun createPlayerChangedServerNetworkEvent(
        uuid: UUID, previousServer: String?, server: String
    ) = PlayerChangedServerNetworkEvent(
        uuid = uuid, previousServer = previousServer, server = server
    )


    fun createPlayerJoinedNetworkEvent(uuid: UUID) = PlayerJoinedNetworkEvent(
        uuid = uuid
    )

    fun createPlayerLeftNetworkEvent(uuid: UUID) = PlayerLeftNetworkEvent(
        uuid = uuid
    )

    fun createPubSubEvent(channel: String, message: String) = PubSubMessageEvent(
        channel = channel, message = message
    )

    fun getResourceAsStream(name: String?) = javaClass.classLoader.getResourceAsStream(name)

    context (JedisClusterContext, LoggerContext)
    val currentCount: Long
        get() {
            var total: Long = 0
            val redisTime = getRedisTime()
            val heartBeats = jedisCluster.hgetAll("heartbeats")

            for ((k, v) in heartBeats) {
                val heartbeatTime = v.toLong()
                if (heartbeatTime + 30 < redisTime) continue

                total += jedisCluster.scard("proxy:$k:usersOnline")

            }

            return total
        }

    // Redis server has disappeared!
    val players: Set<UUID?>
        get() = object : RedisTask<Set<UUID>>() {
            override fun unifiedJedisTask(unifiedJedis: UnifiedJedis): Set<UUID> {


                var setBuilder = ImmutableSet.builder<UUID?>()
                try {
                    val keys: MutableList<String> = ArrayList()
                    for (i in proxiesIds) {
                        keys.add("proxy:$i:usersOnline")
                    }
                    if (keys.isNotEmpty()) {
                        val users = unifiedJedis!!.sunion(*keys.toTypedArray())
                        if (users != null && users.isNotEmpty()) {
                            for (user in users) {
                                try {
                                    setBuilder = setBuilder.add(UUID.fromString(user))
                                } catch (ignored: IllegalArgumentException) {
                                }
                            }
                        }
                    }
                } catch (e: JedisConnectionException) {
                    // Redis server has disappeared!
                    logFatal("Unable to get connection from pool - did your Redis server go away?")
                    throw RuntimeException("Unable to get all players online", e)
                }
                return setBuilder.build()
            }
        }.execute()


    context(JedisClusterContext)
    fun serversToPlayers(): Multimap<String, UUID> {
        val builder = ImmutableMultimap.builder<String, UUID>()

        for (serverId in proxiesIds) {
            val players = jedisCluster.smembers("proxy:$serverId:usersOnline")
            for (player in players) {
                val playerServer = jedisCluster.hget("player:$player", "server") ?: continue
                builder.put(playerServer, UUID.fromString(player))
            }
        }

        return builder.build()
    }

    context(JedisClusterContext)
    fun getPlayersOnProxy(proxyId: String): Set<UUID> {
        Preconditions.checkArgument(proxiesIds.contains(proxyId), "$proxyId is not a valid proxy ID")

        val users = jedisCluster.smembers("proxy:$proxyId:usersOnline")
        val builder = ImmutableSet.builder<UUID>()

        users.forEach { builder.add(UUID.fromString(it)) }

        return builder.build()
    }

    context (JedisClusterContext, LoggerContext)
    fun sendProxyCommand(proxyId: String, command: String) {
        Preconditions.checkArgument(proxiesIds.contains(proxyId) || proxyId == "allservers", "proxyId is invalid")
        sendChannelMessage("redisbungee-$proxyId", command)
    }

    context(JedisClusterContext, RedisAPIContext)
    fun getCurrentProxiesIds(lagged: Boolean) = try {

        val time = getRedisTime()
        val servers = ImmutableList.builder<String>()
        val heartbeats = jedisCluster.hgetAll("heartbeats")
        for ((key, value) in heartbeats) {

            try {
                val stamp = value.toLong()
                if (if (lagged) time >= stamp + RedisUtil.PROXY_TIMEOUT else time <= stamp + RedisUtil.PROXY_TIMEOUT) {
                    servers.add(key)
                } else if (time > stamp + RedisUtil.PROXY_TIMEOUT) {
                    logWarn(key + " is " + (time - stamp) + " seconds behind! (Time not synchronized or server down?) and was removed from heartbeat.")
                    jedisCluster.hdel("heartbeats", key)
                }
            } catch (ignored: NumberFormatException) {
            }
        }
        servers.build()

    } catch (e: JedisConnectionException) {
        logFatal("Unable to fetch server IDs")
        e.printStackTrace()
        listOf(redisAPI.proxyId)
    }

    context (JedisClusterContext, LoggerContext)
    fun sendChannelMessage(channel: String, message: String) = try {
        jedisCluster.publish(channel, message)
    } catch (e: JedisConnectionException) {
        logger.error("Unable to get connection from pool - did the Redis cluster die?", e)
    }

    context (JedisClusterContext, LoggerContext)
    fun sendProxyCommand(cmd: String) = sendProxyCommand(RedisConfig.proxyId, cmd)

    context (JedisClusterContext)
    fun getRedisTime(): Long {
        val data = jedisCluster.sendCommand(Protocol.Command.TIME) as List<*>
        val times: MutableList<String> = ArrayList()
        data.forEach(Consumer { o: Any? -> times.add(String((o as ByteArray?)!!)) })
        return getRedisTime(times)
    }

    fun getRedisTime(timeRes: List<String>) = timeRes[0].toLong()

    context (RedisDataManagerContext, RedisAPIContext)
    fun kickPlayer(playerUniqueId: UUID, message: String) = if (!redisDataManager.handleKick(playerUniqueId, message)) {
        object : RedisTask<Unit>() {

            override fun unifiedJedisTask(unifiedJedis: UnifiedJedis) =
                PayloadUtils.kickPlayerPayload(playerUniqueId, message, unifiedJedis)

        }.execute()
    } else Unit

    context (PluginContext, RedisDataManagerContext, RedisAPIContext)
    fun kickPlayer(playerName: String, message: String) =
        UUIDTranslator.getTranslatedUuid(playerName, true)?.let { kickPlayer(it, message) }

}