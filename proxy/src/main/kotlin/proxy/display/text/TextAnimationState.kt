package io.hyleo.proxy.display.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import kotlin.math.max
import kotlin.math.min

data class TextAnimationState(
    val pattern: TextPattern,
    val preText: Component,
    val text: String,
    val subText: Component,
    val colors: List<TextColor>,
    val decorations: List<TextDecoration>,
    val depth: Int,
) {
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