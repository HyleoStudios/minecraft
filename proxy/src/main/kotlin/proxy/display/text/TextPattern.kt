package io.hyleo.proxy.display.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import kotlin.math.floor

data class TextPattern(
    val frames: (Int, Int) -> Int,
    val text: (TextAnimationState, Int, Int) -> Component,
) {
    companion object {

        fun BUILD(inverse: Boolean = false): TextPattern {
            return TextPattern(
                frames = { colors, length -> colors * (length + 1) },
                text = { state, _, frame ->
                    val text = state.text
                    val textLength = text.length
                    val length = textLength + 1
                    val color = state.color(frame / length % state.colors.size)

                    val i: Int = frame % length
                    val spaces = " ".repeat(textLength - i)

                    return@TextPattern Component.text().color(color)
                        .append(if (inverse) Component.text(spaces) else Component.text(""))
                        .append(Component.text(text.substring(if (inverse) i else 0, if (inverse) length else i)))
                        .append(if (!inverse) Component.text(spaces) else Component.text("")).build()
                }
            )
        }

        fun DIALATE(inverse: Boolean = false, gap: Int = 4): TextPattern {
            return TextPattern(
                frames = { colors, _ -> gap * colors },
                text = { state, frames, f ->
                    val frame: Int = if (inverse) frames - f else f

                    val color: TextColor = state.color(frame % state.totalColors())
                    var text: StringBuilder = StringBuilder(state.text)

                    val chars = text.toString().toCharArray()

                    text = StringBuilder(chars[0].toString() + "")
                    val spacing = " ".repeat(frame % (4 + 1))

                    for (r in 1 until chars.size) {
                        text.append(spacing).append(chars[r])
                    }

                    return@TextPattern Component.text(text.toString()).color(color)
                }
            )
        }

        var FLASH = TextPattern(
            frames = { colors, _ -> colors },
            text = { state, _, frame -> Component.text(state.text).color(state.color(frame)) }
        )

        var REPLACE = TextPattern(
            frames = { colors, length -> colors * length + 1 },
            text = { state, frames, frame ->
                val text = state.text
                val colors = state.totalColors()
                val length = text.length

                val i: Int = frame % length

                if (frames - 1 == frame) {
                    return@TextPattern Component.text(text).color(state.color(0))
                }

                val color1: TextColor = state.color(floor(frame / length.toDouble()).toInt())
                val color2: TextColor = state.color((floor(frame / length.toDouble()).toInt() + 1) % colors)

                return@TextPattern Component.text(text.substring(0, i)).color(color2)
                    .append(Component.text(text.substring(i, length)).color(color1))
            }
        )

        var SWIPE = TextPattern(
            frames = { colors, length -> colors * (length + 2) },
            text = { state, _, frame ->

                val text = state.text
                val colors = state.totalColors()
                val length: Int = text.length + 2

                val i: Int = frame % length

                val color1: TextColor = state.color(floor(frame / length.toDouble()).toInt())
                val color2: TextColor = state.color((floor(frame / length.toDouble()).toInt() + 1) % colors)

                return@TextPattern if (i == length - 1 || i == 0) {
                    Component.text(text).color(color1)
                } else Component.text(text.substring(0, i - 1)).color(color1)
                    .append(Component.text(text.substring(i - 1, i)).color(color2))
                    .append(Component.text(text.substring(i)).color(color1))
            }
        )

        var SLIDE = TextPattern(
            frames = { colors, length -> colors + length },
            text = { state, _, frame ->
                val text = state.text

                val colors = state.totalColors()
                val length = text.length

                val builder = Component.text()

                for (i in 0 until length) {
                    var x = frame - (i - 1)
                    if (x < 0 || frame >= colors) {
                        x = 0
                    }
                    val color: TextColor = state.color(x % colors)
                    builder.append(Component.text(text[i]).color(color))
                }

                return@TextPattern builder.build()
            }
        )
    }
}