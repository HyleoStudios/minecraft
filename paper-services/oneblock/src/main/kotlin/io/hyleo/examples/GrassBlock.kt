package io.hyleo.examples

import io.hyleo.api.block.Block
import io.hyleo.api.probability.Weight
import org.bukkit.Material

class GrassBlock : Block(
    material = Material.GRASS_BLOCK,
    weight = Weight(quality = 1, weight = 1.0),
    occurrences = mapOf(
      Undergrowth to 100f,
    ),
    xpDrops = listOf(
        Weight(quality = 1, weight = 3.0),
        Weight(quality = 3, weight = 1.0)
    ),
    process = { info -> info.blockBreakEvent.player },
)
