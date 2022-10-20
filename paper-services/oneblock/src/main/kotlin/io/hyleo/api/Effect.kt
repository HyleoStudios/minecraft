package io.hyleo.api

import org.bukkit.potion.PotionEffectType

data class Effect(
    val type: PotionEffectType,
    val duration: Int,
    val ambient: Boolean = false,
    val particles: Boolean = true,
    val icon: Boolean = true,
)