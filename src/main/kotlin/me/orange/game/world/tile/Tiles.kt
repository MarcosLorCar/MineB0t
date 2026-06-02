package me.orange.game.world.tile

import me.orange.game.craft.CraftingStationType
import me.orange.game.inventory.Items

object Tiles {
    val NULL           = TileRegistry.register("null")          { }
    val AIR            = TileRegistry.register("air")           { airy() }
    val DIRT           = TileRegistry.register("dirt")          { breakable(); drops(Items.DIRT, 1) }
    val GRASS          = TileRegistry.register("grass")         { breakable(); drops(Items.GRASS, 1) }
    val STONE          = TileRegistry.register("stone")         { breakable(); drops(Items.STONE, 1) }
    val IRON_ORE       = TileRegistry.register("iron_ore")      { breakable(); drops(Items.IRON_CHUNK, 1) }
    @Suppress("unused")
    val CRAFTING_TABLE = TileRegistry.register("crafting_table") {
        breakable()
        drops(Items.CRAFTING_TABLE, 1)
        craftingStation(CraftingStationType.CRAFTING_TABLE)
    }
    val FURNACE        = TileRegistry.register("furnace")       {
        breakable()
        drops(Items.FURNACE, 1)
        craftingStation(CraftingStationType.FURNACE)
    }
    val COAL_ORE       = TileRegistry.register("coal_ore")      { breakable(); drops(Items.COAL, 1) }

    val count: Int get() = TileRegistry.count
}
