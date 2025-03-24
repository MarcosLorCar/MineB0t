package me.orange.game.world.generation

import me.orange.game.utils.Vec
import me.orange.game.world.chunk.Chunk

abstract class ChunkGenerator(val seed: Long) {
    abstract fun generateChunk(vec: Vec): Chunk
}