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
import java.text.SimpleDateFormat
import java.util.*


context (ProxyContext, PluginContext, RedisDataManagerContext, RedisAPIContext)
class LastSeenCommand : SimpleCommand {

    override fun execute(invocation: SimpleCommand.Invocation) {
        proxy.scheduler.buildTask(plugin, Runnable {
            val args = invocation.arguments()
            val sender = invocation.source()
            if (args.isNotEmpty()) {
                val uuid: UUID? = UUIDTranslator.getTranslatedUuid(args[0], true)
                if (uuid == null) {
                    sender.sendMessage(PLAYER_NOT_FOUND)
                    return@Runnable
                }
                val secs: Long = redisAPI.getLastOnline(uuid)
                val message = Component.text()
                if (secs == 0L) {
                    message.color(NamedTextColor.GREEN)
                    message.content(args[0] + " is currently online.")
                } else if (secs != -1L) {
                    message.color(NamedTextColor.BLUE)
                    message.content(args[0] + " was last online on " + SimpleDateFormat().format(secs) + ".")
                } else {
                    message.color(NamedTextColor.RED)
                    message.content(args[0] + " has never been online.")
                }
                sender.sendMessage(message.build())
            } else {
                sender.sendMessage(NO_PLAYER_SPECIFIED)
            }
        }).schedule()
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation): Boolean {
        return invocation.source().hasPermission("redisbungee.command.lastseen")
    }
}