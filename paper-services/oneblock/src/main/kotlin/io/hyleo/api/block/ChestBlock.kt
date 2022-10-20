package io.hyleo.api.block

import io.hyleo.api.Phase
import io.hyleo.api.probability.Weight
import net.kyori.adventure.text.Component
import org.bukkit.Material

class ChestBlock(
    weight: Weight,
    occurrences: Map<Phase, Float>,
    val name: String,
    val displayName: Component,
) : Block(
    material = Material.CHEST,
    weight = weight,
    occurrences = occurrences,
    process = {}
)