package me.orange.game.world

import kotlinx.coroutines.CoroutineScope
import me.orange.bot.Config
import me.orange.game.Game
import me.orange.game.utils.Vec
import me.orange.game.world.chunk.Chunk
import me.orange.game.world.chunk.ChunkManager
import me.orange.game.world.generation.OverworldGenerator
import kotlin.random.Random

class World(
    game: Game,
    val seed: Long,
    scope: CoroutineScope,
) {
    val chunkManager = ChunkManager(this, scope, OverworldGenerator(seed, this))

    val worldDataDir = "${game.gameDataDir}/world"

    suspend fun ensureChunksLoadedAround(vec: Vec, async: Boolean = true) =
        chunkManager.ensureChunksLoadedAround(vec, async)

    fun getChunk(vec: Vec): Chunk? = chunkManager.getChunk(vec)
    fun getTile(vec: Vec): TileType? = chunkManager.getChunk(vec.toChunkPos())?.getTile(vec.toLocalPos())
    fun setTile(vec: Vec, tile: TileType) =  chunkManager.getChunk(vec.toChunkPos())?.setTile(vec.toLocalPos(), tile)

     suspend fun generateSpawnPoint(): Vec {
         val dispersion = Config.SPAWNPOINT_DISPERSION

         val spawnX = Random.nextInt(-dispersion, dispersion)
         val spawnY = chunkManager.getHighestAt(spawnX)

         return Vec(spawnX, spawnY)
    }
}

