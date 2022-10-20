package io.hyleo.display_api

class Supply<T>(
    timings: Timings,
    private val supply: () -> T,
    logicalStates: MutableMap<(Int) -> Boolean, () -> Supply<T>> = mutableMapOf()
) : Animation<Supply<T>, T>(timings, logicalStates) {

    override fun frames(): Int {
        return 1
    }

    override fun frame(frames: Int, frame: Int): T {
        return supply()
    }
}