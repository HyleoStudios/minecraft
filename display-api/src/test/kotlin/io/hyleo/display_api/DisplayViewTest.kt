package io.hyleo.display_api

import io.hyleo.display_api.util.IntAnimation
import io.hyleo.display_api.util.DisplayViewData
import net.kyori.adventure.audience.Audience
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertContains
import kotlin.test.assertEquals

class DisplayViewTest {

    private val display = { intervalSupport: Boolean ->
        Display<Audience, Int, IntAnimation, Int>(
            intervalSupport = intervalSupport,
            condition = { true },
        )
    }


    @ParameterizedTest
    @MethodSource("testData")
    fun correctDisplay(data: DisplayViewData) {
        val audience = object : Audience {}
        val slot = 0

        val timings = data.timings
        val expected = data.expected()
        val display = display.invoke(data.intervalSupport)

        display.display(audience, slot, animations = arrayOf(IntAnimation(timings, data.frames)))

        for (expect in expected) {
            val view = display.tick()
            val slots = view[audience]
            assertContains(view, audience, "No view for audience $audience")
            val frame = slots?.get(slot)
            println("$expect : $frame")
            assertEquals(expect, frame)
        }
    }

    companion object {
        @JvmStatic
        fun testData(): Stream<DisplayViewData> {
            return Stream.of(
                DisplayViewData(
                    intervalSupport = false,
                    frames = 10,
                    timings = Timings(
                        interval = 1,
                    )
                ),
                DisplayViewData(
                    intervalSupport = false,
                    frames = 5,
                    timings = Timings(
                        interval = 2,
                        delay = 10,
                    )
                ),
                DisplayViewData(
                    intervalSupport = false,
                    frames = 5,
                    timings = Timings(
                        interval = 2,
                        delay = 10,
                        repeatDelay = 5,
                    )
                ),
                DisplayViewData(
                    intervalSupport = false,
                    frames = 5,
                    timings = Timings(
                        interval = 2,
                        delay = 10,
                        repeatDelay = 5,
                        finalDelay = 7,
                    )
                ),
                DisplayViewData(
                    intervalSupport = false,
                    frames = 5,
                    timings = Timings(
                        interval = 2,
                        repeats = 2,
                        delay = 10,
                        repeatDelay = 5,
                        finalDelay = 7,
                    )
                )
            )
        }
    }

}