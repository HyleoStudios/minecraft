package proxy.listeners

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.ServerInfo
import net.kyori.adventure.text.Component
import proxy.VelocityPlugin
import proxy.contexts.ProxyContext
import proxy.events.PlayerJoinedNetworkEvent
import java.net.InetSocketAddress

context(ProxyContext)
class ChooseInitialServerListener {

    @Subscribe
    fun chooseInitialServer(event: PlayerChooseInitialServerEvent) {
        val server = proxy.registerServer(ServerInfo("paper-001", InetSocketAddress("localhost", 25565)))
        event.setInitialServer(server)
    }

    @Subscribe
    fun login(event: PlayerJoinedNetworkEvent) {

        println("Player ${event.uuid} joined the network")

        proxy.getPlayer(event.uuid).ifPresent {
            it.disconnect(Component.text("You logged in from another location!"))
        }

    }


}