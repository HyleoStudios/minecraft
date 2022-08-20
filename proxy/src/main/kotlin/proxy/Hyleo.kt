package proxy

import com.google.inject.Inject
import com.velocitypowered.api.command.CommandManager
import com.velocitypowered.api.event.EventManager
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import io.hyleo.proxy.commands.PlayCommand
import io.hyleo.proxy.display.*
import io.hyleo.proxy.display.text.TextAnimation
import io.hyleo.proxy.display.text.TextPattern
import io.hyleo.proxy.display.text.TextAnimationState
import io.hyleo.proxy.listeners.LoginListener
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.bossbar.BossBar.Color
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.slf4j.Logger
import java.util.concurrent.TimeUnit

class Hyleo @Inject constructor(private val server: ProxyServer, private val logger: Logger) {

    companion object {
        private lateinit var instance: Hyleo
        fun logger(): Logger = instance.logger
        fun proxy(): ProxyServer = instance.server
    }

    init {
        instance = this
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        val em = server.eventManager
        val cm = server.commandManager

        registerListeners(em)
        registerCommands(cm)

        server.scheduler.buildTask(this) { bossBar.display() }.repeat(1000, TimeUnit.MILLISECONDS).schedule()
    }

    @Subscribe
    fun login(event: ServerConnectedEvent) {
        hyleoText(event.player)
    }

    private fun registerListeners(em: EventManager) {
        em.register(this, LoginListener())
    }

    private fun registerCommands(cm: CommandManager) {
        cm.register(PlayCommand.BRIGADIER)
    }

    val bossBar = Display<Player, BossBar, TextAnimation, TextAnimationState, Component>(
        intervalSupport = false,
        condition = { p -> p.isActive },
        create = { p, s ->
            s.forEach { e -> e.key.name(e.value) }
            s.forEach { e -> p.showBossBar(e.key) }
            return@Display listOf()
        },
        update = { _, s ->
            s.forEach { e -> e.key.name(e.value) }
            return@Display listOf()
        },
        destroy = { p, s ->
            s.forEach { b -> p.hideBossBar(b) }
            return@Display listOf()
        },
    )

    fun hyleoText(player: Player) {
        val animation = TextAnimation(
            colors = { listOf(NamedTextColor.RED, NamedTextColor.DARK_AQUA) },
            depth = { 20 },
            pattern = { TextPattern.FLASH },
            text = { "Hyleo" },
        )

        val timings = Timings(
            interval = { 1 },
        )

        val slot = BossBar.bossBar(Component.empty(), 0.0f, Color.RED, BossBar.Overlay.PROGRESS)

        bossBar.display(player, slot, timings, animation)
    }

}



