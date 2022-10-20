package proxy.commands

import com.velocitypowered.api.command.SimpleCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import proxy.commands.Messages.NO_PLAYER_SPECIFIED
import proxy.commands.Messages.PLAYER_NOT_FOUND
import proxy.contexts.ProxyContext
import proxy.contexts.PluginContext
import proxy.contexts.RedisAPIContext
import proxy.contexts.RedisDataManagerContext
import proxy.redis.RedisAPI
import proxy.redis.util.uuid.UUIDTranslator
import java.net.InetAddress

context (ProxyContext, PluginContext, RedisDataManagerContext, RedisAPIContext)
class IpCommand : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        val sender = invocation.source()
        val args = invocation.arguments()
       proxy.scheduler.buildTask(plugin, Runnable {
            if (args.isNotEmpty()) {
                val uuid = UUIDTranslator.getTranslatedUuid(args[0], true)
                if (uuid == null) {
                    sender.sendMessage(PLAYER_NOT_FOUND)
                    return@Runnable
                }
                val ia: InetAddress? = redisAPI.getPlayerIp(uuid)
                if (ia != null) {
                    val message =
                        Component.text(args[0] + " is connected from " + ia.toString() + ".", NamedTextColor.GREEN)
                    sender.sendMessage(message)
                } else {
                    sender.sendMessage(PLAYER_NOT_FOUND)
                }
            } else {
                sender.sendMessage(NO_PLAYER_SPECIFIED)
            }
        }).schedule()
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation): Boolean {
        return invocation.source().hasPermission("redisbungee.command.ip")
    }
}