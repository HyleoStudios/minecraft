package io.hyleo.display_api

import net.kyori.adventure.audience.Audience

data class Display<Receiver : Audience, Slot, Type : Animation<Type, Frame>, Frame : Any>(

    private val intervalSupport: Boolean,
    private val setup: (Receiver) -> Unit = {},
    private val teardown: (Receiver) -> Unit = {},
    private val condition: (Receiver) -> Boolean,

    private val create: (Receiver, List<Slot>) -> List<Slot> = { _, _ ->
        listOf()
    },
    private val update: (Receiver, Map<Slot, Frame>) -> List<Slot> = { _, _ ->
        listOf()
    },
    val destroy: (Receiver, List<Slot>) -> List<Slot> = { _, _ ->
        listOf()
    },
) {

    private val setups = mutableListOf<Receiver>()
    private val toHide = mutableMapOf<Receiver, MutableList<Slot>>()

    private val animations = mutableMapOf<Receiver, MutableMap<Slot, Sequence<Type>>>()
    private val conditions = mutableMapOf<Receiver, MutableMap<Slot, (Receiver, Slot) -> Boolean>>()

    fun display(
        receiver: Receiver,
        slot: Slot,
        condition: (Receiver, Slot) -> Boolean = { _, _ -> true },
        vararg animations: Type,
    ) {

        if (!setups.contains(receiver)) {
            setup(receiver)
            setups.add(receiver)
        }

        this.animations.getOrPut(receiver) { mutableMapOf() }[slot] = animations.asSequence()

        animations.forEach {
            it.setState(0)
            it.timings.setState(it.frames())
        }

        conditions.getOrPut(receiver) { mutableMapOf() }[slot] = condition
    }

    fun hide(receiver: Receiver, vararg slot: Slot) {
        val toHide = toHide[receiver]
        toHide?.addAll(if (slot.isEmpty()) animations[receiver]?.keys ?: emptyList() else slot.toMutableList())
    }


    fun tick(): Map<Receiver, Map<Slot, Frame>> {

        val view = mutableMapOf<Receiver, Map<Slot, Frame>>()

        for (displays in animations) {
            val receiver = displays.key
            val display = displays.value
            val teardown = !condition(receiver)

            if (teardown) {
                hide(receiver)
            }

            for (slot in display.keys) {
                if (conditions[receiver]?.get(slot)?.invoke(receiver, slot) == true) {
                    continue
                }
                hide(receiver, slot)
            }

            view[receiver] = receive(receiver, display.keys.toList())

            if (teardown) {
                doTeardown(receiver)
            }
        }

        return view
    }

    private fun doTeardown(receiver: Receiver) {
        teardown(receiver)
        setups.remove(receiver)
        toHide.remove(receiver)
        animations.remove(receiver)
        conditions.remove(receiver)
    }

    private fun doDestroy(receiver: Receiver, slot: Slot) {
        animations[receiver]?.remove(slot)
        conditions[receiver]?.remove(slot)
    }

    private fun receive(receiver: Receiver, slots: List<Slot>): Map<Slot, Frame> {

        val view = mutableMapOf<Slot, Frame>()

        val creations = mutableListOf<Slot>()
        val decimations = mutableListOf<Slot>()
        val updates = mutableMapOf<Slot, Frame>()

        for (slot in slots) {
            val animation = animation(receiver, slot)
            val timings = animation?.getState()?.timings

            if (animations[receiver]?.get(slot)?.first()?.timings?.ticks() == -1) {
                println("creating")
                creations.add(slot)
                continue
            }

            if (toHide[receiver]?.contains(slot) == true || animation == null) {
                println("destroying")
                decimations.add(slot)
                continue
            }

            while (timings!!.getState().nextCycle()) {
                setNextStates(receiver, slot)
            }

            val frame = timings.getState().frame(intervalSupport)
            val animatedFrame = frame?.let { animation.getState().animate(frame) }

            animatedFrame?.let { updates[slot] = animatedFrame }
            animatedFrame?.let { view[slot] = it }

            if (animatedFrame == null) {
                animation.timings.tick()
            }

        }

        val skip = mutableListOf<Slot>()

        skip += if (creations.isNotEmpty()) create(receiver, creations) else emptyList()
        skip += if (decimations.isNotEmpty()) destroy(receiver, decimations) else emptyList()
        skip += if (updates.isNotEmpty()) update(receiver, updates) else emptyList()

        slots.filterIndexed { _, it -> !skip.contains(it) }.forEach { s -> animation(receiver, s)?.timings?.tick() }
        (creations + decimations).forEach { s -> setNextStates(receiver, s) }

//        creations.filter { s -> timings(receiver, s)!!.delay == 0 }
//            .forEach { s -> setNextStates(receiver, s) }

        decimations.forEach { s -> doDestroy(receiver, s) }

        return view
    }


    private fun animation(receiver: Receiver, slot: Slot): Type? {
        return animations[receiver]?.get(slot)?.find { !it.timings.getState().complete() }
    }

    private fun setNextStates(receiver: Receiver, slot: Slot) {
        val animation = animation(receiver, slot)
        val timings = animation?.timings

        animation?.setState(timings!!.repeated() + 1)
        val animationState = animation?.getState()

        animationState?.timings?.setState(animationState.frames())
    }

}