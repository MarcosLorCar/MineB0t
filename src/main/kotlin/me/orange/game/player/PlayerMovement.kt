package me.orange.game.player

import me.orange.game.utils.Vec

class PlayerMovement(
    private val player: Player,
) {
    val world = player.game.world

    fun move(vec: Vec) = with(player) {
        if (falling) vec.x = 0

        val oldChunk = pos.toChunkPos()

        val newPos = pos + vec
        val allowed = if (falling && vec == Vec(0, -1)) {
            canWalkThrough(newPos, ignoreHead = true)
        } else {
            canWalkThrough(newPos)
        }

        if (!allowed) {
            if (canStepUp(pos, vec))
                pos.move(vec.plus(0, 1))
        } else {
            pos.move(vec)
        }

        // Only touch the occupant lists when the player actually crossed a chunk
        // boundary, so a blocked move never drops the player from the map.
        val newChunk = pos.toChunkPos()
        if (newChunk != oldChunk) {
            world.chunkManager.players[oldChunk]?.remove(player.id)
            world.chunkManager.players.getOrPut(newChunk) { mutableListOf() }.add(player.id)
        }
    }

    fun fall() = with(player) {
        val below = world.getTile(player.pos - Vec(0, 1))
        if (below?.airy == true) {
            falling = true
            move(Vec(0, -1))
        } else {
            falling = false
        }
    }
}