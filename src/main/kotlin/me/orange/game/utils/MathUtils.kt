package me.orange.game.utils

fun <T> List<List<T>>.safeGet(y: Int, x: Int): T? =
    getOrNull(y)?.getOrNull(x)

fun isPlayerTile(dx: Int, dy: Int) = dx == 0 && (dy == 0 || dy == 1)