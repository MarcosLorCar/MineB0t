package me.orange.game.player.data

import kotlinx.serialization.json.Json
import me.orange.game.Game
import me.orange.game.player.Player
import java.io.File

class PlayerDataManager(
    private val player: Player
) {
    fun saveData() = with(player) {
        val data = PlayerData(pos, gameMode, inventory.getData())

        val file = fileOf(id, player.game.gameDataDir)

        file.parentFile.mkdirs() // Ensure directory exists

        file.writeText(Json.encodeToString(data))
    }

    companion object {
        fun loadData(id: Long, game: Game): PlayerData? {
            val file = fileOf(id, game.gameDataDir)

            return if (file.exists() && file.isFile) {
                Json.decodeFromString<PlayerData>(file.readText())
            } else null
        }

        private fun fileOf(id: Long, gameDir: String): File = File("${gameDir}/players/$id.json")
    }
}