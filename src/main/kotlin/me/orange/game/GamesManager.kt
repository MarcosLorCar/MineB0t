package me.orange.game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.orange.game.gameData.GameDataManager

object GamesManager {
    val scope = CoroutineScope(Dispatchers.Default)
    val games: MutableMap<String, Game> = mutableMapOf()
    val gameJobs: MutableMap<String, Job> = mutableMapOf()

    fun startGame(guildId: String) : Game {
        if (games.containsKey(guildId)) return games[guildId]!!

        val game = GameDataManager.loadGame(guildId) ?: newGame(guildId)

        gameJobs[guildId] = scope.launch {
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