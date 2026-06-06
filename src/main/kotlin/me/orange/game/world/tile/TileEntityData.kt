package me.orange.game.world.tile

import kotlinx.serialization.Serializable
import me.orange.game.inventory.Inventory

@Serializable
sealed class TileEntityData {
    @Serializable
    data class ChestData(val inventory: Inventory.InventoryData) : TileEntityData()
}
