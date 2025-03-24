package me.orange.game.world

enum class TileType(
    val emoji: String,
    val airy: Boolean = false,
    val breakable: Boolean = false,
) {
    AIR("<:03:1352306492723695736>", airy = true),
    GRASS("<:02:1352306218278064218>", breakable = true),
    DIRT("<:01:1352297912515694655>", breakable = true),
    NULL("❌"),
}