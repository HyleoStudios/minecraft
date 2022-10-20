package io.hyleo.api.probability

import org.bukkit.Location
import org.bukkit.loot.LootContext
import kotlin.random.Random

data class Weight(val condition: (LootContext) -> Boolean = { true }, val quality: Int, val weight: Double) {

    companion object {
        fun <T> selectGuarantees(
            context: LootContext = LootContext.Builder(Location(null, 0.0, 0.0, 0.0)).build(),
            weights: Map<T, Weight>
        ): List<T> {

            val adjusted = adjustWeights(context = context, weights = weights)

            return adjusted.keys.toList()
        }

        fun <T> selectAtRandom(
            random: Random = Random,
            context: LootContext = LootContext.Builder(Location(null, 0.0, 0.0, 0.0)).build(),
            weights: Map<T, Weight>,
            amount: Int = 1,
            default: List<T> = emptyList(),
        ): List<T> {
            if (weights.isEmpty()) return listOf()

            val filtered = weights.toMap()
            val adjusted = adjustWeights(context = context, weights = filtered)

            val totalWeight = adjusted.values.stream().mapToDouble { it?.weight!! }.sum()
            var r: Double = random.nextDouble() * totalWeight

            val selection = mutableListOf<T>()

            for (i in 0 until amount) {
                for ((key, value) in adjusted) {
                    if (r <= value?.weight!!) {
                        selection.add(key)
                        break
                    }
                    r -= value.weight!!
                }
            }

            return if (selection.isEmpty()) default else selection
        }


        fun <T> adjustWeights(context: LootContext, weights: Map<T, Weight?>): Map<T, Weight?> {
            return weights.map { entry ->
                entry.key to entry.value!!.adjust(context)
            }.toMap()
        }
    }


    fun adjust(context: LootContext): Weight {
        val looting = if (context.lootedEntity == null) 0 else context.lootingModifier
        val quality = quality
        val adjusted = weight.let { it + quality * (looting + context.luck) }
        return Weight(quality = quality, weight = adjusted)
    }

}
