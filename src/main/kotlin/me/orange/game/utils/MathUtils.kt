package me.orange.game.utils

fun Pos.surroundingChunks(): Sequence<Pos> = sequence {
    for (dx in -1..1) {
        for (dy in -1..1) {
            yield(this@surroundingChunks + Pos(dx, dy))
        }
    }
}

fun <T> List<List<T>>.safeGet(y: Int, x: Int): T? =
    getOrNull(y)?.getOrNull(x)

fun isPlayerTile(dx: Int, dy: Int) = dx == 0 && (dy == 0 || dy == 1)