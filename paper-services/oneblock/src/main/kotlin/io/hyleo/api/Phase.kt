package io.hyleo.api

import net.kyori.adventure.bossbar.BossBar.Color
import net.kyori.adventure.text.format.TextColor

open class Phase(
    val name: String,
    val displayName: String,
    val textColor: TextColor,
    val barColor: Color,
)
