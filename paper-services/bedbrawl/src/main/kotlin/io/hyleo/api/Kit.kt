package io.hyleo.api

import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

enum class Kit(val displayName: Component, val spawnItems: List<()-> ItemStack>, val respawnItems: List<ItemStack>) {

    DEFAULT(Component.text("Default"), listOf(), listOf()),
    BUILDER(Component.text("Builder"), listOf(), listOf()),
    Miner(Component.text("Default"), listOf(), listOf()),
    ENGINEER(Component.text("Engineer"), listOf(), listOf()),
    TRAINER(Component.text("Trainer"), listOf(), listOf()),
}