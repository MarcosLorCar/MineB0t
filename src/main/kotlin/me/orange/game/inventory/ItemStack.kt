package me.orange.game.inventory

import kotlinx.serialization.Serializable

@Serializable
data class ItemStack(
    val itemKey: String,
    var count: Int,
) {
    val item: Item get() = Items.get(itemKey)
    constructor(item: Item, count: Int) : this(item.key, count)
}
