package proxy.events

import java.util.*

/**
 * This event is sent when a player disconnects. RedisBungee sends the event only when
 * the proxy the player has been connected to is different than the local proxy.
 *
 *
 * This event corresponds to [com.velocitypowered.api.event.connection.DisconnectEvent], and is fired
 * asynchronously.
 *
 * @since 0.3.4
 */
class PlayerLeftNetworkEvent( val uuid: UUID)