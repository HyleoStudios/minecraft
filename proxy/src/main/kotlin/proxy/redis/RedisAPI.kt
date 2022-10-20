package proxy.redis

import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Multimap
import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.api.proxy.server.ServerInfo
import proxy.*
import proxy.contexts.*
import proxy.redis.config.RedisConfig
import proxy.redis.jedis.JedisListener
import proxy.redis.jedis.JedisSummoner
import proxy.redis.util.uuid.UUIDTranslator
import redis.clients.jedis.JedisCluster
import java.util.*

/**
 * This platform class exposes some internal RedisBungee functions. You obtain an instance of this object by invoking [RedisAPI.getRedisBungeeApi]
 * or somehow you got the Plugin instance by you can call the api using [RedisBungeePlugin.getAbstractRedisBungeeApi].
 *
 * @author tuxed
 * @since 0.2.3 | updated 0.8.0
 */
class RedisAPI {

    /**
     * Get the server where the specified player is playing. This function also deals with the case of local players
     * as well, and will return local information on them.
     *
     * @param player a player uuid
     * @return [ServerInfo] Can be null if proxy can't find it.
     * @see .getServerNameFor
     */
    context(ProxyContext, RedisDataManagerContext)
    fun getServerFor(player: UUID) = proxy.getServer(getServerNameFor(player))
        .map { obj: RegisteredServer -> obj.serverInfo }
        .orElse(null)

    val reservedChannels = listOf(
        "redisbungee-allservers",
        "redisbungee-" + RedisConfig.proxyId,
        "redisbungee-data"
    )

    /**
     * Get the last time a player was on. If the player is currently online, this will return 0. If the player has not been recorded,
     * this will return -1. Otherwise it will return a value in milliseconds.
     *
     * @param player a player name
     * @return the last time a player was on, if online returns a 0
     */
    context(RedisDataManagerContext)
    fun getLastOnline(player: UUID) = redisDataManager.getLastOnline(player)

    /**
     * Get the server where the specified player is playing. This function also deals with the case of local players
     * as well, and will return local information on them.
     *
     * @param player a player uuid
     * @return a String name for the server the player is on.
     */
    context(RedisDataManagerContext)
    fun getServerNameFor(player: UUID) = redisDataManager.getServer(player)

    /**
     * Get a combined list of players on this network.
     *
     *
     * **Note that this function returns an instance of [com.google.common.collect.ImmutableSet].**
     *
     * @return a Set with all players found
     */
    context(PluginContext)
    val playersOnline: Set<UUID>
        get() = plugin.players as Set<UUID>

    /**
     * Get a combined list of players on this network, as a collection of usernames.
     *
     * @return a Set with all players found
     * @see .getNameFromUuid
     * @since 0.3
     */
    context(PluginContext)
    val humanPlayersOnline: Collection<String>
        get() {
            val names: MutableSet<String> = HashSet()
            for (uuid in playersOnline) {
                getNameFromUuid(uuid, false)?.let { names.add(it) }
            }
            return names
        }

    /**
     * Get a full list of players on all servers.
     *
     * @return a immutable Multimap with all players found on this server
     * @since 0.2.5
     */
    context(PluginContext, JedisClusterContext)
    val serverToPlayers: Multimap<String, UUID>
        get() = plugin.serverToPlayersCache()

    /**
     * Get a list of players on the server with the given name.
     *
     * @param server a server name
     * @return a Set with all players found on this server
     */
    context(PluginContext, JedisClusterContext)
    fun getPlayersOnServer(server: String) = ImmutableSet.copyOf(serverToPlayers.get(server) ?: emptySet())

    /**
     * Get a list of players on the specified proxy.
     *
     * @param server a server name
     * @return a Set with all UUIDs found on this proxy
     */
    context(PluginContext, JedisClusterContext)
    fun getPlayersOnProxy(server: String) = plugin.getPlayersOnProxy(server)


    /**
     * Convenience method: Checks if the specified player is online.
     *
     * @param player a player name
     * @return if the player is online
     */
    context(RedisDataManagerContext)
    fun isPlayerOnline(player: UUID) = getLastOnline(player) == 0L


    /**
     * Get the [java.net.InetAddress] associated with this player.
     *
     * @param player the player to fetch the IP for
     * @return an [java.net.InetAddress] if the player is online, null otherwise
     * @since 0.2.4
     */
    context(RedisDataManagerContext)
    fun getPlayerIp(player: UUID) = redisDataManager.getIp(player)


    /**
     * Get the RedisBungee proxy ID this player is connected to.
     *
     * @param player the player to fetch the IP for
     * @return the proxy the player is connected to, or null if they are offline
     * @since 0.3.3
     */
    context(RedisDataManagerContext)
    fun getProxy(player: UUID) = redisDataManager.getProxy(player)


    /**
     * Sends a proxy command to the proxy with the given ID. "allservers" means all proxies.
     *
     * @param proxyId a proxy ID
     * @param command the command to send and execute
     * @see .getProxyId
     * @see .getAllProxies
     * @since 0.2.5
     */
    context(PluginContext, JedisClusterContext, LoggerContext)
    fun sendProxyCommand(proxyId: String = "allservers", command: String) =
        plugin.sendProxyCommand(proxyId, command)


    /**
     * Sends a message to a PubSub channel. The channel has to be subscribed to on this, or another redisbungee instance for
     * PubSubMessageEvent to fire.
     *
     * @param channel The PubSub channel
     * @param message the message body to send
     * @since 0.3.3
     */
    context(PluginContext, JedisClusterContext, LoggerContext)
    fun sendChannelMessage(channel: String, message: String) =
        plugin.sendChannelMessage(channel, message)


    /**
     * Get the current BungeeCord / Velocity proxy ID for this server.
     *
     * @return the current server ID
     * @see .getAllProxies
     * @since 0.8.0
     */
    val proxyId: String
        get() = RedisConfig.proxyId


    /**
     * Get all the linked proxies in this network.
     *
     * @return the list of all proxies
     * @see .getProxyId
     * @since 0.8.0
     */
    context (PluginContext)
    val allProxies: List<String>
        get() = plugin.proxiesIds

    /**
     * Register (a) PubSub channel(s), so that you may handle PubSubMessageEvent for it.
     *
     * @param channels the channels to register
     * @since 0.3
     */
    context (JedisListenerContext)
    fun registerPubSubChannels(vararg channels: String) = jedisListener.addChannel(*channels)

    /**
     * Unregister (a) PubSub channel(s).
     *
     * @param channels the channels to unregister
     * @since 0.3
     */
    context (JedisListenerContext)
    fun unregisterPubSubChannels(vararg channels: String) {
        for (channel in channels) {
            Preconditions.checkArgument(
                !reservedChannels.contains(channel), "attempting to unregister internal channel"
            )
        }
        jedisListener.removeChannel(*channels)
    }

    /**
     * Fetch a name from the specified UUID. UUIDs are cached locally and in Redis. This function falls back to Mojang
     * as a last resort, so calls **may** be blocking.
     *
     *
     * For the common use case of translating a list of UUIDs into names, use [.getHumanPlayersOnline] instead.
     *
     *
     * If performance is a concern, use [.getNameFromUuid] as this allows you to disable Mojang lookups.
     *
     * @param uuid the UUID to fetch the name for
     * @return the name for the UUID
     * @since 0.3
     */
    context (PluginContext)
    fun getNameFromUuid(uuid: UUID) = getNameFromUuid(uuid, true)


    /**
     * Fetch a name from the specified UUID. UUIDs are cached locally and in Redis. This function can fall back to Mojang
     * as a last resort if `expensiveLookups` is true, so calls **may** be blocking.
     *
     *
     * For the common use case of translating the list of online players into names, use [.getHumanPlayersOnline].
     *
     *
     * If performance is a concern, set `expensiveLookups` to false as this will disable lookups via Mojang.
     *
     * @param uuid             the UUID to fetch the name for
     * @param expensiveLookups whether or not to perform potentially expensive lookups
     * @return the name for the UUID
     * @since 0.3.2
     */
    context (PluginContext)
    fun getNameFromUuid(uuid: UUID, expensiveLookups: Boolean) =
        UUIDTranslator.getNameFromUuid(uuid, expensiveLookups)

    /**
     * Fetch a UUID from the specified name. Names are cached locally and in Redis. This function falls back to Mojang
     * as a last resort, so calls **may** be blocking.
     *
     *
     * If performance is a concern, see [.getUuidFromName], which disables the following functions:
     *
     *  * Searching local entries case-insensitively
     *  * Searching Mojang
     *
     *
     * @param name the UUID to fetch the name for
     * @return the UUID for the name
     * @since 0.3
     */
    context(PluginContext)
    fun getUuidFromName(name: String) = getUuidFromName(name, true)

    /**
     * Fetch a UUID from the specified name. Names are cached locally and in Redis. This function falls back to Mojang
     * as a last resort if `expensiveLookups` is true, so calls **may** be blocking.
     *
     *
     * If performance is a concern, set `expensiveLookups` to false to disable searching Mojang and searching for usernames
     * case-insensitively.
     *
     * @param name             the UUID to fetch the name for
     * @param expensiveLookups whether or not to perform potentially expensive lookups
     * @return the [UUID] for the name
     * @since 0.3.2
     */
    context (PluginContext)
    fun getUuidFromName(name: String, expensiveLookups: Boolean) =
        UUIDTranslator.getTranslatedUuid(name, expensiveLookups)


    /**
     * Kicks a player from the network
     *
     * @param playerName player name
     * @param message    kick message that player will see on kick
     * @since 0.8.0
     */
    context (PluginContext, RedisDataManagerContext, RedisAPIContext)
    fun kickPlayer(playerName: String, message: String) =
        plugin.kickPlayer(playerName, message)

    /**
     * Kicks a player from the network
     *
     * @param playerUUID player name
     * @param message    kick message that player will see on kick
     * @since 0.8.0
     */
    context (PluginContext, RedisDataManagerContext, RedisAPIContext)
    fun kickPlayer(playerUUID: UUID, message: String) = plugin.kickPlayer(playerUUID, message)

    /**
     * This gives you instance of JedisCluster
     * WARNING DO NOT USE [JedisCluster.close] it will break the functionally
     *
     * @return [redis.clients.jedis.JedisCluster]
     * @throws IllegalStateException if the [.getMode] is not equal to [RedisBungeeMode.CLUSTER]
     * @since 0.8.0
     */
    fun requestClusterJedis() = JedisSummoner


}