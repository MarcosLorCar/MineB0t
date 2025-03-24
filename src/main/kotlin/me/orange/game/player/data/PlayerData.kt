package me.orange.game.player.data

import kotlinx.serialization.Serializable
import me.orange.game.utils.GameMode
import me.orange.game.utils.Vec

@Serializable
data class PlayerData(val position: Vec, val gameMode: GameMode)