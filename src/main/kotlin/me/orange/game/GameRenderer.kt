package me.orange.game

import me.orange.bot.Emojis
import me.orange.game.player.Player
import me.orange.game.utils.Vec
import me.orange.game.utils.isPlayerTile
import me.orange.game.world.chunk.Chunk

class GameRenderer(
    val game: Game,
) {
    suspend fun getView(
        player: Player,
    ): String {
        // Count all players (self + others) at each world position
        val crowd = mutableMapOf<Vec, Int>()
        crowd[player.pos] = (crowd[player.pos] ?: 0) + 1
        player.pos.toChunkPos().surroundingChunks().forEach { chunkPos ->
            game.world.chunkManager.players[chunkPos]?.forEach { otherId ->
                if (otherId == player.id) return@forEach
                val otherPos = game.players[otherId]?.pos ?: return@forEach
                crowd[otherPos] = (crowd[otherPos] ?: 0) + 1
            }
        }

        val view = getEnvironment(game, player, crowd)
        addPlayersToView(game, player, view, crowd)

        return view.joinToString("\n") { it.joinToString("") }
    }

    suspend fun getEnvironment(
        game: Game,
        player: Player,
        crowd: Map<Vec, Int>,
    ): MutableList<MutableList<String>> {
        val list = mutableListOf<MutableList<String>>()
        game.world.ensureChunksLoadedAround(player.pos, false)

        for (dy in Player.zoom.second downTo -Player.zoom.second) {
            val row = mutableListOf<String>()
            for (dx in -Player.zoom.first..Player.zoom.first) {
                if (isPlayerTile(dx, dy)) {
                    if (dy == 0) {
                        row.add(game.preferencesManager.getBodyEmoji(player.id))
                    } else {
                        // head tile — use chosen emoji, or group emoji when others share the tile
                        val headEmoji = if ((crowd[player.pos] ?: 1) > 1) {
                            Emojis.getFormatted("group_head")
                        } else {
                            game.preferencesManager.getHeadEmoji(player.id)
                        }
                        row.add(headEmoji)
                    }
                    continue
                }
                val worldVec = player.pos + Vec(dx, dy)

                val chunk = game.world.getChunk(worldVec.toChunkPos())
                val localX = worldVec.x.mod(Chunk.SIZE)
                val localY = worldVec.y.mod(Chunk.SIZE)

                row.add(chunk?.getTile(localX, localY)?.getEmoji(worldVec) ?: Emojis.getFormatted("null"))
            }
            list.add(row)
        }

        return list
    }

    private fun addPlayersToView(
        game: Game,
        player: Player,
        view: MutableList<MutableList<String>>,
        crowd: Map<Vec, Int>,
    ) {
        player.pos.toChunkPos().surroundingChunks().forEach { chunk ->
            game.world.chunkManager.players[chunk]?.forEach { otherId ->
                if (player.id == otherId) return@forEach

                val otherPlayer = game.players[otherId]
                otherPlayer?.pos?.toEnvPos(player)?.let { envPos ->
                    envPos.y = (Player.zoom.second * 2 + 1) - (envPos.y + 1)

                    if (envPos.y in view.indices && envPos.x in view[0].indices)
                        view[envPos.y][envPos.x] = game.preferencesManager.getBodyEmoji(otherId)

                    val headPos = envPos.plus(0, -1)
                    if (headPos.y in view.indices && headPos.x in view[0].indices) {
                        val headEmoji = when {
                            (crowd[otherPlayer.pos] ?: 1) > 1 -> Emojis.getFormatted("group_head")
                            otherPlayer is Player -> game.preferencesManager.getHeadEmoji(otherId)
                            else -> Emojis.getFormatted("sleepy_head")
                        }
                        view[headPos.y][headPos.x] = headEmoji
                    }
                }
            }
        }
    }
}
