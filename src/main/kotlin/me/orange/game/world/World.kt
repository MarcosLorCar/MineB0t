package me.orange.game.world

import me.orange.bot.Config
import me.orange.bot.MineB0t
import me.orange.game.Game
import me.orange.game.craft.CraftingStationType
import me.orange.game.inventory.Inventory
import me.orange.game.world.tile.TileEntityData
import me.orange.game.world.tile.TileInteraction
import me.orange.game.utils.Vec
import me.orange.game.world.chunk.Chunk
import me.orange.game.world.chunk.ChunkManager
import me.orange.game.world.generation.OverworldGenerator
import me.orange.game.world.tile.Tile
import kotlin.random.Random

class World(
    val game: Game,
    val seed: Long,
) {
    val chunkManager = ChunkManager(this, OverworldGenerator(seed, this))

    val worldDataDir = "${game.gameDataDir}/world"

    suspend fun ensureChunksLoadedAround(vec: Vec, async: Boolean = true) =
        chunkManager.ensureChunksLoadedAround(vec, async)

    fun getChunk(vec: Vec): Chunk? = chunkManager.getChunk(vec)
    fun getTile(vec: Vec): Tile? = chunkManager.getChunk(vec.toChunkPos())?.getTile(vec.toLocalPos())
    /** Sets a tile, returning false (and logging) if the target chunk isn't loaded so the edit is dropped. */
    fun setTile(vec: Vec, tile: Tile): Boolean {
        val chunk = chunkManager.getChunk(vec.toChunkPos()) ?: run {
            MineB0t.log("Dropped setTile at $vec: chunk ${vec.toChunkPos()} is not loaded")
            return false
        }
        chunk.setTile(vec.toLocalPos(), tile)
        return true
    }

    suspend fun generateSpawnPoint(): Vec {
        val dispersion = Config.SPAWNPOINT_DISPERSION
        val spawnX = Random.nextInt(-dispersion, dispersion)
        val spawnY = chunkManager.surfaceY(spawnX) ?: chunkManager.getHighestAt(spawnX)
        return Vec(spawnX, spawnY)
    }

    /**
     * Resolves [pos] to the nearest safe standing position (both feet and head airy, resting on solid
     * ground). First pushes upward to escape any solid block, then drops down to the ground. Stops
     * early if a chunk boundary is not loaded. Call after ensureChunksLoadedAround for best results.
     */
    fun settle(pos: Vec): Vec {
        var y = pos.y
        // Push up until both feet and head are airy (or chunk unloaded — stop there)
        for (i in 0 until 64) {
            val feet = getTile(Vec(pos.x, y)) ?: break
            val head = getTile(Vec(pos.x, y + 1)) ?: break
            if (feet.airy && head.airy) break
            y++
        }
        // Fall down to rest on the first solid tile
        while (true) {
            val below = getTile(Vec(pos.x, y - 1)) ?: break
            if (!below.airy) break
            y--
        }
        return Vec(pos.x, y)
    }

    /**
     * The interactable tile the player at [pos] can use. Checks under feet, at feet, and at head.
     * First non-None interaction wins; None if none (or chunk unloaded).
     */
    fun getInteractionAt(pos: Vec): TileInteraction {
        for (candidate in listOf(pos.plus(0, -1), pos, pos.plus(0, 1))) {
            val interaction = getTile(candidate)?.interaction ?: continue
            if (interaction != TileInteraction.BareHanded) return interaction
        }
        return TileInteraction.BareHanded
    }

    fun getCraftingStationAt(pos: Vec): CraftingStationType =
        candidates(pos).firstNotNullOfOrNull { (getTile(it)?.interaction as? TileInteraction.CraftingStation)?.type }
            ?: CraftingStationType.NONE

    fun getChestPosAt(pos: Vec): Vec? =
        candidates(pos).firstOrNull { getTile(it)?.interaction is TileInteraction.Chest }

    fun getChestAt(worldPos: Vec): TileEntityData.ChestData? {
        val chunk = chunkManager.getChunk(worldPos.toChunkPos()) ?: return null
        return chunk.tileEntities[worldPos.toLocalPos()] as? TileEntityData.ChestData
    }

    fun ensureChestAt(worldPos: Vec) {
        val chunk = chunkManager.getChunk(worldPos.toChunkPos()) ?: return
        val localPos = worldPos.toLocalPos()
        if (chunk.tileEntities[localPos] == null) {
            chunk.tileEntities[localPos] = TileEntityData.ChestData(
                Inventory.InventoryData(Vec(3, 4), mutableListOf())
            )
        }
    }

    fun removeChestAt(worldPos: Vec) {
        val chunk = chunkManager.getChunk(worldPos.toChunkPos()) ?: return
        chunk.tileEntities.remove(worldPos.toLocalPos())
    }

    private fun candidates(pos: Vec) = listOf(pos.plus(0, -1), pos, pos.plus(0, 1))

}

