package me.orange.game.world.generation

import me.orange.game.utils.Vec
import me.orange.game.world.chunk.Chunk
import me.orange.game.world.tile.Tile

abstract class ChunkGenerator(val seed: Long) {
    abstract fun generateChunk(chunkVec: Vec): Chunk
    open fun decorateCorner(cornerChunkPos: Vec, getTile: (Vec) -> Tile?, setTile: (Vec, Tile) -> Unit) {}
}
