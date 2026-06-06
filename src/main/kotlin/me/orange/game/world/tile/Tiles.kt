package me.orange.game.world.tile

import me.orange.bot.Emojis
import me.orange.game.craft.CraftingStationType
import me.orange.game.inventory.Items

object Tiles {
    val NULL: Tile = TileRegistry.register("null", Emojis.get("null")) {
        airy()
    }
    val AIR: Tile = TileRegistry.register("air", Emojis.get("air")) {
        airy()
    }
    val DIRT: Tile = TileRegistry.register("dirt", Emojis.get("dirt")) {
        breakable()
        drops(Items.DIRT, 1)
        variant(Emojis.get("dirt_1"))
        variant(Emojis.get("dirt_2"))
    }
    val GRASS: Tile = TileRegistry.register("grass", Emojis.get("grass")) {
        breakable()
        drops(Items.GRASS, 1)
        variant(Emojis.get("grass_1"))
        variant(Emojis.get("grass_2"))
    }
    val STONE: Tile = TileRegistry.register("stone", Emojis.get("stone")) {
        breakable()
        drops(Items.STONE, 1)
        variant(Emojis.get("stone_2"))
    }
    val IRON_ORE: Tile = TileRegistry.register("iron_ore", Emojis.get("iron_ore")) {
        breakable()
        drops(Items.IRON_CHUNK, 1)
        variant(Emojis.get("iron_ore_1"))
    }
    @Suppress("unused")
    val CRAFTING_TABLE: Tile = TileRegistry.register("crafting_table", Emojis.get("crafting_table")) {
        breakable()
        drops(Items.CRAFTING_TABLE, 1)
        craftingStation(CraftingStationType.CRAFTING_TABLE)
    }
    val FURNACE: Tile = TileRegistry.register("furnace", Emojis.get("furnace")) {
        breakable()
        drops(Items.FURNACE, 1)
        craftingStation(CraftingStationType.FURNACE)
    }
    val COAL_ORE: Tile = TileRegistry.register("coal_ore", Emojis.get("coal_ore")) {
        breakable()
        drops(Items.COAL, 1)
        variant(Emojis.get("coal_ore_1"))
    }
    val RED_SHROOM: Tile = TileRegistry.register("red_shroom_tile", Emojis.get("red_shroom_tile")) {
        airy()
        breakable()
        drops(Items.RED_SHROOM)
        variant(Emojis.get("red_shroom_tile_1"))
    }
    val CHEST: Tile = TileRegistry.register("chest", Emojis.get("chest")) {
        breakable()
        drops(Items.CHEST, 1)
        interaction(TileInteraction.Chest)
    }

    val count: Int get() = TileRegistry.count
}
