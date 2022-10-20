package io.hyleo.api.block

import io.hyleo.api.Balanceable
import io.hyleo.api.BlockProcessInfo
import io.hyleo.api.Phase
import io.hyleo.api.probability.Weight
import org.bukkit.Material

open class Block(
    weight: Weight,
    occurrences: Map<Phase, Float>,
    val material: Material,
    val xpDrops: List<Weight> = listOf(),
    val process: (BlockProcessInfo) -> Unit = { _ -> },
) : Balanceable<Phase>(
    weight = weight,
    occurrences = occurrences
)