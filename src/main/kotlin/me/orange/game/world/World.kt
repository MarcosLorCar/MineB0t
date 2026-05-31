package me.orange.game.world

import me.orange.bot.Config
import me.orange.bot.MineB0t
import me.orange.game.Game
import me.orange.game.utils.Vec
import me.orange.game.world.chunk.Chunk
import me.orange.game.world.chunk.ChunkManager
import me.orange.game.world.generation.OverworldGenerator
import me.orange.game.world.tile.Tile
import kotlin.random.Random

class World(
    game: Game,
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
         val spawnY = chunkManager.getHighestAt(spawnX)

         return Vec(spawnX, spawnY)
    }

    fun hasCraftingStation(pos: Vec): Boolean = TODO()
}

