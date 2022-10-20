package io.hyleo.api

import io.hyleo.api.probability.Weight
import net.kyori.adventure.sound.Sound

open class Noise(
    weight: Weight,
    occurrences: Map<Phase, Float>,
    val sound: Sound,
) : Balanceable<Phase>(weight = weight, occurrences = occurrences)