package me.orange.game.player

import me.orange.game.utils.Vec

class PlayerMovement(
    private val player: Player
) {
    fun move(vec: Vec) = with(player) {
        if (falling) vec.x = 0

        val newPos = pos + vec
        if (!canWalkThrough(newPos)) return

        pos.move(vec)
    }

    fun fall() = with(player) {
        val below = world.getTile(player.pos - Vec(0, 1))
        if (below?.airy == true) {
            move(Vec(0, -1))
        } else {
            falling = false
        }
    }

    fun canWalkThrough(vec: Vec): Boolean {
        val tileBottom = player.world.getTile(vec) ?: return false
        val tileTop = player.world.getTile(vec + Vec(0, 1)) ?: return false
        return tileTop.airy && tileBottom.airy
    }
}