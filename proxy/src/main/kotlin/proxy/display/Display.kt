package io.hyleo.proxy.display

import com.google.common.collect.Range
import net.kyori.adventure.audience.Audience

data class Display<Receiver : Audience, Slot, Type : Animation<State, Frame>, State, Frame : Any>(
    val intervalSupport: Boolean,

    val setup: (Receiver) -> Unit = {},
    val teardown: (Receiver) -> Unit = {},
    val condition: (Receiver) -> Boolean,

    val create: (Receiver, Map<Slot, Frame>) -> List<Slot> = { _, _ ->
        listOf()
    },
    val update: (Receiver, Map<Slot, Frame>) -> List<Slot> = { _, _ ->
        listOf()
    },
    val destroy: (Receiver, List<Slot>) -> List<Slot> = { _, _ ->
        listOf()
    },
) {

    private val setups = mutableListOf<Receiver>()

    private val animations = mutableMapOf<Receiver, MutableMap<Slot, Type>>()
    private val frames = mutableMapOf<Receiver, MutableMap<Slot, Int>>()
    private val timings = mutableMapOf<Receiver, MutableMap<Slot, Timings>>()

    private val animationStates = mutableMapOf<Receiver, MutableMap<Slot, State>>()
    private val timingsStates = mutableMapOf<Receiver, MutableMap<Slot, TimingsState>>()

    private val repeats = mutableMapOf<Receiver, MutableMap<Slot, Int>>()
    private val toHide = mutableMapOf<Receiver, MutableList<Slot>>()

    fun display(receiver: Receiver, slot: Slot, times: Timings, animation: Type) {
        val animations = animations.getOrPut(receiver) { mutableMapOf() }
        val timings = timings.getOrPut(receiver) { mutableMapOf() }
        animations[slot] = animation
        timings[slot] = times
    }

    fun hide(receiver: Receiver, vararg slot: Slot) {
        val toHide = toHide.getOrPut(receiver) { mutableListOf() }
        toHide.addAll(if (slot.isEmpty()) animations[receiver]?.keys ?: emptyList() else slot.toMutableList())
    }

    fun display() {
        for (displays in animations) {
            val receiver = displays.key
            val display = displays.value

            if (!condition(receiver)) {
                hide(receiver)
                setups.remove(receiver)
                continue
            }

            if (!setups.contains(receiver)) {
                setup.invoke(receiver)
                setups.add(receiver)
            }

            receive(receiver, display)
        }
    }

    private fun receive(receiver: Receiver, display: Map<Slot, Type>) {

        val timingsStates = mutableMapOf<Slot, TimingsState>()

        val creations = mutableMapOf<Slot, Frame>()
        val decimations = mutableListOf<Slot>()
        val updates = mutableMapOf<Slot, Frame?>()

        for (receiving in display) {
            val slot = receiving.key
            val animation = receiving.value
            val timingsState = timingsState(receiver, slot)
            val animationState = animationState(receiver, slot)
            val frames = frames(receiver, slot)

            timingsStates[slot] = timingsState

            if (shouldCreate(receiver, slot)) {
                println("creating")
                creations[slot] = animation.animate(animationState, frames, if (timingsState.reverse) frames - 1 else 0)
                repeats.getOrPut(receiver) { mutableMapOf() }[slot] = 0
            } else if (shouldDestroy(receiver, slot, timingsState)) {
                println("destroying")
                decimations.add(slot)
            } else {
                updates[slot] = frame(receiver, slot, timingsState)
            }
        }
        if (creations.isNotEmpty()) {
            handleTicking(create.invoke(receiver, creations), timingsStates)
        }
        if (updates.isNotEmpty()) {
            handleTicking(destroy.invoke(receiver, decimations), timingsStates)
        }
        if (decimations.isNotEmpty()) {
            handleTicking(update.invoke(receiver, updates.filter { e -> e.value != null } as Map<Slot, Frame>),
                timingsStates)
        }

    }

    private fun animation(receiver: Receiver, slot: Slot): Type? {
        val animations = animations.getOrPut(receiver) { mutableMapOf() }
        return animations[slot]
    }

    private fun animationState(receiver: Receiver, slot: Slot): State {
        val animationStates = animationStates.getOrPut(receiver) { mutableMapOf() }
        return animationStates.getOrPut(slot) { animation(receiver, slot)!!.state() }
    }

    private fun timings(receiver: Receiver, slot: Slot): Timings? {
        val timings = timings.getOrPut(receiver) { mutableMapOf() }
        return timings[slot]
    }

    private fun timingsState(receiver: Receiver, slot: Slot): TimingsState {
        val timingsState = timingsStates.getOrPut(receiver) { mutableMapOf() }
        return timingsState.getOrPut(slot) { timings(receiver, slot)!!.state() }
    }

    private fun repeats(receiver: Receiver, slot: Slot): Int {
        val repeats = repeats.getOrPut(receiver) { mutableMapOf() }
        return repeats.getOrPut(slot) { -1 }
    }

    private fun shouldCreate(receiver: Receiver, slot: Slot): Boolean {
        return !toHide.getOrPut(receiver) { mutableListOf() }.contains(slot) && repeats(receiver, slot) == -1
    }

    private fun timeout(state: TimingsState): Boolean {
        return !state.infinite() && state.ticks >= state.timeout!!
    }

    private fun timeoutCycle(state: TimingsState): Boolean {
        return state.hasCycleTimeout() && state.ticks >= state.cycleTimeout!!
    }

    private fun shouldDestroy(receiver: Receiver, slot: Slot, state: TimingsState): Boolean {
        println(repeats(receiver, slot))
        println(state.repeats)
        return toHide[receiver]?.contains(slot) == true || (!state.infinite() && repeats(
            receiver,
            slot
        ) >= state.repeats!!)
    }

    private fun frames(receiver: Receiver, slot: Slot): Int {
        val animation = animation(receiver, slot)
        val state = animationState(receiver, slot)
        val frames = frames.getOrPut(receiver) { mutableMapOf() }
        return frames.getOrPut(slot) { animation!!.frames(state) }
    }

    private fun cycleLength(receiver: Receiver, slot: Slot, state: TimingsState): Int {

        val repeated = repeats(receiver, slot)
        var offset = 0

        if (repeated == 0) {
            offset += state.delay
        }

        if (state.infinite() && repeated == state.repeats?.minus(1)) {
            offset = state.finalDelay
        } else {
            offset += state.repeatDelay
        }

        return frames(receiver, slot) * state.interval + offset
    }

    private fun handleTicking(skipTick: List<Slot>, states: Map<Slot, TimingsState>) {
        states.forEach() { (s, ts) -> if (!skipTick.contains(s)) ts.tick() }
    }

    private fun frame(receiver: Receiver, slot: Slot, state: TimingsState): Frame? {
        val animation = animation(receiver, slot)
        val animationState = animationState(receiver, slot)
        val cycleFrames = frames(receiver, slot)
        val cycleLength = cycleLength(receiver, slot, state)
        val repeated = repeats(receiver, slot)

        if (state.ticks == cycleLength || timeoutCycle(state)) {
            //
            // println("end of cycle")
            println(state.ticks)
            animationStates[receiver]?.set(slot, animation!!.state())
            timingsStates[receiver]?.set(slot, timings(receiver, slot)!!.state())
            frames[receiver]?.set(slot, animation!!.frames(animationState))
            repeats[receiver]?.set(slot, repeated)
            return frame(receiver, slot, state)
        }

        if (delay(repeated, state)) {
            println("delay")
            return if (intervalSupport) null else animation!!.animate(
                animationState,
                cycleFrames,
                if (state.reverse) cycleFrames - 1 else 0
            )
        }

        if (finalDelay(state, cycleLength, repeated) || repeatDelay(state, cycleLength)) {
            println("final delay or repeat delay")
            return if (intervalSupport) null else animation!!.animate(
                animationState,
                cycleFrames,
                if (state.reverse) 0 else cycleFrames - 1
            )
        }
        println("normal frame")

        return if (state.ticks % state.interval != 0 && intervalSupport) null else animation!!.animate(
            animationState,
            cycleFrames,
            state.ticks / state.interval
        )
    }

    private fun delay(repeated: Int, state: TimingsState): Boolean {
        return repeated == -1 && 0 > state.ticks - state.delay
    }

    private fun finalDelay(state: TimingsState, cycleLength: Int, repeated: Int): Boolean {
        return if (state.infinite()) {
            false
        } else repeated == state.repeats?.minus(1) && Range.closedOpen(cycleLength - state.finalDelay, cycleLength)
            .contains(state.ticks)
    }

    private fun repeatDelay(state: TimingsState, cycleLength: Int): Boolean {
        return Range.closedOpen(cycleLength - state.repeatDelay, cycleLength).contains(state.ticks)
    }

}