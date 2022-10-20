import io.hyleo.api.season.Holiday
import io.hyleo.api.season.Month
import io.hyleo.api.season.Season
import net.kyori.adventure.text.Component

val firstDayOfSchool = Holiday(name = Component.text("First Day of School"), period = 24..24)

val laborDay = Holiday(name = Component.text("Labor Day"), period = 5..5)
val harvestFest = Holiday(name = Component.text("Harvest Fest"), period = 23..30)

val halloween = Holiday(name = Component.text("Halloween"), period = 31..31)
val spookyWeek = Holiday(name = Component.text("Spooky Week"), period = 24..31)

val august = Month(name = Component.text("August"), days = 31, holidays = listOf(firstDayOfSchool))
val september = Month(name = Component.text("September"), days = 30, holidays = listOf(laborDay, harvestFest))
val october = Month(name = Component.text("October"), days = 31, holidays = listOf(spookyWeek, halloween))

val autumn = Season(name = Component.text("Fall"), months = listOf(august, september, october))
