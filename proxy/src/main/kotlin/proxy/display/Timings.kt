package io.hyleo.proxy.display

data class Timings(
    val reverse: () -> Boolean = { false },
    val delay: Int = 0,
    val interval: () -> Int,
    val repeats: Int? = null,
    val repeatDelay: Int = 0,
    val finalDelay: Int = 0,
    val timeout: Int? = null,
    val cycleTimeout: Int? = null,
) : Stateful<TimingsState> {

    fun validDelay() = delay >= 0
    fun validRepeats() = repeats?.let { it > 0 } ?: true
    fun validTimeout() = timeout?.let { it > 0 } ?: true
    fun validCycleTimeout() = cycleTimeout?.let { it > 0 } ?: true
    fun hasTimeout() = timeout != null

    override fun state(): TimingsState {
        val reverse = reverse()
        return TimingsState(
            reverse,
            if(reverse) finalDelay else delay,
            interval(),
            repeats,
            repeatDelay,
            if(reverse) delay else finalDelay,
            timeout,
            cycleTimeout
        )
    }
}