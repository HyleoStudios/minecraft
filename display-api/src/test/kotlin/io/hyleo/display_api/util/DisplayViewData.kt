package io.hyleo.display_api.util

import io.hyleo.display_api.Timings

data class DisplayViewData(val intervalSupport: Boolean, val timings: Timings, val frames: Int) {

    fun expected(): Array<Int?> {

        if (timings.repeats < 0) {
            throw IllegalArgumentException("Testing can not be done with an infinite number of repeats")
        }

        var expected = mutableListOf<Int?>()

        // Delay Frames
        for (i in 0 until timings.delay) {
            expected.add(if (intervalSupport) null else 0)
        }

        val interval = timings.interval

        // First cycle frames
        for (i in 0 until frames * interval) {
            expected.add(if (intervalSupport && i % interval == 0) i else i / interval)
        }

        // Repeated cycles
        for (i in 0 until timings.repeats) {
            // Repeat delay
            for (c in 0 until timings.repeatDelay) {
                expected.add(if (intervalSupport) null else frames)
            }
            // Cycle Frames
            for (f in 0 until frames * interval) {
                expected.add(if (intervalSupport && f % interval == 0) f else f / interval)
            }
        }

        // Final Delay frames
        for (i in 0 until timings.finalDelay) {
            expected.add(if (intervalSupport) null else frames)
        }

        // Must reverse if timings are reversed
        if (timings.reverse) {
            expected.reverse()
        }

        expected = (mutableListOf<Int?>(null) + expected) as MutableList<Int?> // Create Frame
        expected.add(null) // Destroy Frame

        return expected.toTypedArray()
    }
}
