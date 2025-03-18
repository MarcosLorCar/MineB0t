package me.orange.game.world.chunk

import me.orange.game.utils.Pos

abstract class ChunkGenerator(val seed: Long) {
    abstract fun generateChunk(pos: Pos): Chunk
}