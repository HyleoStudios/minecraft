package io.hyleo.display_api.text

import net.kyori.adventure.text.Component

data class TextPattern(
    val frames: (Int, Int) -> Int,
    val text: (TextAnimation, Int, Int) -> Component,
)