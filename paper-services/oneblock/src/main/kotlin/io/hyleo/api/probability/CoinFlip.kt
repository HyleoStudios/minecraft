package io.hyleo.api.probability

import org.bukkit.loot.LootContext
import kotlin.random.Random

data class CoinFlip(val heads: Weight, val tails: Weight) {

    companion object {
        val FAIR = CoinFlip(Weight(quality = 1, weight = 1.0), Weight(quality = 1, weight = 1.0))
    }

    fun flip(context: LootContext, tieBreaker: Boolean): Boolean {
        val headsAdjusted = heads.adjust(context)
        val tailsAdjusted = tails.adjust(context)

        val headsCondition = headsAdjusted.condition(context)
        val tailsCondition = tailsAdjusted.condition(context)

        if (!headsCondition && !tailsCondition) {
            return tieBreaker
        } else if (!headsCondition) {
            return false
        } else if (!tailsCondition) {
            return true
        }

        val headsGuaranteed = headsAdjusted.isGuaranteed()
        val tailsGuaranteed = tailsAdjusted.isGuaranteed()

        if (headsGuaranteed && tailsGuaranteed) {
            return tieBreaker
        } else if (headsGuaranteed) {
            return true
        } else if (tailsGuaranteed) {
            return false
        }

        val total = headsAdjusted.weight!! + tailsAdjusted.weight!!

        return Random.nextFloat() < headsAdjusted.weight / total
    }

}