package me.orange.game.player

import kotlinx.serialization.Serializable

@Serializable
enum class GameMode(
    val actionName: String,
) {
    BREAK("break"),
    PLACE("place"),
}