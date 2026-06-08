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
        .perlin(seed, Interpolation.COSINE, FadeFunction.QUINTIC_POLY)
        .build()

    val caveNoise = JNoise.newBuilder()
        .perlin(seed, Interpolation.COSINE, FadeFunction.QUINTIC_POLY)
        .scale(0.1)
        .build()

    val dirtNoise = JNoise.newBuilder()
        .perlin(seed + 53, Interpolation.COSINE, FadeFunction.QUINTIC_POLY)
        .scale(0.08)
        .build()

    private val decorations = listOf(
        PatchDecoration(Tiles.IRON_ORE, 0x517CC1B727220A95L),
        PatchDecoration(Tiles.COAL_ORE, 0x27D4EB2F165667C5L),
        ScatterDecoration(0.1f, { it == Tiles.GRASS }, { it == Tiles.AIR }, Tiles.RED_SHROOM, 0x3141592653589793L),
        TreeDecoration(0.25f, 0x9E3779B9L)
    )

    companion object {
        const val STONE_LAYER_DEPTH = 4
        const val DIRT_VARIATION = 3
        const val CAVE_THRESHOLD = 0.25
    }

    override fun generateChunk(chunkVec: Vec): Chunk {
        val tiles = MutableList(Chunk.SIZE) { MutableList(Chunk.SIZE) { Tiles.NULL.id } }

        for (x in 0 until Chunk.SIZE) {
            val worldX = x + chunkVec.x * Chunk.SIZE
            val height = heightMap(worldX)
            val depth = dirtDepth(worldX)

            for (y in 0 until Chunk.SIZE) {
                val worldVec = Vec(x, y).toWorldPos(chunkVec)
                tiles[y][x] = getTileType(worldVec, height, depth).id
            }
        }

        return Chunk(chunkVec, tiles)
    }

    private fun getTileType(worldVec: Vec, height: Int, dirtDepth: Int): Tile {
        val base = when {
            worldVec.y == height -> Tiles.GRASS
            worldVec.y < (height - dirtDepth) -> Tiles.STONE
            worldVec.y < height -> Tiles.DIRT
            else -> Tiles.AIR
        }
        return if (base == Tiles.STONE && isCave(worldVec.x, worldVec.y)) Tiles.AIR else base
    }

    override fun decorateCorner(
        cornerChunkPos: Vec,
        getTile: (Vec) -> Tile?,
        setTile: (Vec, Tile) -> Unit
    ) {
        val origin = Vec(
            cornerChunkPos.x * Chunk.SIZE + Chunk.SIZE / 2,
            cornerChunkPos.y * Chunk.SIZE + Chunk.SIZE / 2
        )

        decorations.forEach { decoration ->
            val rng = java.util.Random(
                seed xor (cornerChunkPos.x.toLong() * 0x517CC1B727220A95L)
                     xor (cornerChunkPos.y.toLong() * 0x6C62272E07BB0142L)
                     xor decoration.salt
            )
            decoration.generate(origin, rng, getTile, setTile)
        }
    }

    fun heightMap(x: Int): Int = floor(noise.evaluateNoise(x / 20.0) * 10).toInt()

    fun dirtDepth(x: Int): Int =
        (STONE_LAYER_DEPTH + floor(dirtNoise.evaluateNoise(x.toDouble()) * DIRT_VARIATION).toInt())
            .coerceAtLeast(2)

    fun isCave(x: Int, y: Int): Boolean = caveNoise.evaluateNoise(x.toDouble(), y.toDouble()) > CAVE_THRESHOLD
}
