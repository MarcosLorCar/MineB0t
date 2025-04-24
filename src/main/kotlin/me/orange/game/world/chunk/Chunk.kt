package me.orange.game.world.chunk

import kotlinx.serialization.Serializable
import me.orange.game.utils.Vec
import me.orange.game.world.TileType

@Serializable
data class Chunk(
    val worldPos: Vec,
    val tiles: MutableList<MutableList<TileType>> = MutableList(SIZE) { MutableList(SIZE) { TileType.AIR } }
) {
    var decorated = false

    fun getTile(vec: Vec): TileType? = tiles.getOrNull(vec.y)?.getOrNull(vec.x)
    fun setTile(vec: Vec, tile: TileType) {
        tiles[vec.y][vec.x] = tile
    }

    companion object {
        const val SIZE = 16

        fun nullChunk(vec: Vec): Chunk {
            val tiles = MutableList(SIZE) { MutableList(SIZE) { TileType.NULL } }
            return Chunk(vec, tiles)
        }
    }
}