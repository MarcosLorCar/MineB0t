package me.orange.game.inventory

import me.orange.game.utils.Vec

class Inventory(
    val size: Vec = Vec(5, 5),
    val contents: MutableList<ItemStack> = mutableListOf(),
) {
    fun addItem(item: ItemStack) {}
    fun hasItem(item: ItemStack): Boolean = false
    fun removeItem(item: ItemStack) {}
}