package io.hyleo.api

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Color
import org.bukkit.Material

enum class TeamInfo(
    val prefix: String,
    val textColor: TextColor,
    val armorColor: Color,
    val bed: Material,
    val wool: Material,
    val glass: Material,
    val terracotta: Material
) {

    RED(
        prefix = "Red",
        textColor = NamedTextColor.RED,
        armorColor = Color.RED,
        bed = Material.RED_BED,
        wool = Material.RED_WOOL,
        glass = Material.RED_STAINED_GLASS,
        terracotta = Material.RED_TERRACOTTA
    ),
    ORANGE(
        prefix = "Orange",
        textColor = TextColor.color(255, 127, 0),
        armorColor = Color.ORANGE,
        bed = Material.ORANGE_BED,
        wool = Material.ORANGE_WOOL,
        glass = Material.ORANGE_STAINED_GLASS,
        terracotta = Material.ORANGE_TERRACOTTA
    ),
    YELLOW(
        prefix = "Yellow",
        textColor = NamedTextColor.YELLOW,
        armorColor = Color.YELLOW,
        bed = Material.YELLOW_BED,
        wool = Material.YELLOW_WOOL,
        glass = Material.YELLOW_STAINED_GLASS,
        terracotta = Material.YELLOW_TERRACOTTA
    ),
    GREEN(
        prefix = "Green",
        textColor = NamedTextColor.GREEN,
        armorColor = Color.GREEN,
        bed = Material.GREEN_BED,
        wool = Material.GREEN_WOOL,
        glass = Material.GREEN_STAINED_GLASS,
        terracotta = Material.GREEN_TERRACOTTA
    ),
    BLUE(
        prefix = "Blue",
        textColor = NamedTextColor.BLUE,
        armorColor = Color.BLUE,
        bed = Material.BLUE_BED,
        wool = Material.BLUE_WOOL,
        glass = Material.BLUE_STAINED_GLASS,
        terracotta = Material.BLUE_TERRACOTTA
    ),
    PURPLE(
        prefix = "Purple",
        textColor = NamedTextColor.DARK_PURPLE,
        bed = Material.PURPLE_BED,
        armorColor = Color.PURPLE,
        wool = Material.PURPLE_WOOL,
        glass = Material.PINK_STAINED_GLASS,
        terracotta = Material.PURPLE_TERRACOTTA
    ),
    WHITE(
        prefix = "White",
        textColor = NamedTextColor.WHITE,
        armorColor = Color.WHITE,
        bed = Material.WHITE_BED,
        wool = Material.WHITE_WOOL,
        glass = Material.WHITE_STAINED_GLASS,
        terracotta = Material.WHITE_TERRACOTTA
    ),
    GRAY(
        prefix = "Gray",
        textColor = NamedTextColor.GRAY,
        armorColor = Color.GRAY,
        bed = Material.GRAY_BED,
        wool = Material.GRAY_WOOL,
        glass = Material.GRAY_STAINED_GLASS,
        terracotta = Material.GRAY_TERRACOTTA
    )
}