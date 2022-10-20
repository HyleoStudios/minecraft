package proxy.commands

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import proxy.VelocityPlugin
import proxy.commands.Messages.NO_PLAYER_SPECIFIED
import proxy.commands.Messages.PLAYER_NOT_FOUND
import proxy.contexts.PluginContext
import proxy.contexts.ProxyContext
import proxy.contexts.RedisAPIContext
import proxy.contexts.RedisDataManagerContext
import proxy.redis.RedisAPI
import proxy.redis.util.uuid.UUIDTranslator
import java.util.*

context(ProxyContext, PluginContext, RedisDataManagerContext, RedisAPIContext)
class FindCommand : SimpleCommand {

    override fun hasPermission(invocation: SimpleCommand.Invocation) = true

    override fun execute(invocation: SimpleCommand.Invocation) {
        val sender = invocation.source()
        val args = invocation.arguments()
        proxy.scheduler.buildTask(plugin, Runnable {

            if (args.isEmpty() && sender !is Player) {
                return@Runnable
            }

            val arg = if (args.isEmpty()) ((sender as Player).username) else args[0]

            val uuid: UUID? = UUIDTranslator.getTranslatedUuid(arg, true)

            if (uuid == null) {
                sender.sendMessage(PLAYER_NOT_FOUND)
                return@Runnable
            }

            val playerProxy: String? = redisAPI.getProxy(uuid)
            val server = proxy.getServer(redisAPI.getServerNameFor(uuid))
                .map { obj: RegisteredServer -> obj.serverInfo }
                .orElse(null)

            playerProxy?.let {
                server?.let {
                    connectedTo(arg, playerProxy, server.name, sender)
                    return@Runnable
                }
            }

            sender.sendMessage(PLAYER_NOT_FOUND)

        }).schedule()
    }

    private fun noArgs(sender: CommandSource) {

        if (sender !is Player) {
            sender.sendMessage(NO_PLAYER_SPECIFIED)
            return
        }

        sender.sendMessage(
            Component.text(
                "You are on proxy ${redisAPI.getPlayerIp(sender.uniqueId)}",
                NamedTextColor.GREEN
            )
        )
    }

    private fun connectedTo(player: String, proxy: String, server: String, sender: CommandSource) {
        sender.sendMessage(
            Component.textOfChildren(
                Component.text(
                    "$player is connected to ",
                    NamedTextColor.YELLOW
                ),
                Component.text(
                    "$proxy",
                    NamedTextColor.GREEN
                ),
                Component.text(
                    " and playing on ",
                    NamedTextColor.YELLOW
                ),
                Component.text(
                    "$server",
                    NamedTextColor.AQUA
                )
            )
        )
    }


}