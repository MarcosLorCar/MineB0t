package me.orange.game

import me.orange.bot.MineB0t
import me.orange.game.gameData.GameDataManager
import java.util.concurrent.ConcurrentHashMap

object GamesManager {
    val games: ConcurrentHashMap<String, Game> = ConcurrentHashMap()

    private fun startGame(guildId: String) : Game {
        val game = GameDataManager.loadGame(guildId) ?: run {
            MineB0t.log("No save data for guild $guildId, creating new game")
            newGame(guildId)
        }

        MineB0t.launch {
            game.run()
        }

        return game
    }

    private fun newGame(guildId: String): Game {
        return Game(guildId)
    }

    fun getGame(guildId: String): Game =
         games.computeIfAbsent(guildId) { startGame(it) }

    fun saveAll() {
        games.forEach { (_, game) ->
            game.saveAll()
        }
    }
}