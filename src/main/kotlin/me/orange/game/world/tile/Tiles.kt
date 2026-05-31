package me.orange.game.world.tile

import me.orange.game.craft.CraftingStationType
import me.orange.game.inventory.Items

// IDs are positional — NEVER reorder, only append.
object Tiles {
    val NULL = TileRegistry.register("null") { }
    val AIR  = TileRegistry.register("air")  { airy() }
    val DIRT = TileRegistry.register("dirt") {
        breakable()
        drops(Items.DIRT, 1)
    }
    val GRASS = TileRegistry.register("grass") {
        breakable()
        drops(Items.GRASS, 1)
    }
    val STONE = TileRegistry.register("stone") {
        breakable()
        drops(Items.STONE, 1)
    }
    val IRON_ORE = TileRegistry.register("iron_ore") {
        breakable()
        drops(Items.IRON_CHUNK, 1)
    }
    @Suppress("unused")
    val CRAFTING_TABLE = TileRegistry.register("crafting_table") {
        breakable()
        drops(Items.CRAFTING_TABLE, 1)
        craftingStation(CraftingStationType.CRAFTING_TABLE)
    }
    @Suppress("unused")
    val FURNACE = TileRegistry.register("furnace") {
        breakable()
        drops(Items.FURNACE, 1)
        craftingStation(CraftingStationType.FURNACE)
    }

    val count: Int get() = TileRegistry.count
}
