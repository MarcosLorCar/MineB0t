package me.orange.game.utils

import kotlinx.serialization.Serializable
import me.orange.game.world.chunk.Chunk.Companion.SIZE
import java.lang.Math.floorDiv

@Serializable
data class Pos(var x: Int, var y: Int) {
    operator fun plus(other: Pos) = Pos(x + other.x, y + other.y)
    operator fun minus(other: Pos) = Pos(x - other.x, y - other.y)
    operator fun times(i: Int) = Pos(x * i, y * i)

    fun toChunkPos(): Pos = Pos(floorDiv(x, SIZE), floorDiv(y, SIZE))
    fun toWorldPos(chunkPos: Pos) = Pos(x + chunkPos.x * SIZE, y + chunkPos.y * SIZE)
    fun toLocalPos(): Pos = Pos(x.mod(SIZE), y.mod(SIZE))
    fun move(x: Int, y: Int) {
        this.x += x
        this.y += y
    }
}
