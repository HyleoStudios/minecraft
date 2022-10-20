package io.hyleo.api

import io.hyleo.api.probability.Weight
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.loot.LootContext

class Creature(
    weight: Weight,
    occurrences: Map<Phase, Float>,
    val entity: EntityType,
    val quantities: List<Weight>,
    val spawnLocation: (BlockProcessInfo, Int) -> Location = { info, _ ->
        val loc = info.blockBreakEvent.block.location
        loc.add(0.0, 1.5, 0.0)
        loc
    },
    val spawned: (Phase, LootContext, Entity, Int) -> Unit,
) : Balanceable<Phase>(
    weight = weight,
    occurrences = occurrences,
)