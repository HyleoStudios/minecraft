package proxy.events

import java.util.*

/**
 * This event is sent when a player joins the network. RedisBungee sends the event only when
 * the proxy the player has been connected to is different than the local proxy.
 *
 *
 * This event corresponds to [com.velocitypowered.api.event.connection.PostLoginEvent], and is fired
 * asynchronously.
 *
 * @since 0.3.4
 */
class PlayerJoinedNetworkEvent( val uuid: UUID)