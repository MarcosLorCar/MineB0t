package me.orange.game.world.chunk

import kotlinx.coroutines.*
import me.orange.bot.MineB0t
import me.orange.game.utils.Vec
import me.orange.game.world.World
import me.orange.game.world.chunk.Chunk.Companion.SIZE
import me.orange.game.world.generation.ChunkGenerator
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

    companion object {
        private const val UNLOAD_DELAY = 30_000L
    }

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
        chunks[chunkPos] = withContext(Dispatchers.IO) { chunkDataManager.loadData(chunkPos) } ?: generateChunk(chunkPos)
        deferred.complete(Unit)
        loadingChunks.remove(chunkPos)
    }

    private fun generateChunk(vec: Vec): Chunk {
        return chunkGenerator?.generateChunk(vec) ?: Chunk.nullChunk(vec)
    }

     fun unloadUnusedChunks() {
        val currentTime = System.currentTimeMillis()
        val toRemove = chunkLastUsed.filterValues { currentTime - it > UNLOAD_DELAY }.keys

        toRemove.forEach { chunkPos ->
            MineB0t.log("Unloading chunk $chunkPos")
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

    fun saveChunks() {
        chunks.forEach { (pos, chunk) ->
            chunkDataManager.saveData(chunk)
        }
    }

    fun getChunk(vec: Vec): Chunk? = chunks[vec]
}