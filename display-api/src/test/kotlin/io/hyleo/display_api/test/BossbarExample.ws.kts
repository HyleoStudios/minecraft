package io.hyleo.display_api.test

import io.hyleo.display_api.Display
import io.hyleo.display_api.Timings
import io.hyleo.display_api.display_factories.bossbar.BossbarFactory
import io.hyleo.display_api.text.TextAnimation
import io.hyleo.display_api.text.TextPatterns
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.bossbar.BossBar.Color
import net.kyori.adventure.bossbar.BossBar.Overlay
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

val bossBar: Display<Audience, BossBar, TextAnimation, Component> = BossbarFactory.textDisplay { true }

val audience = object : Audience {} // This could be a player, world, server, etc
val slot = BossBar.bossBar(Component.empty(), 1.0f, Color.RED, Overlay.PROGRESS)

val text = TextAnimation(
    timings = Timings(
        interval = 1,
        delay = 10
    ),
    pattern = TextPatterns.FLASH,
    colors = listOf(NamedTextColor.DARK_RED, NamedTextColor.YELLOW),
    depth = 20,
    text = { "This is how you use Display API" }
)

bossBar.display(receiver = audience, slot = slot, animations = arrayOf(text))
