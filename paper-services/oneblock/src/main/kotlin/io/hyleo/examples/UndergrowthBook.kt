package io.hyleo.examples

import io.hyleo.api.Register
import io.hyleo.api.item.EnchantedItem
import io.hyleo.api.probability.Weight
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

@Register
object UndergrowthBook : EnchantedItem(
    weight = Weight(
        quality = 4,
        weight = 5.0
    ),
    occurrences = mapOf(
       Undergrowth to 0.25f,
    ),
    stack = {
        ItemStack(Material.ENCHANTED_BOOK)
    },
    ignoreConflicts = true,
    enchantments = mapOf(
        Enchantment.DAMAGE_ALL to listOf(
            Weight(quality = 1, weight = 1.0),
            Weight(quality = 2, weight = 1.0)
        ),
    ),
    variants = mapOf(
        { i: ItemStack ->
            i.amount = 1
        } to Weight(quality = 1, weight = 5.0),
        { i: ItemStack ->
            i.amount = 3
        } to Weight(quality = 3, weight = 1.0),
    ),
    weighRandomQuantities = { t, n -> t / (t - (n * 2)) },
    randomEnchantments = mapOf(
        Enchantment.DAMAGE_ARTHROPODS to listOf(
            Weight(quality = 1, weight = 1.0),
            Weight(quality = 2, weight = 1.0)
        ),
        Enchantment.DAMAGE_UNDEAD to listOf(
            Weight(quality = 3, weight = 1.0),
            Weight(quality = 4, weight = 1.0)
        )
    ),
)


