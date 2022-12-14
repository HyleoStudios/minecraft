package io.hyleo.display_api.text

import io.hyleo.display_api.Animation
import io.hyleo.display_api.Timings
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import kotlin.math.max
import kotlin.math.min

data class TextAnimation(
    override val timings: Timings,
    val pattern: TextPattern,
    val preText: () -> Component = { Component.empty() },
    val text: () -> String,
    val subText: () -> Component = { Component.empty() },
    val colors: List<TextColor>,
    val decorations: List<TextDecoration> = listOf(),
    val depth: Int,
    val logicalStates: Map<(Int) -> Boolean, () -> TextAnimation> = mutableMapOf(),
) : Animation<TextAnimation, Component>(timings, logicalStates) {

    private var currentText = ""

    fun currentText() = currentText

    override fun frames(): Int {
        currentText = text()
        return pattern.frames(totalColors(), currentText.length)
    }

    override fun frame(frames: Int, frame: Int): Component {
        var text = pattern.text(this, frames, frame)
        decorations.forEach { text = text.decorate(it) }

        return Component.textOfChildren(preText.invoke(), text, subText.invoke())
    }

    fun totalColors(): Int {
        return colors.size * depth
    }

    fun color(color: Int): TextColor {
        val color1: TextColor = colors[firstColor(color)]
        val color2: TextColor = colors[secondColor(colors.size, color)]
        val red = color(
            color % depth, color1, color2
        ) { obj: TextColor -> obj.red() }
        val green = color(
            color % depth, color1, color2
        ) { obj: TextColor -> obj.green() }
        val blue = color(
            color % depth, color1, color2
        ) { obj: TextColor -> obj.blue() }
        return TextColor.color(red, green, blue)
    }

    private fun firstColor(color: Int): Int {
        return color / depth
    }

    private fun secondColor(colors: Int, color: Int): Int {
        return (color + depth) / depth % colors
    }

    private fun distance(color1: TextColor, color2: TextColor, function: (color: TextColor) -> Int): Int {
        val x1 = function.invoke(color1)
        val x2 = function.invoke(color2)
        return (max(x1, x2) - min(x1, x2)) / depth
    }

    private fun direction(color1: TextColor, color2: TextColor, function: (color: TextColor) -> Int): Int {
        return function.invoke(color2).compareTo(function.invoke(color1))
    }

    private fun color(
        color: Int, color1: TextColor, color2: TextColor, function: (color: TextColor) -> Int
    ): Int {
        return (function.invoke(color1)
                + color * distance(color1, color2, function) * direction(color1, color2, function))
    }


}