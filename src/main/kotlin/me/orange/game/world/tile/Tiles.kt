package me.orange.game.world.tile

import me.orange.game.craft.CraftingStationType
import me.orange.game.inventory.Items

object Tiles {
    val registry = mutableListOf<Tile>()
    val idMap = mutableMapOf<String, Int>()

    // Tiles
    val NULL = register("null") { }
    val AIR = register("air") { airy() }
    val DIRT = register("dirt") {
        breakable()
        drops(Items.DIRT, 1)
    }
    val GRASS = register("grass") {
        breakable()
        drops(Items.GRASS, 1)
    }
    val STONE = register("stone") {
        breakable()
        drops(Items.STONE, 1)
    }
    val IRON_ORE = register("iron_ore") {
        breakable()
        drops(Items.IRON_CHUNK, 1)
    }
    // APPEND-ONLY below: tile IDs are positional, never reorder.
    // Placed via tileKey lookup, never referenced by name — registration is the only purpose.
    @Suppress("unused")
    val CRAFTING_TABLE = register("crafting_table") {
        breakable()
        drops(Items.CRAFTING_TABLE, 1)
        craftingStation(CraftingStationType.CRAFTING_TABLE)
    }
    @Suppress("unused")
    val FURNACE = register("furnace") {
        breakable()
        drops(Items.FURNACE, 1)
        craftingStation(CraftingStationType.FURNACE)
    }

    fun register(key: String, block: Tile.Builder.() -> Unit): Tile {
        val id = registry.size
        val tile = Tile.Builder(key).apply(block).build(id)
        registry.add(tile)
        idMap[tile.key] = id
        return tile
    }

    fun getID(key: String) = idMap[key] ?: error("Unknown tile key '$key'")
    fun getTile(id: Int) = registry.getOrNull(id) ?: error("Unknown tile ID '$id'")
}