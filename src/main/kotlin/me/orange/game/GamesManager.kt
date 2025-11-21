package me.orange.game

import me.orange.bot.MineB0t
import me.orange.game.gameData.GameDataManager

object GamesManager {
    val games: MutableMap<String, Game> = mutableMapOf()

    fun startGame(guildId: String) : Game {
        if (games.containsKey(guildId)) return games[guildId]!!

        val game = GameDataManager.loadGame(guildId) ?: newGame(guildId)

        MineB0t.launch {
            game.run()
        }

        return game
    }

    fun newGame(guildId: String): Game {
        val game = Game(
            guildId,
        )
        games[guildId] = game

        return game
    }

    fun getGame(guildId: String): Game =
         games[guildId] ?: startGame(guildId)

    fun saveAll() {
        games.forEach { (guildId, game) ->
            game.saveAll()
        }
    }
}