package io.hyleo.proxy.display

interface Stateful<State> {
    fun state(): State
}