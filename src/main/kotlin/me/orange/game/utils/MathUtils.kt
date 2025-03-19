package me.orange.game.utils

fun Pos.surroundingChunks(size: Int = 1): Sequence<Pos> = sequence {
    for (dx in -size..size) {
        for (dy in -size..size) {
            yield(this@surroundingChunks + Pos(dx, dy))
        }
    }
}

fun <T> List<List<T>>.safeGet(y: Int, x: Int): T? =
    getOrNull(y)?.getOrNull(x)

fun isPlayerTile(dx: Int, dy: Int) = dx == 0 && (dy == 0 || dy == 1)