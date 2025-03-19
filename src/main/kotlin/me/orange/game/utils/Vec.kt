package me.orange.game.utils

import kotlinx.serialization.Serializable
import me.orange.game.player.Player
import me.orange.game.world.chunk.Chunk.Companion.SIZE
import java.lang.Math.floorDiv

@Serializable
data class Vec(var x: Int, var y: Int) {
    operator fun plus(other: Vec) = plus(other.x, other.y)
    fun plus(x: Int, y: Int) = Vec(this.x + x, this.y + y)
    operator fun minus(other: Vec) = minus(other.x, other.y)
    fun minus(x: Int, y: Int) = Vec(this.x - x, this.y - y)

    fun toChunkPos(): Vec = Vec(floorDiv(x, SIZE), floorDiv(y, SIZE))
    fun toWorldPos(chunkVec: Vec) = Vec(x + chunkVec.x * SIZE, y + chunkVec.y * SIZE)
    fun toLocalPos(): Vec = Vec(x.mod(SIZE), y.mod(SIZE))

    fun move(x: Int, y: Int) {
        this.x += x
        this.y += y
    }
    fun move(vec: Vec) = move(vec.x, vec.y)

    fun toEnvPos(player: Player): Vec? =
        Vec(x - player.pos.x + player.zoom.first, y - player.pos.y + player.zoom.second)
}
