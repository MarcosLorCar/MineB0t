package me.orange.game.world

enum class TileType(
    val emoji: String,
    val airy: Boolean = false,
    val breakable: Boolean = false,
) {
    AIR("⬛", airy = true),
    GRASS("\uD83D\uDFE9", breakable = true),
    DIRT("🟫", breakable = true),
    NULL("❌"),
}