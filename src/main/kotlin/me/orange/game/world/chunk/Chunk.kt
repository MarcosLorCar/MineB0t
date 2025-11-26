package me.orange.game.world.chunk

import kotlinx.serialization.Serializable
import me.orange.game.utils.Vec
import me.orange.game.utils.safeGet
import me.orange.game.world.tile.Tile
import me.orange.game.world.tile.Tiles

@Serializable
data class Chunk(
    val worldPos: Vec,
    val tiles: MutableList<MutableList<Int>> = MutableList(SIZE) { MutableList(SIZE) { Tiles.AIR.id } }
) {
    var decorated = false

    fun getTile(vec: Vec): Tile? = tiles.safeGet(vec.y, vec.x)?.let { Tiles.getTile(it) }
    fun getTile(x: Int, y: Int): Tile? = tiles.safeGet(y, x)?.let { Tiles.getTile(it) }
    fun setTile(vec: Vec, tile: Tile) {
        tiles[vec.y][vec.x] = tile.id
    }

    companion object {
        const val SIZE = 16

        fun uniformChunk(vec: Vec, tile: Tile): Chunk {
            val tiles = MutableList(SIZE) { MutableList(SIZE) { tile.id } }
            return Chunk(vec, tiles)
        }
    }
}