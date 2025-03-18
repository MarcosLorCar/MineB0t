package me.orange.game.world

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.orange.game.Game.world
import me.orange.game.player.Player
import me.orange.game.utils.Pos
import me.orange.game.utils.isPlayerTile
import me.orange.game.utils.safeGet
import me.orange.game.world.chunk.Chunk
import me.orange.game.world.chunk.ChunkManager
import me.orange.game.world.chunk.OverworldGenerator

class World(
    seed: Long,
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val chunkManager = ChunkManager(scope, OverworldGenerator(seed))

    fun ensureChunksLoadedAround(pos: Pos, async: Boolean = true) =
        chunkManager.ensureChunksLoadedAround(pos, async)

    fun getEnvironment(player: Player): String {
        ensureChunksLoadedAround(player.pos, false)

        val sb = StringBuilder()

        for (dy in player.zoom.second downTo -player.zoom.second) {
            for (dx in -player.zoom.first..player.zoom.first) {
                if (isPlayerTile(dx, dy)) {
                    sb.append(player.emojis[if (dy == 0) 1 else 0])
                    continue
                }
                val worldPos = player.pos + Pos(dx, dy)

                val chunk = world.getChunk(worldPos.toChunkPos())
                val localX = worldPos.x.mod(Chunk.SIZE)
                val localY = worldPos.y.mod(Chunk.SIZE)

                sb.append(chunk?.tiles?.safeGet(localY, localX)?.emoji ?: ":x:")
            }
            sb.append('\n')
        }

        return sb.toString()
    }

    fun getChunk(pos: Pos): Chunk? = chunkManager.getChunk(pos)
    fun getTile(pos: Pos): TileType? = chunkManager.getChunk(pos.toChunkPos())?.getTile(pos.toLocalPos())
    fun setTile(pos: Pos, tile: TileType) =  chunkManager.getChunk(pos.toChunkPos())?.setTile(pos.toLocalPos(), tile)
}

