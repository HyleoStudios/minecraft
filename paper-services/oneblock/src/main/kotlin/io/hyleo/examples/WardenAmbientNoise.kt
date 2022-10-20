package io.hyleo.examples

import io.hyleo.api.Noise
import io.hyleo.api.probability.Weight
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound

object WardenAmbientNoise : Noise(
    weight = Weight(quality = 2, weight = 1.0),
    occurences = mapOf(
        Undergrowth to 1.5f
    ),
    sound = Sound.sound(
        Key.key("entity.warden.ambient"),
        Sound.Source.AMBIENT,
        1.0f,
        1.0f,
    )
)
