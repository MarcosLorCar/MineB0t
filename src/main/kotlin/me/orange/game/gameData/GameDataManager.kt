package me.orange.game.gameData

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import me.orange.bot.Config
import me.orange.bot.MineB0t
import me.orange.game.Game
import me.orange.game.GamesManager
import java.io.File

class GameDataManager(
    val game: Game
) {
    val file = File("${game.gameDataDir}/game.dat")

    @OptIn(ExperimentalSerializationApi::class)
    fun saveGameData() {
        if (!Config.PERSISTENCE_ENABLED) return
        val gameData = GameData(
            worldSeed = game.world.seed,
            time = game.time,
        )

        file.parentFile.mkdirs()

        file.writeBytes(Cbor.encodeToByteArray(GameData.serializer(), gameData))
    }

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        fun loadGame(guildId: String) : Game? {
            if (!Config.PERSISTENCE_ENABLED) return null
            val file = File("${Config.GAME_DATA_DIR}/$guildId/game.dat")

            if (!file.exists()) return null

            MineB0t.log("Loading save data for guild $guildId")
            val gameData = Cbor.decodeFromByteArray(GameData.serializer(), file.readBytes())
            MineB0t.log("Loaded guild $guildId: seed=${gameData.worldSeed}, time=${gameData.time}")

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