package me.orange.game

import me.orange.Emojis
import me.orange.game.Game.players
import me.orange.game.player.Player
import me.orange.game.utils.Vec
import me.orange.game.utils.isPlayerTile
import me.orange.game.utils.safeGet
import me.orange.game.world.chunk.Chunk

object GameRenderer {
    suspend fun getView(player: Player): String {
        val view = getEnvironment(player)

        addPlayersToView(player, view)

        return view.joinToString("\n") { it.joinToString("") }
    }

    suspend fun getEnvironment(player: Player): MutableList<MutableList<String>> {
        val list = mutableListOf<MutableList<String>>()
        Game.world.ensureChunksLoadedAround(player.pos, false)

        for (dy in Player.zoom.second downTo -Player.zoom.second) {
            val row = mutableListOf<String>()
            for (dx in -Player.zoom.first..Player.zoom.first) {
                if (isPlayerTile(dx, dy)) {
                    row.add(Player.emojis[if (dy == 0) 1 else 0])
                    continue
                }
                val worldVec = player.pos + Vec(dx, dy)

                val chunk = Game.world.getChunk(worldVec.toChunkPos())
                val localX = worldVec.x.mod(Chunk.SIZE)
                val localY = worldVec.y.mod(Chunk.SIZE)

                row.add(chunk?.tiles?.safeGet(localY, localX)?.emoji ?: ":x:")
            }
            list.add(row)
        }

        return list
    }

    private fun addPlayersToView(
        player: Player,
        view: MutableList<MutableList<String>>
    ) {
        player.pos.toChunkPos().surroundingChunks().forEach { chunk ->
            player.world.chunkManager.players[chunk]?.forEach { otherId ->
                if (player.id == otherId) return@forEach

                players[otherId]?.pos?.toEnvPos(player)?.let { envPos ->
                    envPos.y = (Player.zoom.second*2+1) - (envPos.y + 1)

                    if (envPos.y in view.indices && envPos.x in view[0].indices)
                        view[envPos.y][envPos.x] = Emojis.getEmojiCode("other_body")

                    val headPos = envPos.plus(0, -1)
                    if (headPos.y in view.indices && headPos.x in view[0].indices)
                        view[headPos.y][headPos.x] = Emojis.getEmojiCode("other_head")
                }
            }
        }
    }
}