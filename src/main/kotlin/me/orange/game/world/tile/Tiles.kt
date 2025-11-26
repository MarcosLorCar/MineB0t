package me.orange.game.world.tile

import me.orange.game.inventory.ItemType

object Tiles {
    val registry = mutableListOf<Tile>()
    val idMap = mutableMapOf<String, Int>()

    // Tiles
    val NULL = register("null") { }
    val AIR = register("air") { airy() }
    val DIRT = register("dirt") {
        breakable()
        drops(ItemType.DIRT, 1)
    }
    val GRASS = register("grass") {
        breakable()
        drops(ItemType.GRASS, 1)
    }
    val STONE = register("stone") {
        breakable()
        drops(ItemType.STONE, 1)
    }
    val IRON_ORE = register("iron_ore") {
        breakable()
        drops(ItemType.IRON_CHUNK, 1)
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