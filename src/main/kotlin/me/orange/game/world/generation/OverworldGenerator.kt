package me.orange.game.world.generation

import de.articdive.jnoise.core.api.functions.Interpolation
import de.articdive.jnoise.generators.noise_parameters.fade_functions.FadeFunction
import de.articdive.jnoise.pipeline.JNoise
import me.orange.game.utils.Vec
import me.orange.game.world.TileType
import me.orange.game.world.World
import me.orange.game.world.chunk.Chunk
import kotlin.math.floor
import kotlin.random.Random

class OverworldGenerator(
    seed: Long,
    val world: World
) : ChunkGenerator(seed) {
    val noise = JNoise.newBuilder()
        .perlin(
            seed,
            Interpolation.COSINE,
            FadeFunction.QUINTIC_POLY
        ).build()

    val caveNoise = JNoise.newBuilder()
        .perlin(seed, Interpolation.COSINE, FadeFunction.QUINTIC_POLY)
        .scale(0.1)
        .build()

    companion object {
        const val STONE_LAYER_DEPTH = 4
        const val CAVE_THRESHOLD = 0.25
    }

    override fun generateChunk(chunkVec: Vec): Chunk {
        val tiles = MutableList(Chunk.Companion.SIZE) { MutableList(Chunk.Companion.SIZE) { TileType.NULL } }

        for (x in 0 until Chunk.Companion.SIZE) {
            val worldX = x + chunkVec.x * Chunk.Companion.SIZE
            val height = heightMap(worldX)

            for (y in 0 until Chunk.Companion.SIZE) {
                val worldVec = Vec(x, y).toWorldPos(chunkVec)

                val type = getTileType(worldVec, height)

                tiles[y][x] = type
            }
        }

        // For each chunk around this one *C, if all around *C are generated, decorate *C


        // If this chunk has all chunks around it generated, decorate it


        return Chunk(chunkVec, tiles)
    }

    private fun decorate(chunk: Chunk) {
        TODO("Not yet implemented")
    }

    private fun getTileType(worldVec: Vec, height: Int): TileType {
        var type = when {
            // The most superficial layer
            (worldVec.y == height) -> TileType.GRASS

            // Cave depth
            (worldVec.y < (height - STONE_LAYER_DEPTH)) -> TileType.STONE

            // Between surface and stone
            (worldVec.y < height) -> TileType.DIRT

            // Above surface
            else -> TileType.AIR
        }

        // Carve out caves
        if (type == TileType.STONE && isCave(worldVec.x, worldVec.y))
            type = TileType.AIR

        // Ores
        if (type == TileType.STONE && Random.nextFloat() < 0.05f)
            type = TileType.IRON_ORE
        return type
    }

    fun heightMap(x: Int): Int {
        return floor(noise.evaluateNoise(x / 20.0) * 10).toInt()
    }

    fun isCave(x: Int, y: Int): Boolean {
        val noiseValue = caveNoise.evaluateNoise(x.toDouble(), y.toDouble())
        return noiseValue > CAVE_THRESHOLD
    }
}