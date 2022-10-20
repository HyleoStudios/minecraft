package proxy.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

object Messages {

    val NO_PLAYER_SPECIFIED: Component = Component.text("You must specify a player's name.", NamedTextColor.RED)

    val PLAYER_NOT_FOUND: Component = Component.text("Player not found.", NamedTextColor.RED)

    val NO_COMMAND_SPECIFIED: Component =
        Component.text("You must specify a command to be run.", NamedTextColor.RED)

}