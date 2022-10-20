package io.hyleo.api.item

import io.hyleo.api.block.ChestBlock
import io.hyleo.api.probability.Weight
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

open class EnchantedItem(
    weight: Weight,
    occurrences: Map<ChestBlock, Float>,
    stack: () -> ItemStack,
    variants: Map<(ItemStack) -> Unit, Weight> = mapOf(),
    val ignoreConflicts: Boolean = false,
    val enchantments: Map<Enchantment, List<Weight>>,
    val weighRandomQuantities: (Float, Float) -> Float = { t, n -> t / (t - n) },
    val randomEnchantments: Map<Enchantment, List<Weight>> = mapOf(),
) : Item(
    weight = weight,
    occurrences = occurrences,
    stack = {
        val stack = stack()

        stack
    },
    variants = variants,
) {
}