package io.hyleo.display_api.util

import io.hyleo.display_api.Animation
import io.hyleo.display_api.Timings

class IntAnimation(
    timings: Timings,
    private val frames: Int,
    logicalStates: Map<(Int) -> Boolean, () -> IntAnimation> = mapOf(),
) : Animation<IntAnimation, Int>(timings, logicalStates) {

    override fun frames(): Int {
        return frames
    }

    override fun frame(frames: Int, frame: Int): Int {
        return frame
    }

}