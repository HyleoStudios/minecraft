package io.hyleo.api.item

import io.hyleo.api.block.ChestBlock
import io.hyleo.api.Effect
import io.hyleo.api.probability.Weight
import org.bukkit.Color
import org.bukkit.inventory.ItemStack

open class PotionItem(
    weight: Weight,
    occurrences: Map<ChestBlock, Float>,
    variants: Map<(ItemStack) -> Unit, Weight> = mapOf(),
    val type: PotionType,
    val color: Color,
    val effects: Map<Effect, List<Weight>>,
    val weighRandomQuantities: (Float, Float) -> Float = { t, n -> t / (t - n) },
    val randomEffects: Map<Effect, List<Weight>> = mapOf(),
) : Item(
    weight = weight,
    occurrences = occurrences,
    stack = {
        val stack = ItemStack(type.material)

        stack
    },
    variants = variants,
)