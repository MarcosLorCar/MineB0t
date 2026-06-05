package me.orange.game.world.tile

import me.orange.game.craft.CraftingStationType
import me.orange.game.inventory.Items

object Tiles {
    val NULL = TileRegistry.register("null") {
        airy()
    }
    val AIR = TileRegistry.register("air") {
        airy()
    }
    val DIRT = TileRegistry.register("dirt") {
        breakable()
        drops(Items.DIRT, 1)
        variant("dirt_1")
        variant("dirt_2")
    }
    val GRASS = TileRegistry.register("grass") {
        breakable()
        drops(Items.GRASS, 1)
        variant("grass_1")
        variant("grass_2")
    }
    val STONE = TileRegistry.register("stone") {
        breakable()
        drops(Items.STONE, 1)
        variant("stone_2")
    }
    val IRON_ORE = TileRegistry.register("iron_ore") {
        breakable()
        drops(Items.IRON_CHUNK, 1)
        variant("iron_ore_1")
    }
    @Suppress("unused")
    val CRAFTING_TABLE = TileRegistry.register("crafting_table") {
        breakable()
        drops(Items.CRAFTING_TABLE, 1)
        craftingStation(CraftingStationType.CRAFTING_TABLE)
    }
    val FURNACE = TileRegistry.register("furnace")       {
        breakable()
        drops(Items.FURNACE, 1)
        craftingStation(CraftingStationType.FURNACE)
    }
    val COAL_ORE = TileRegistry.register("coal_ore") {
        breakable()
        drops(Items.COAL, 1)
        variant("coal_ore_1")
    }
    val RED_SHROOM = TileRegistry.register("red_shroom_tile") {
        airy()
        breakable()
        drops(Items.RED_SHROOM)
        variant("red_shroom_tile_1")
    }

    val count: Int get() = TileRegistry.count
}
