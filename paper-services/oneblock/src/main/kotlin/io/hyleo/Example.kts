package io.hyleo

import io.hyleo.api.item.Item
import io.hyleo.api.block.Block
import io.hyleo.api.Phase
import io.hyleo.api.probability.CoinFlip
import io.hyleo.api.probability.Weight
import io.hyleo.examples.UndergrowthBook
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.loot.LootContext

fun defaultData(material: Material) = { _: BlockBreakEvent, _: LootContext -> material.createBlockData() }

fun defaultEntity(type: EntityType): (BlockBreakEvent, LootContext) -> Entity =
    { e, _ -> e.block.world.spawnEntity(e.block.location, type) }

fun bosses(event: BlockBreakEvent, player: Player): Collection<Entity> = listOf()


val sounds: Map<io.hyleo.api.Noise, Weight> = mapOf(
    io.hyleo.api.Noise(sound = Sound.ENTITY_HORSE_GALLOP) to Weight(quality = 1, weight = 1.0),
)

val entities: Map<(BlockBreakEvent, LootContext) -> Entity, List<Weight>> = mapOf(
    defaultEntity(EntityType.COW) to listOf(Weight(quality = 1, weight = 10.0)),
    defaultEntity(EntityType.ZOMBIE) to listOf(Weight(quality = 1, weight = 1.0)),
)

object WildLoot : Digest(
    key = NamespacedKey.minecraft("WILD_LOOT"),
    rolls = listOf(Weight(quality = 4, weight = 5.0), Weight(quality = 6, weight = 2.0)),
    items = listOf(
        Item(
            noChain = true,
            amounts = listOf(Weight(quality = 1, weight = 5.0), Weight(quality = 5, weight = 1.0)),
            stack = { ItemStack(Material.APPLE) },
        ),
        Item(
            amounts = listOf(Weight(quality = 1, weight = 5.0), Weight(quality = 5, weight = 1.0)),
            stack = { ItemStack(Material.STICK) },
        ),
    )
)

val loot: Map<Loot, Weight> = mapOf(
    Loot(
        name = Component.text("Wild", NamedTextColor.YELLOW),
        digest = wildLoot
    ) to Weight(quality = 1, weight = 1.0),
)


val rewards = Digest(
    key = NamespacedKey.minecraft("REWARDS"),
    rolls = listOf(Weight(quality = 3, weight = 5.0), Weight(quality = 5, weight = 2.0)),
    chains = mapOf(
        wildLoot to Chain(
            rolls = listOf(
                Weight(quality = 1, weight = 3.0),
                Weight(quality = 3, weight = 1.0)
            )
        ),
    ),
    items = mapOf(UndergrowthBook to null,)
)

val blocks: Map<Block, Weight> = mapOf(
    Block(data = defaultData(Material.GRASS_BLOCK)) to Weight(quality = 1, weight = 100.0),
    Block(data = defaultData(Material.OAK_LOG)) to Weight(quality = 2, weight = 30.0),
    Block(
        data = defaultData(Material.COAL_ORE),
        xpDrops = listOf(Weight(quality = 1, weight = 5.0), Weight(quality = 2, weight = 1.0)),
    ) to Weight(quality = 3, weight = 20.0),
)

object ExamplePhase : Phase(
    name = "Example",
    displayName = Component.text("Example", NamedTextColor.YELLOW),
    barColor = BossBar.Color.GREEN,
    bossName = Component.text("Example Boss", NamedTextColor.RED),
    length = 1000,
    bosses = ::bosses,
    sounds = sounds,
    soundChance = CoinFlip(heads = Weight(quality = 1, weight = 1.0), tails = Weight(quality = 1, weight = 29.0)),
    entities = entities,
    entityChance = CoinFlip(heads = Weight(quality = 1, weight = 1.0), tails = Weight(quality = 1, weight = 49.0)),
    loot = loot,
    lootChance = CoinFlip(heads = Weight(quality = 1, weight = 1.0), tails = Weight(quality = 1, weight = 99.0)),
    rewards = rewards,
    blocks = blocks,
)
