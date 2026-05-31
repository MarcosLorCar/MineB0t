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
import kotlin.math.sqrt

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
        val originX = cornerChunkPos.x * Chunk.SIZE + Chunk.SIZE / 2
        val originY = cornerChunkPos.y * Chunk.SIZE + Chunk.SIZE / 2

        val rng = java.util.Random(
            seed xor (cornerChunkPos.x.toLong() * 0x517CC1B727220A95L)
                 xor (cornerChunkPos.y.toLong() * 0x6C62272E07BB0142L)
        )

        var stoneCount = 0
        for (dy in 0 until Chunk.SIZE)
            for (dx in 0 until Chunk.SIZE)
                if (getTile(Vec(originX + dx, originY + dy)) == Tiles.STONE) stoneCount++

        val patchCount = when {
            stoneCount >= 80 -> rng.nextInt(3)
            stoneCount >= 30 -> rng.nextInt(2)
            else -> 0
        }

        repeat(patchCount) {
            val cx = originX + rng.nextInt(Chunk.SIZE)
            val cy = originY + rng.nextInt(Chunk.SIZE)
            val radius = 1.5f + rng.nextFloat() * 1.5f
            placePatch(Vec(cx, cy), radius, rng, getTile, setTile)
        }
    }

    private fun placePatch(
        center: Vec,
        radius: Float,
        rng: java.util.Random,
        getTile: (Vec) -> Tile?,
        setTile: (Vec, Tile) -> Unit
    ) {
        val r = radius.toInt() + 1
        for (dy in -r..r) {
            for (dx in -r..r) {
                val dist = sqrt((dx * dx + dy * dy).toFloat())
                if (dist > radius) continue
                val pos = center.plus(dx, dy)
                if (getTile(pos) != Tiles.STONE) continue
                if (rng.nextFloat() < 1f - (dist / radius) * 0.5f)
                    setTile(pos, Tiles.IRON_ORE)
            }
        }
    }

    fun heightMap(x: Int): Int = floor(noise.evaluateNoise(x / 20.0) * 10).toInt()

    fun dirtDepth(x: Int): Int =
        (STONE_LAYER_DEPTH + floor(dirtNoise.evaluateNoise(x.toDouble()) * DIRT_VARIATION).toInt())
            .coerceAtLeast(2)

    fun isCave(x: Int, y: Int): Boolean = caveNoise.evaluateNoise(x.toDouble(), y.toDouble()) > CAVE_THRESHOLD
}
