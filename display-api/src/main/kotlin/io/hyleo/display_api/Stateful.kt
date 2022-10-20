package io.hyleo.display_api

abstract class Stateful<S : Stateful<S, Info>, Info>(private val logicalStates: Map<(Info) -> Boolean, () -> S> = mapOf()) {

    private var logic: ((Info) -> Boolean?)? = null

    private var state: S? = null

    @Suppress("UNCHECKED_CAST")
    fun getState() = (if (state == null) this else state!!) as S

    open fun setState(info: Info): S? {
        val previous = state
        setState(info, this as S).let { state = it }
        return previous
    }

    private fun setState(info: Info, default: S): S? {
        val first = logicalStates.entries.find { it.key(info) }

        if (first?.key == logic) {
            return state
        }

        return first?.value?.invoke() ?: default
    }

}