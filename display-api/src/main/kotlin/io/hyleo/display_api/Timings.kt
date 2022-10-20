package io.hyleo.display_api

data class Timings(
    val reverse: Boolean = false,
    val delay: Int = 0,
    val interval: Int,
    val repeats: Int = 0,
    val repeatDelay: Int = 0,
    val finalDelay: Int = 0,
    val cycleTimeout: Int? = null,
    val logicalStates: MutableMap<(Int) -> Boolean, () -> Timings> = mutableMapOf(),
) : Stateful<Timings, Int>(logicalStates) {

    private var repeated = 0
    private var ticks = 0
    private var frames = 0

    fun repeated() = repeated
    fun ticks() = ticks
    fun frames() = frames

    internal fun tick() {
        ticks++
        val state = getState()
        if (state != this) {
            state.tick()
        }
    }


    fun validDelay() = delay >= 0
    fun validCycleTimeout() = cycleTimeout?.let { it > 0 } ?: true
    fun infinite() = repeats < 0
    fun hasCycleTimeout() = cycleTimeout != null

    override fun setState(info: Int): Timings? {
        val previous = super.setState(info)

        if (previous == null) {

            this.repeated = if (frames != 0) repeated else -2
            this.ticks = if (frames != 0) ticks else -1
            this.frames = info
        }
        repeat()

        val state = getState()
        if (state != this) {
            state.frames = info
        }

        return previous
    }

    private fun repeat() {

        println("repeated $repeated")
        repeated++
        val state = getState()
        if (state != this) {
            state.repeat()
            println("going to repeat the state")
        }
    }

    fun cycleLength(): Int {
        var offset = if (repeated() == 0) delay else 0

        if (!infinite() && repeated() == repeats - 1) {
            offset = finalDelay
            println("adding final delay $finalDelay")
        } else {
            offset += repeatDelay
        }

        return frames() * interval + offset
    }

    fun nextCycle() = ticks() == cycleLength() || timeoutCycle()

    fun delay(): Boolean {
        println("repeated @ delay: $repeated")
        return repeated == 0 && 0 > ticks() - delay
    }

    fun finalDelay() = !infinite() &&
            repeated == repeats.minus(1) && ticks() in cycleLength() - finalDelay..cycleLength()

    fun repeatDelay() = ticks() in (cycleLength() - repeatDelay)..cycleLength()

    fun timeoutCycle() = hasCycleTimeout() && ticks() >= cycleTimeout!!

    fun complete() = timeoutCycle() || (!infinite() && repeated() >= repeats && ticks() == cycleLength())


    fun frame(intervalSupport: Boolean): Int? {

        if (delay()) {
            println("delay")
            return if (intervalSupport) null else if (reverse) frames() - 1 else 0
        }

        if (finalDelay() || repeatDelay()) {
            println("final delay or repeat delay")
            return if (intervalSupport) null else if (reverse) 0 else frames() - 1
        }

        println("normal frame")
        return if (intervalSupport && ((ticks() - delay) % interval != 0)) null else (ticks() - delay) / interval
    }

}