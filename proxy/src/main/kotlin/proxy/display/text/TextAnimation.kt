package io.hyleo.proxy.display.text

import io.hyleo.proxy.display.Animation
import io.hyleo.proxy.display.Stateful
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration


data class TextAnimation(
    val pattern: () -> TextPattern,
    val preText: () -> Component = { Component.empty() },
    val text: () -> String,
    val subText: () -> Component = { Component.empty() },
    val colors: () -> List<TextColor>,
    val decorations: () -> List<TextDecoration> = { listOf() },
    val depth: () -> Int
) : Animation<TextAnimationState, Component> {

    override fun frames(state: TextAnimationState): Int {
        return state.pattern.frames(state.totalColors(), state.text.length)
    }

    override fun animate(state: TextAnimationState, frames: Int, frame: Int): Component {
        return state.pattern.text(state, frames, frame)
    }

    override fun state(): TextAnimationState {
        return TextAnimationState(
            pattern = pattern(),
            preText = preText(),
            text = text(),
            subText = subText(),
            colors = colors(),
            decorations = decorations(),
            depth = depth()
        )
    }

}