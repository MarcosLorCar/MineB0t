package me.orange.game.world.chunk

import me.orange.game.utils.Pos
import me.orange.game.world.TileType
import me.orange.game.world.chunk.Chunk.Companion.SIZE

class OverworldGenerator(seed: Long) : ChunkGenerator(seed) {
    override fun generateChunk(chunkPos: Pos): Chunk {
        val tiles = MutableList(SIZE) { MutableList(SIZE) { TileType.NULL } }

        for (y in 0 until SIZE) {
            for (x in 0 until SIZE) {
                val worldPos = Pos(x, y).toWorldPos(chunkPos)

                when {
                    (worldPos.y == 0) -> tiles[y][x] = TileType.GRASS

                    (worldPos.y < 0) -> tiles[y][x] = TileType.DIRT

                    else -> tiles[y][x] = TileType.AIR
                }
            }
        }

        return Chunk(chunkPos, tiles)
    }
}