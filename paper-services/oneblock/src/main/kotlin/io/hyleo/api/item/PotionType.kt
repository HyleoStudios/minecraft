package io.hyleo.api.item

import org.bukkit.Material

enum class PotionType(val material: Material) {
    DRINKABLE(Material.POTION),
    SPLASH(Material.SPLASH_POTION),
    LINGERING(Material.LINGERING_POTION),
}