package io.hyleo.display_api

abstract class Animation<A : Animation<A, Frame>, Frame>(
    open val timings: Timings,
    logicalStates: Map<(Int) -> Boolean, () -> A> = mapOf()
) :
    Stateful<A, Int>(logicalStates) {

    abstract fun frames(): Int

    abstract fun frame(frames: Int, frame: Int): Frame

    fun animate(frame: Int) = frame(timings.getState().frames(), frame)

}