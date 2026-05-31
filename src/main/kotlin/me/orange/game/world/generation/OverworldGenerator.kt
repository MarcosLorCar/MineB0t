package me.orange.game.world.generation

import de.articdive.jnoise.core.api.functions.Interpolation
import de.articdive.jnoise.generators.noise_parameters.fade_functions.FadeFunction
import de.articdive.jnoise.pipeline.JNoise
import me.orange.game.utils.Vec
import me.orange.game.world.World
import me.orange.game.world.chunk.Chunk
import me.orange.game.world.tile.Tile
import me.orange.game.world.tile.Tiles
import kotlin.math.floor

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

    // Seed-offset so ore veins don't correlate with terrain height / caves.
    val oreNoise = JNoise.newBuilder()
        .perlin(seed + 31, Interpolation.COSINE, FadeFunction.QUINTIC_POLY)
        .scale(0.3)
        .build()

    companion object {
        const val STONE_LAYER_DEPTH = 4
        const val CAVE_THRESHOLD = 0.25
        const val ORE_THRESHOLD = 0.45
    }

    override fun generateChunk(chunkVec: Vec): Chunk {
        val tiles = MutableList(Chunk.SIZE) { MutableList(Chunk.SIZE) { Tiles.NULL.id } }

        for (x in 0 until Chunk.SIZE) {
            val worldX = x + chunkVec.x * Chunk.SIZE
            val height = heightMap(worldX)

            for (y in 0 until Chunk.SIZE) {
                val worldVec = Vec(x, y).toWorldPos(chunkVec)

                val type = getTileType(worldVec, height)

                tiles[y][x] = type.id
            }
        }

        // TODO For each chunk around this one *C, if all around *C are generated, decorate *C


        // TODO If this chunk has all chunks around it generated, decorate it


        return Chunk(chunkVec, tiles)
    }

    private fun decorate(chunk: Chunk?) {
        TODO("Not yet implemented")
    }

    private fun getTileType(worldVec: Vec, height: Int): Tile {
        var type = when {
            // The most superficial layer
            (worldVec.y == height) -> Tiles.GRASS

            // Cave depth
            (worldVec.y < (height - STONE_LAYER_DEPTH)) -> Tiles.STONE

            // Between surface and stone
            (worldVec.y < height) -> Tiles.DIRT

            // Above surface
            else -> Tiles.AIR
        }

        // Carve out caves
        if (type == Tiles.STONE && isCave(worldVec.x, worldVec.y))
            type = Tiles.AIR

        // Ores (seeded noise so generation is reproducible for a given seed)
        if (type == Tiles.STONE && isOre(worldVec.x, worldVec.y))
            type = Tiles.IRON_ORE
        return type
    }

    fun heightMap(x: Int): Int {
        return floor(noise.evaluateNoise(x / 20.0) * 10).toInt()
    }

    fun isCave(x: Int, y: Int): Boolean {
        val noiseValue = caveNoise.evaluateNoise(x.toDouble(), y.toDouble())
        return noiseValue > CAVE_THRESHOLD
    }

    fun isOre(x: Int, y: Int): Boolean {
        val noiseValue = oreNoise.evaluateNoise(x.toDouble(), y.toDouble())
        return noiseValue > ORE_THRESHOLD
    }
}