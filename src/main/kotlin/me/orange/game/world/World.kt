package me.orange.game.world

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.orange.game.Game.world
import me.orange.game.player.Player
import me.orange.game.utils.Vec
import me.orange.game.utils.isPlayerTile
import me.orange.game.utils.safeGet
import me.orange.game.world.chunk.Chunk
import me.orange.game.world.chunk.ChunkManager
import me.orange.game.world.chunk.OverworldGenerator

class World(
    seed: Long,
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    val chunkManager = ChunkManager(scope, OverworldGenerator(seed))

    suspend fun ensureChunksLoadedAround(vec: Vec, async: Boolean = true) =
        chunkManager.ensureChunksLoadedAround(vec, async)

    suspend fun getEnvironment(player: Player): MutableList<MutableList<String>> {
        val list = mutableListOf<MutableList<String>>()
        ensureChunksLoadedAround(player.pos, false)

        for (dy in player.zoom.second downTo -player.zoom.second) {
            val row = mutableListOf<String>()
            for (dx in -player.zoom.first..player.zoom.first) {
                if (isPlayerTile(dx, dy)) {
                    row.add(player.emojis[if (dy == 0) 1 else 0])
                    continue
                }
                val worldVec = player.pos + Vec(dx, dy)

                val chunk = world.getChunk(worldVec.toChunkPos())
                val localX = worldVec.x.mod(Chunk.SIZE)
                val localY = worldVec.y.mod(Chunk.SIZE)

                row.add(chunk?.tiles?.safeGet(localY, localX)?.emoji ?: ":x:")
            }
            list.add(row)
        }

        return list
    }

    fun getChunk(vec: Vec): Chunk? = chunkManager.getChunk(vec)
    fun getTile(vec: Vec): TileType? = chunkManager.getChunk(vec.toChunkPos())?.getTile(vec.toLocalPos())
    fun setTile(vec: Vec, tile: TileType) =  chunkManager.getChunk(vec.toChunkPos())?.setTile(vec.toLocalPos(), tile)
}

