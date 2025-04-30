package me.orange.game.gameData

import kotlinx.serialization.Serializable

@Serializable
data class GameData(
    val worldSeed: Long,
    val time: Long,
)