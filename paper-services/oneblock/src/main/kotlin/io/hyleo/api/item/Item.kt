package io.hyleo.api.item

import io.hyleo.api.Balanceable
import io.hyleo.api.block.ChestBlock
import io.hyleo.api.probability.Weight
import org.bukkit.inventory.ItemStack

open class Item(
    weight: Weight,
    occurrences: Map<ChestBlock, Float>,
    val stack: () -> ItemStack,
    val variants: Map<(ItemStack) -> Unit, Weight> = mapOf(),
) : Balanceable<ChestBlock>(
    weight = weight,
    occurrences = occurrences
) {

    fun stack(): ItemStack {
        val stack = stack()

        return stack
    }


}