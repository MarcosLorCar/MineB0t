package me.orange.game.world.chunk

import kotlinx.serialization.Serializable
import me.orange.game.utils.Pos
import me.orange.game.world.TileType

@Serializable
data class Chunk(
    val pos: Pos,
    val tiles: MutableList<MutableList<TileType>> = MutableList(SIZE) { MutableList(SIZE) { TileType.AIR } }
) {
    fun getTile(pos: Pos): TileType = tiles[pos.y][pos.x]
    fun setTile(pos: Pos, tile: TileType) {
        tiles[pos.y][pos.x] = tile
    }

    companion object {
        const val SIZE = 16

        fun nullChunk(pos: Pos): Chunk {
            val tiles = MutableList(SIZE) { MutableList(SIZE) { TileType.NULL } }
            return Chunk(pos, tiles)
        }
    }
}