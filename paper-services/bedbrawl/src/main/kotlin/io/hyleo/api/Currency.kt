package io.hyleo.api

import net.kyori.adventure.text.Component
import org.bukkit.Material

enum class Currency(val material: Material, val displayName: Component) {

    IRON(Material.IRON_INGOT, Component.text("Iron")),
    GOLD(Material.GOLD_INGOT, Component.text("Gold")),
    DIAMOND(Material.DIAMOND, Component.text("Diamond")),
    EMERALD(Material.EMERALD, Component.text("Emerald"))
}