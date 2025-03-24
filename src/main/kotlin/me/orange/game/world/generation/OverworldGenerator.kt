package me.orange.game.world.generation

import de.articdive.jnoise.core.api.functions.Interpolation
import de.articdive.jnoise.generators.noise_parameters.fade_functions.FadeFunction
import de.articdive.jnoise.pipeline.JNoise
import me.orange.game.utils.Vec
import me.orange.game.world.TileType
import me.orange.game.world.chunk.Chunk
import kotlin.math.floor

class OverworldGenerator(seed: Int) : ChunkGenerator(seed) {
    val noise = JNoise.newBuilder()
        .perlin(
            seed.toLong(),
            Interpolation.COSINE,
            FadeFunction.QUINTIC_POLY
        ).build()

    override fun generateChunk(chunkVec: Vec): Chunk {
        val tiles = MutableList(Chunk.Companion.SIZE) { MutableList(Chunk.Companion.SIZE) { TileType.NULL } }

        for (x in 0 until Chunk.Companion.SIZE) {
            val worldX = x + chunkVec.x * Chunk.Companion.SIZE
            val height = heightMap(worldX)

            for (y in 0 until Chunk.Companion.SIZE) {
                val worldVec = Vec(x, y).toWorldPos(chunkVec)
                when {
                    (worldVec.y == height) -> tiles[y][x] = TileType.GRASS

                    (worldVec.y < height) -> tiles[y][x] = TileType.DIRT

                    else -> tiles[y][x] = TileType.AIR
                }
            }
        }

        return Chunk(chunkVec, tiles)
    }

    fun heightMap(x: Int): Int {
        return floor(noise.evaluateNoise(x / 20.0) * 10).toInt()
    }
}