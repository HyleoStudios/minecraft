package io.hyleo.examples

import io.hyleo.api.Phase
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component

object Undergrowth : Phase(
    name = "Undergrowth",
    displayName = Component.text("Undergrowth"),
    barColor = BossBar.Color.BLUE,
)