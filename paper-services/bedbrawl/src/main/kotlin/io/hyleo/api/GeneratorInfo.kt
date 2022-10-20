package io.hyleo.api

import net.kyori.adventure.text.Component
import org.bukkit.Material

enum class GeneratorInfo(val displayName: Component, val icon: Material? = null, val materials: Map<Currency, List<SpawnRate>>) {
    TEAM_FORGE(
        displayName = Component.text("Team Forge"),
        materials = mapOf(Currency.IRON to listOf(SpawnRate(ticks = 12)), Currency.GOLD to listOf(SpawnRate(ticks = 72)))
    ),
    IRON_FORGE(
        displayName = Component.text("Iron Forge"),
        materials = mapOf(Currency.IRON to listOf(SpawnRate(ticks = 12)), Currency.GOLD to listOf(SpawnRate(ticks = 72)))
    ),
    GOLDEN_FORGE(
        displayName = Component.text("Golden Forge"),
        materials = mapOf(Currency.IRON to listOf(SpawnRate(ticks = 12)), Currency.GOLD to listOf(SpawnRate(ticks = 72)))
    ),
    EMERALD_FORGE(
        displayName = Component.text("Emerald Forge"),
        materials = mapOf(Currency.IRON to listOf(SpawnRate(ticks = 12)), Currency.GOLD to listOf(SpawnRate(ticks = 72)))
    ),
    MOLTEN_FORGE(
        displayName = Component.text("Molten Forge"),
        materials = mapOf(Currency.IRON to listOf(SpawnRate(ticks = 12)), Currency.GOLD to listOf(SpawnRate(ticks = 72)))
    ),
    DIAMOND(
        displayName = Component.text("Diamond"),
        icon = Material.DIAMOND_BLOCK,
        materials = mapOf(Currency.DIAMOND to listOf(SpawnRate(ticks = 900), SpawnRate(ticks = 600), SpawnRate(ticks = 300)))
    ),
    EMERALD(
        displayName = Component.text("Emerald"),
        icon = Material.EMERALD_BLOCK,
        materials = mapOf(Currency.EMERALD to listOf(SpawnRate(ticks = 1800), SpawnRate(ticks = 1200), SpawnRate(ticks = 600)))
    ),
}