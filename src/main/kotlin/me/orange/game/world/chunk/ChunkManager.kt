package me.orange.game.world.chunk

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.orange.game.utils.Pos
import me.orange.game.utils.surroundingChunks
import java.util.concurrent.ConcurrentHashMap

class ChunkManager(
    val scope: CoroutineScope,
    private var chunkGenerator: ChunkGenerator? = null,
) {
    private val chunks = ConcurrentHashMap<Pos, Chunk>()
    private val chunkLastUsed = ConcurrentHashMap<Pos, Long>()
    private val loadingChunks = ConcurrentHashMap<Pos, CompletableDeferred<Unit>>()
    val players: MutableMap<Pos, MutableList<Long>> = mutableMapOf()

    companion object {
        private const val UNLOAD_DELAY = 30_000L
    }

     fun ensureChunksLoadedAround(pos: Pos, async: Boolean = true) = runBlocking(scope.coroutineContext) {
         val centerChunk = pos.toChunkPos()

         centerChunk.surroundingChunks().forEach { chunkPos ->
             loadOrWaitForChunk(chunkPos, async)
             chunkLastUsed[chunkPos] = System.currentTimeMillis()
         }

         if (async) unloadUnusedChunks()
     }

    private fun loadOrWaitForChunk(chunkPos: Pos, async: Boolean) {
        when {
            shouldLoadChunk(chunkPos) ->
                loadChunk(chunkPos, async)
            !async ->
                while (loadingChunks.containsKey(chunkPos)) {
                    println("Waiting for chunk ${chunkPos}...")
                    Thread.sleep(10) // Wait a bit for it to load
                }
        }
    }

    private fun shouldLoadChunk(chunkPos: Pos): Boolean =
        !chunks.containsKey(chunkPos) && loadingChunks.putIfAbsent(chunkPos, CompletableDeferred()) == null

    private fun loadChunk(chunkPos: Pos, async: Boolean) {
        if (!async) {
            loadingChunks.putIfAbsent(chunkPos, CompletableDeferred())
            chunks[chunkPos] = generateChunk(chunkPos)
            loadingChunks.remove(chunkPos)
            return
        }

        val deferred = CompletableDeferred<Unit>()
        loadingChunks[chunkPos] = deferred

        scope.launch {
            try {
                chunks[chunkPos] = generateChunk(chunkPos)
            } finally {
                deferred.complete(Unit)
                loadingChunks.remove(chunkPos)
            }
        }
    }

    private fun generateChunk(pos: Pos): Chunk {
        return chunkGenerator?.generateChunk(pos) ?: Chunk.nullChunk(pos)
    }

    private fun unloadUnusedChunks() {
        val currentTime = System.currentTimeMillis()
        val toRemove = chunkLastUsed.filterValues { currentTime - it > UNLOAD_DELAY }.keys

        toRemove.forEach { chunkPos ->
            chunks.remove(chunkPos)
            chunkLastUsed.remove(chunkPos)
        }
    }

    fun getChunk(pos: Pos): Chunk? = chunks[pos]
}