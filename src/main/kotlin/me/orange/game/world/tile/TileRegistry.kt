package me.orange.game.world.tile

object TileRegistry {
    private val tiles = mutableMapOf<Int, Tile>()
    private val keyMap = mutableMapOf<String, Int>()

    fun register(key: String, block: Tile.Builder.() -> Unit): Tile {
        val id = key.hashCode()
        require(!tiles.containsKey(id)) { "Tile key '$key' hashes to ID $id which is already taken by '${tiles[id]?.key}'" }
        val tile = Tile.Builder(key).apply(block).build(id)
        tiles[id] = tile
        keyMap[tile.key] = id
        return tile
    }

    fun getID(key: String) = keyMap[key] ?: error("Unknown tile key '$key'")
    fun getTile(id: Int): Tile? = tiles[id]
    val count: Int get() = tiles.size
}
