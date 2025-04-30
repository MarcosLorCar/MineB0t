package me.orange.game.gameData

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import me.orange.bot.Config
import me.orange.game.Game
import me.orange.game.GamesManager
import java.io.File

class GameDataManager(
    val game: Game
) {
    val file = File("${game.gameDataDir}/game.dat")

    @OptIn(ExperimentalSerializationApi::class)
    fun saveGameData() {
        val gameData = GameData(
            worldSeed = game.world.seed,
            time = game.time,
        )

        file.parentFile.mkdirs()

        file.writeBytes(Cbor.Default.encodeToByteArray(GameData.serializer(), gameData))
    }

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        fun loadGame(guildId: String) : Game? {
            println("Loading game $guildId")
            val file = File("${Config.GAME_DATA_DIR}/$guildId/game.dat")

            if (!file.exists()) return null

            val gameData = Cbor.Default.decodeFromByteArray(GameData.serializer(), file.readBytes())

            val game = Game(
                guildId,
                gameData.worldSeed,
                time = gameData.time
            )

            GamesManager.games[guildId] = game

            return game
        }
    }
}