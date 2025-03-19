package me.orange.game.world.chunk

import me.orange.game.utils.Vec

abstract class ChunkGenerator(val seed: Long) {
    abstract fun generateChunk(vec: Vec): Chunk
}