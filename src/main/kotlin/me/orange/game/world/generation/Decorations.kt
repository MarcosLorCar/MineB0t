package me.orange.game.world.generation

import me.orange.game.utils.Vec
import me.orange.game.world.chunk.Chunk
import me.orange.game.world.tile.Tile
import me.orange.game.world.tile.Tiles
import java.util.*
import kotlin.math.sqrt

class PatchDecoration(
    private val targetTile: Tile,
    override val salt: Long,
    private val spreadTile: Tile = Tiles.STONE,
    private val minPatches: Int = 0,
    private val maxPatches: Int = 3,
    private val radiusMin: Float = 1.5f,
    private val radiusMax: Float = 3.0f,
    private val patchChance: (stoneCount: Int) -> Int = { count ->
        when {
            count >= 80 -> (minPatches..maxPatches).random()
            count >= 30 -> (minPatches..(maxPatches - 1).coerceAtLeast(0)).random()
            else -> 0
        }
    }
) : Decoration {
    override fun generate(origin: Vec, rng: Random, getTile: (Vec) -> Tile?, setTile: (Vec, Tile) -> Unit) {
        var stoneCount = 0
        for (dy in 0 until Chunk.SIZE)
            for (dx in 0 until Chunk.SIZE)
                if (getTile(origin.plus(dx, dy)) == spreadTile) stoneCount++

        val count = patchChance(stoneCount)
        repeat(count) {
            val cx = origin.x + rng.nextInt(Chunk.SIZE)
            val cy = origin.y + rng.nextInt(Chunk.SIZE)
            val radius = radiusMin + rng.nextFloat() * (radiusMax - radiusMin)
            placePatch(Vec(cx, cy), radius, rng, getTile, setTile)
        }
    }

    private fun placePatch(
        center: Vec,
        radius: Float,
        rng: Random,
        getTile: (Vec) -> Tile?,
        setTile: (Vec, Tile) -> Unit
    ) {
        val r = radius.toInt() + 1
        for (dy in -r..r) {
            for (dx in -r..r) {
                val dist = sqrt((dx * dx + dy * dy).toFloat())
                if (dist > radius) continue
                val pos = center.plus(dx, dy)
                if (getTile(pos) != spreadTile) continue
                if (rng.nextFloat() < 1f - (dist / radius) * 0.5f)
                    setTile(pos, targetTile)
            }
        }
    }
}

class ScatterDecoration(
    private val chance: Float,
    private val canPlaceOn: (Tile?) -> Boolean,
    private val canPlaceAt: (Tile?) -> Boolean,
    private val targetTile: Tile,
    override val salt: Long
) : Decoration {
    override fun generate(origin: Vec, rng: Random, getTile: (Vec) -> Tile?, setTile: (Vec, Tile) -> Unit) {
        for (dx in 0 until Chunk.SIZE) {
            val wx = origin.x + dx
            for (dy in 0 until Chunk.SIZE) {
                val wy = origin.y + dy
                val pos = Vec(wx, wy)
                if (canPlaceOn(getTile(pos)) &&
                    canPlaceAt(getTile(pos.plus(0, 1))) &&
                    rng.nextFloat() < chance
                ) {
                    setTile(pos.plus(0, 1), targetTile)
                }
            }
        }
    }
}

class TreeDecoration(
    private val chance: Float = 0.1f,
    override val salt: Long = 0x9E3779B9L
) : Decoration {
    override fun generate(origin: Vec, rng: Random, getTile: (Vec) -> Tile?, setTile: (Vec, Tile) -> Unit) {
        val treeTiles = listOf(Tiles.LOG, Tiles.LEAVES, Tiles.CANOPY)
        for (dx in 0 until Chunk.SIZE) {
            val wx = origin.x + dx
            for (dy in 0 until Chunk.SIZE) {
                val wy = origin.y + dy
                val pos = Vec(wx, wy)
                if (getTile(pos) == Tiles.GRASS &&
                    getTile(pos.plus(0, 1)) == Tiles.AIR &&
                    getTile(pos.plus(-1, 1)) !in treeTiles &&
                    getTile(pos.plus(1, 1)) !in treeTiles &&
                    rng.nextFloat() < chance
                ) {
                    placeTree(pos.plus(0, 1), rng, setTile)
                }
            }
        }
    }

    private fun placeTree(base: Vec, rng: Random, setTile: (Vec, Tile) -> Unit) {
        val height = 2 + rng.nextInt(3)
        // Trunk
        setTile(base, Tiles.LOG)

        // Middle leaves
        for (h in 1 until height - 1) {
            setTile(base.plus(0, h), Tiles.LEAVES)
        }

        // Canopy
        setTile(base.plus(0, height - 1), Tiles.CANOPY)
    }
}
