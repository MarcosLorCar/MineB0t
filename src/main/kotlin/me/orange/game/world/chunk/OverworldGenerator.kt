package me.orange.game.world.chunk

import me.orange.game.utils.Vec
import me.orange.game.world.TileType
import me.orange.game.world.chunk.Chunk.Companion.SIZE

class OverworldGenerator(seed: Long) : ChunkGenerator(seed) {
    override fun generateChunk(chunkVec: Vec): Chunk {
        val tiles = MutableList(SIZE) { MutableList(SIZE) { TileType.NULL } }

        for (y in 0 until SIZE) {
            for (x in 0 until SIZE) {
                val worldVec = Vec(x, y).toWorldPos(chunkVec)

                when {
                    (worldVec.y == 0) -> tiles[y][x] = TileType.GRASS

                    (worldVec.y < 0) -> tiles[y][x] = TileType.DIRT

                    else -> tiles[y][x] = TileType.AIR
                }
            }
        }

        return Chunk(chunkVec, tiles)
    }
}