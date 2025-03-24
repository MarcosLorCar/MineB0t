package me.orange.game.utils

import kotlinx.serialization.Serializable

@Serializable
enum class GameMode(
    val actionName: String,
) {
    BREAK("break"),
    PLACE("place"),
}