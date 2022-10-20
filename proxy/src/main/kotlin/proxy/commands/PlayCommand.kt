package proxy.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor


object PlayCommand {

    private const val NAME = "play"

    val BRIGADIER = BrigadierCommand(
        LiteralArgumentBuilder
            .literal<CommandSource>(NAME)
            .requires(this::requirement)
            .executes(this::playRandomModeRandomMap)
            .then(
                modeArgument().executes(this::playRandomMap)
                    .then(mapArgument().executes(this::playExact))
            )
            .build()
    )

    private fun requirement(source: CommandSource) = source is Player

    private fun playRandomModeRandomMap(context: CommandContext<CommandSource>): Int {
        val source = context.source
        val message = Component.text("Playing a random game and random map...", NamedTextColor.GREEN)
        source.sendMessage(message)

        return Command.SINGLE_SUCCESS
    }

    private fun modeArgument() =
        RequiredArgumentBuilder.argument<CommandSource, String>("mode", StringArgumentType.string())

    private fun playRandomMap(context: CommandContext<CommandSource>): Int {
        val argumentProvided = context.getArgument("mode", String::class.java)

        context.source.sendMessage(
            Component.text(
                "Playing $argumentProvided with a random map...",
                NamedTextColor.GREEN
            )
        )
        return Command.SINGLE_SUCCESS
    }

    private fun mapArgument() =
        RequiredArgumentBuilder.argument<CommandSource, String>("map", StringArgumentType.string())

    private fun playExact(context: CommandContext<CommandSource>): Int {

        val mode = context.getArgument("mode", String::class.java)
        val map = context.getArgument("map", String::class.java)
        val source = context.source;

        source.sendMessage {
            Component.text(
                "Playing ",
                NamedTextColor.GREEN
            ).append(
                Component.text(
                    mode,
                    NamedTextColor.AQUA
                )
            ).append(
                Component.text(
                    " on ",
                    NamedTextColor.GREEN
                )
            ).append(
                Component.text(
                    map,
                    NamedTextColor.YELLOW
                )
            )
        }

        return Command.SINGLE_SUCCESS
    }

}

