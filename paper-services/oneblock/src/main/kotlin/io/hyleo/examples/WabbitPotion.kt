package io.hyleo.examples

import io.hyleo.api.Effect
import io.hyleo.api.item.PotionItem
import io.hyleo.api.item.PotionType
import io.hyleo.api.probability.Weight
import org.bukkit.Color
import org.bukkit.potion.PotionEffectType

object WabbitPotion : PotionItem(
    weight = Weight(
        quality = 10,
        weight = 1.0,
    ),
    occurrences = mapOf(),
    type = PotionType.DRINKABLE,
    color = Color.GREEN,
    effects = mapOf(
        Effect(type = PotionEffectType.JUMP, duration = 20 * 60 * 5) to listOf(
            Weight(quality = 1, weight = 1.0),
            Weight(quality = 2, weight = 1.0),
            Weight(quality = 3, weight = 1.0),
        ),
        Effect(type = PotionEffectType.SPEED, duration = 20 * 60 * 3) to listOf(
            Weight(quality = 1, weight = 1.0),
            Weight(quality = 2, weight = 1.0),
        ),
    ),
    weighRandomQuantities = { t, n -> t / (t - (n * 0.5f)) },
    randomEffects = mapOf(
        Effect(type = PotionEffectType.BLINDNESS, duration = 20 * 10) to listOf(),
        Effect(type = PotionEffectType.POISON, duration = 20 * 60) to listOf(
            Weight(quality = 1, weight = 1.0),
            Weight(quality = 2, weight = 1.0),
        ),
    )
)