package io.hyleo.proxy.display

interface Animation<State, Frame> : Stateful<State> {

    fun frames(state: State): Int

    fun animate(state: State, frames:Int, frame: Int): Frame
}