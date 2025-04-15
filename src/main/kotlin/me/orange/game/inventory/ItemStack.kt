package me.orange.game.inventory

import kotlinx.serialization.Serializable

@Serializable
data class ItemStack(
    val itemType: ItemType,
    var count: Int,
)
