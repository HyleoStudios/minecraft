package io.hyleo.proxy.commands

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


class PlayCommand private constructor() {

    companion object {
        private const val NAME = "play"
        private val INSTANCE = PlayCommand()

        val BRIGADIER = BrigadierCommand(
            LiteralArgumentBuilder
                .literal<CommandSource>(NAME)
                .requires(INSTANCE::requirement)
                .executes(INSTANCE::playRandomModeRandomMap)
                .then(
                    INSTANCE.modeArgument().executes(INSTANCE::playRandomMap)
                        .then(INSTANCE.mapArgument().executes(INSTANCE::playExact))
                )
                .build()
        )
    }

    private fun requirement(source: CommandSource): Boolean {
        return source is Player
    }

    private fun playRandomModeRandomMap(context: CommandContext<CommandSource>): Int {
        val source = context.source
        val message = Component.text("Playing a random game and random map...", NamedTextColor.GREEN)
        source.sendMessage(message)

        return Command.SINGLE_SUCCESS
    }

    private fun modeArgument(): RequiredArgumentBuilder<CommandSource, String> {
        return RequiredArgumentBuilder.argument("mode", StringArgumentType.string())
    }

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

    private fun mapArgument(): RequiredArgumentBuilder<CommandSource, String> {
        return RequiredArgumentBuilder.argument("map", StringArgumentType.string())
    }

    private fun playExact(context: CommandContext<CommandSource>): Int {

        val mode = context.getArgument("mode", String::class.java)
        val map = context.getArgument("map", String::class.java)
        val source = context.source;

        source.sendMessage {
            Component.text(
                "Playing $mode on $map...",
                NamedTextColor.GREEN
            )
        }

        return Command.SINGLE_SUCCESS
    }

}

