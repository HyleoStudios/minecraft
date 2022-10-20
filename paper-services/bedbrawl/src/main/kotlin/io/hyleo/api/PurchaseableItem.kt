package io.hyleo.api

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

data class PurchaseableItem(
    val item: (Player) -> ItemStack,
    val currency: Currency,
    val costByKit: Map<Kit, Int>,
    val upgradedVersion: PurchaseableItem? = null,
)
