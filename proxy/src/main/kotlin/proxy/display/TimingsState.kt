package io.hyleo.proxy.display

data class TimingsState(
    val reverse: Boolean,
    val delay: Int,
    val interval: Int,
    val repeats: Int?,
    val repeatDelay: Int,
    val finalDelay: Int,
    val timeout: Int?,
    val cycleTimeout: Int?,
) {
    var ticks: Int = 0
    var frames: Int = 0

    fun infinite() = repeats == null && timeout ==null
    fun hasCycleTimeout() = cycleTimeout != null

    fun tick() {
        ticks++
    }
}
