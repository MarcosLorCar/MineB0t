package me.orange.game.world.chunk

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import me.orange.bot.Config
import me.orange.bot.MineB0t
import me.orange.game.utils.Vec
import me.orange.game.world.World
import java.io.File

class ChunkDataManager(
    private val world: World,
) {
    @OptIn(ExperimentalSerializationApi::class)
    fun loadData(chunkPos: Vec) : Chunk? {
        if (!Config.PERSISTENCE_ENABLED) return null
        val file = fileOf(chunkPos)

        if (!file.exists()) return null

        return try {
            Cbor.decodeFromByteArray(Chunk.serializer(), file.readBytes())
        } catch (e: Exception) {
            MineB0t.log("Discarding unreadable chunk $chunkPos (${e.message}), will regenerate")
            file.delete()
            null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun saveData(chunk: Chunk?) {
        if (!Config.PERSISTENCE_ENABLED || chunk == null) return

        val file = fileOf(chunk.worldPos)

        file.parentFile.mkdirs()

        file.writeBytes(Cbor.encodeToByteArray(Chunk.serializer(), chunk))
    }

    private fun fileOf(chunkPos: Vec): File = File(buildString {
        append(world.worldDataDir)
        append("/")
        append(chunkPos.x)
        append(".")
        append(chunkPos.y)
        append(".dat")
    })
}