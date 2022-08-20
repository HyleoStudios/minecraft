package io.hyleo.proxy.listeners

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import com.velocitypowered.api.proxy.server.ServerInfo
import proxy.Hyleo
import java.net.InetSocketAddress

class LoginListener {
    @Subscribe
    fun proxyLogin(event: PlayerChooseInitialServerEvent) {
        val logger = Hyleo.logger();
        Hyleo.proxy().registerServer(ServerInfo("The Lobby", InetSocketAddress("localhost", 25565)))
        val server = Hyleo.proxy().getServer("The Lobby")
        event.setInitialServer(server.get())
    }

}