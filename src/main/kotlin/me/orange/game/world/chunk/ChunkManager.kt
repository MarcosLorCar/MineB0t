package me.orange.game.world.chunk

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.orange.MineB0t
import me.orange.game.utils.Vec
import me.orange.game.utils.surroundingChunks
import java.util.concurrent.ConcurrentHashMap

class ChunkManager(
    val scope: CoroutineScope,
    private var chunkGenerator: ChunkGenerator? = null,
) {
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

        if (async) unloadUnusedChunks()
    }

    private suspend fun loadOrWaitForChunk(chunkVec: Vec, async: Boolean) {
        when {
            shouldLoadChunk(chunkVec) ->
                loadChunk(chunkVec, async)
            !async ->
                while (loadingChunks.containsKey(chunkVec)) {
                    MineB0t.log("Waiting for chunk ${chunkVec}...")
                    delay(10) // Wait a bit for it to load
                }
        }
    }

    private fun shouldLoadChunk(chunkVec: Vec): Boolean =
        !chunks.containsKey(chunkVec) && loadingChunks.putIfAbsent(chunkVec, CompletableDeferred()) == null

    private fun loadChunk(chunkVec: Vec, async: Boolean) {
        if (!async) {
            loadingChunks.putIfAbsent(chunkVec, CompletableDeferred())
            chunks[chunkVec] = generateChunk(chunkVec)
            loadingChunks.remove(chunkVec)
            return
        }

        val deferred = CompletableDeferred<Unit>()
        loadingChunks[chunkVec] = deferred

        scope.launch {
            try {
                chunks[chunkVec] = generateChunk(chunkVec)
            } finally {
                deferred.complete(Unit)
                loadingChunks.remove(chunkVec)
            }
        }
    }

    private fun generateChunk(vec: Vec): Chunk {
        return chunkGenerator?.generateChunk(vec) ?: Chunk.nullChunk(vec)
    }

    private fun unloadUnusedChunks() {
        val currentTime = System.currentTimeMillis()
        val toRemove = chunkLastUsed.filterValues { currentTime - it > UNLOAD_DELAY }.keys

        toRemove.forEach { chunkPos ->
            chunks.remove(chunkPos)
            chunkLastUsed.remove(chunkPos)
        }
    }

    fun getChunk(vec: Vec): Chunk? = chunks[vec]
}