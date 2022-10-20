package io.hyleo.api

import org.bukkit.block.data.BlockData
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.loot.LootContext

data class BlockProcessInfo(
    val phase: Phase,
    val blockBreakEvent: BlockBreakEvent,
    val lootContext: LootContext,
    val blockData: BlockData
)
