package io.hyleo.display_api.display_factories.bossbar

import io.hyleo.display_api.Display
import io.hyleo.display_api.Supply
import io.hyleo.display_api.text.TextAnimation
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.bossbar.BossBar.Flag
import net.kyori.adventure.bossbar.BossBar.Overlay
import net.kyori.adventure.text.Component
import kotlin.math.min

class BossbarFactory {

    companion object {
        fun <A : Audience> create(): (A, List<BossBar>) -> List<BossBar> {
            return { a, s ->
                s.forEach { b -> a.showBossBar(b) }
                listOf()
            }
        }

        fun <A : Audience, F> update(u: (BossBar, F) -> Unit): (A, Map<BossBar, F>) -> List<BossBar> {
            return { a, s ->
                s.forEach { b -> u.invoke(b.key, b.value) }
                listOf()
            }
        }

        fun <A : Audience> destroy(): (A, List<BossBar>) -> List<BossBar> {
            return { a, s ->
                s.forEach { b -> a.hideBossBar(b) }
                listOf()
            }
        }

        fun <A : Audience> textDisplay(condition: (A) -> Boolean) = Display<A, BossBar, TextAnimation, Component>(
            intervalSupport = false,
            condition = condition,
            create = create(),
            update = update { b, f -> b.name(f) },
            destroy = destroy(),
        )

        fun <A : Audience> colorDisplay(condition: (A) -> Boolean) =
            Display<A, BossBar, Supply<BossBar.Color>, BossBar.Color>(
                intervalSupport = false,
                condition = condition,
                create = create(),
                update = update { b, f -> b.color(f) },
                destroy = destroy(),
            )

        fun <A : Audience> progressDisplay(condition: (A) -> Boolean) = Display<A, BossBar, Supply<Float>, Float>(
            intervalSupport = false,
            condition = condition,
            create = create(),
            update = update { b, f -> b.progress(min(1.0f, f)) },
            destroy = destroy(),
        )

        fun <A : Audience> overlayDisplay(condition: (A) -> Boolean) = Display<A, BossBar, Supply<Overlay>, Overlay>(
            intervalSupport = false,
            condition = condition,
            create = create(),
            update = update { b, f -> b.overlay(f) },
            destroy = destroy(),
        )

        fun <A : Audience> flagDisplay(condition: (A) -> Boolean) = Display<A, BossBar, Supply<List<Flag>>, List<Flag>>(
            intervalSupport = false,
            condition = condition,
            create = create(),
            update = update { b, f -> b.flags(f.toMutableSet()) },
            destroy = destroy(),
        )

    }

}

