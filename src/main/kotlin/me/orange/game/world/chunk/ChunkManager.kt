package me.orange.game.world.chunk

import kotlinx.coroutines.*
import me.orange.bot.Config
import me.orange.bot.MineB0t
import me.orange.game.utils.Vec
import me.orange.game.world.World
import me.orange.game.world.chunk.Chunk.Companion.SIZE
import me.orange.game.world.generation.ChunkGenerator
import me.orange.game.world.tile.Tile
import me.orange.game.world.tile.Tiles
import java.util.concurrent.ConcurrentHashMap

class ChunkManager(
    val world: World,
    private var chunkGenerator: ChunkGenerator? = null,
) {
    private val chunkDataManager = ChunkDataManager(world)
    private val chunks = ConcurrentHashMap<Vec, Chunk>()
    private val chunkLastUsed = ConcurrentHashMap<Vec, Long>()
    private val loadingChunks = ConcurrentHashMap<Vec, CompletableDeferred<Unit>>()
    val players: MutableMap<Vec, MutableList<Long>> = mutableMapOf()


    suspend fun ensureChunksLoadedAround(vec: Vec, async: Boolean = true) {
        val centerChunk = vec.toChunkPos()

        centerChunk.surroundingChunks().forEach { chunkPos ->
            loadOrWaitForChunk(chunkPos, async)
            chunkLastUsed[chunkPos] = System.currentTimeMillis()
        }
    }

    private suspend fun loadOrWaitForChunk(chunkVec: Vec, async: Boolean) {
        when {
            shouldLoadChunk(chunkVec) ->
                if(async) loadChunkAsync(chunkVec) else loadChunkSync(chunkVec)
            !async ->
                loadingChunks[chunkVec]?.await()
        }
    }

    private fun shouldLoadChunk(chunkVec: Vec): Boolean =
        !chunks.containsKey(chunkVec) && loadingChunks.putIfAbsent(chunkVec, CompletableDeferred()) == null

    fun loadChunkAsync(chunkPos: Vec) {
        MineB0t.launch {
            loadChunkSync(chunkPos)
        }
    }

    suspend fun loadChunkSync(chunkPos: Vec) {
        val deferred = CompletableDeferred<Unit>()

        loadingChunks.putIfAbsent(chunkPos, deferred)
        val loaded = withContext(Dispatchers.IO) { chunkDataManager.loadData(chunkPos) }
        if (loaded == null) MineB0t.log("Generating chunk $chunkPos in guild ${world.game.guildName} (${world.game.guildId})")
        chunks[chunkPos] = loaded ?: generateChunk(chunkPos)
        deferred.complete(Unit)
        loadingChunks.remove(chunkPos)

        for (dcx in 0 downTo -1)
            for (dcy in 0 downTo -1)
                tryDecorate(chunkPos + Vec(dcx, dcy))
    }

    private fun tryDecorate(cornerOwner: Vec) {
        val ownerChunk = chunks[cornerOwner] ?: return
        if (ownerChunk.decorated) return

        val group = listOf(
            cornerOwner,
            cornerOwner + Vec(1, 0),
            cornerOwner + Vec(0, 1),
            cornerOwner + Vec(1, 1)
        )
        if (!group.all { chunks.containsKey(it) }) return

        synchronized(ownerChunk) {
            if (ownerChunk.decorated) return

            val getTile = { worldVec: Vec -> chunks[worldVec.toChunkPos()]?.getTile(worldVec.toLocalPos()) }
            val setTile = { worldVec: Vec, tile: Tile ->
                chunks[worldVec.toChunkPos()]?.setTile(worldVec.toLocalPos(), tile)
                Unit
            }

            chunkGenerator?.decorateCorner(cornerOwner, getTile, setTile)
            ownerChunk.decorated = true
        }
        chunkDataManager.saveData(ownerChunk)
    }

    private fun generateChunk(vec: Vec): Chunk {
        return chunkGenerator?.generateChunk(vec) ?: Chunk.uniformChunk(vec, Tiles.NULL)
    }

     fun unloadUnusedChunks() {
        val currentTime = System.currentTimeMillis()
        val toRemove = chunkLastUsed.filterValues { currentTime - it > Config.CHUNK_UNLOAD_DELAY }.keys

        toRemove.forEach { chunkPos ->
            chunkDataManager.saveData(chunks[chunkPos])
            chunks.remove(chunkPos)
            chunkLastUsed.remove(chunkPos)
        }
    }

    suspend fun getHighestAt(x: Int): Int {
        val localX = x.mod(SIZE)
        for (chunkY in 3 downTo -3) {
            val worldPos = Vec(x, chunkY * SIZE)
            loadChunkSync(worldPos.toChunkPos())
            val chunk = getChunk(worldPos.toChunkPos())!!
            for (localY in (SIZE-1) downTo 0) {
                if (
                    chunk.getTile(Vec(localX, localY))?.airy == true &&
                    chunk.getTile(Vec(localX, localY-1))?.airy == false
                )
                    return localY + SIZE * chunkY
            }
        }
        return 0
    }

    /** Removes a player id from every chunk's occupant list, dropping now-empty entries. */
    fun removePlayer(id: Long) {
        players.values.forEach { it.remove(id) }
        players.entries.removeIf { it.value.isEmpty() }
    }

    fun saveChunks() {
        chunks.forEach { (_, chunk) ->
            chunkDataManager.saveData(chunk)
        }
    }

    fun getChunk(vec: Vec): Chunk? = chunks[vec]

    /** Returns the first airy Y above the generated surface at column [x], or null if unavailable. */
    fun surfaceY(x: Int): Int? = (chunkGenerator as? me.orange.game.world.generation.OverworldGenerator)
        ?.run { heightMap(x) + 1 }
}