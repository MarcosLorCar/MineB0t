package me.orange.game.player.data

import kotlinx.serialization.json.Json
import me.orange.Config
import me.orange.game.player.Player
import java.io.File

class PlayerDataManager(
    private val player: Player
) {
    fun saveData() = with(player) {
        val data = PlayerData(pos, gameMode)

        val file = fileOf(id)

        file.parentFile.mkdirs() // Ensure directory exists

        file.writeText(Json.encodeToString(data))
    }

    companion object {
        fun loadData(id: Long): PlayerData? {
            val file = fileOf(id)

            return if (file.exists() && file.isFile) {
                Json.decodeFromString<PlayerData>(file.readText())
            } else null
        }

        private fun fileOf(id: Long): File = File("${Config.PLAYER_DATA_DIR}/$id.json")
    }
}