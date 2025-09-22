package me.orange.game.player.offline

import me.orange.game.player.GameMode
import me.orange.game.utils.Vec

open class OfflinePlayer(
    val id: Long,
    val pos: Vec,
    var gameMode: GameMode,
)