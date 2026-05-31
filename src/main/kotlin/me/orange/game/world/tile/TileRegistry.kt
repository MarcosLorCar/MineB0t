package me.orange.game.world.tile

object TileRegistry {
    private val tiles = mutableListOf<Tile>()
    private val keyMap = mutableMapOf<String, Int>()

    fun register(key: String, block: Tile.Builder.() -> Unit): Tile {
        val id = tiles.size
        val tile = Tile.Builder(key).apply(block).build(id)
        tiles.add(tile)
        keyMap[tile.key] = id
        return tile
    }

    fun getID(key: String) = keyMap[key] ?: error("Unknown tile key '$key'")
    fun getTile(id: Int) = tiles.getOrNull(id) ?: error("Unknown tile ID '$id'")
    val count: Int get() = tiles.size
}
