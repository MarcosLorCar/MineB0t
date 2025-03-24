package me.orange.game.player

import me.orange.game.utils.Vec

class PlayerMovement(
    private val player: Player,
) {
    val world = player.game.world

    fun move(vec: Vec) = with(player) {
        world.chunkManager.players[pos.toChunkPos()]?.remove(player.id)
        if (falling) vec.x = 0

        val newPos = pos + vec
        if (!canWalkThrough(newPos)) {
            if (canStepUp(pos, vec))
                pos.move(vec.plus(0, 1))
            return@with
        }

        pos.move(vec)
        world.chunkManager.players.getOrPut(pos.toChunkPos()){mutableListOf()}.add(player.id)
    }

    fun fall(time: Long) = with(player) {

        val below = world.getTile(player.pos - Vec(0, 1))
        if (below?.airy == true) {
            falling = true
            move(Vec(0, -1))
        } else {
            falling = false
        }
    }
}