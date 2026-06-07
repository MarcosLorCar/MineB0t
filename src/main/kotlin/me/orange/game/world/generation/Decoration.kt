package me.orange.game.world.generation

import me.orange.game.utils.Vec
import me.orange.game.world.tile.Tile
import java.util.*

interface Decoration {
    val salt: Long
    fun generate(origin: Vec, rng: Random, getTile: (Vec) -> Tile?, setTile: (Vec, Tile) -> Unit)
}
