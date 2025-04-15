package me.orange.game.inventory

import kotlinx.serialization.Serializable
import me.orange.game.utils.Vec

class Inventory(
    val size: Vec = Vec(5, 5),
    val contents: MutableList<ItemStack> = mutableListOf(),
) {
    private val renderer = InventoryRenderer(this)
    var selectedSlot: Int = 0

    fun addItem(itemStack: ItemStack) {
        var remaining = itemStack.count

        // Try to merge with existing stacks
        for (stack in contents) {
            if (stack.itemType == itemStack.itemType && stack.count < stack.itemType.maxCount) {
                val spaceAvailable = stack.itemType.maxCount - stack.count
                val toAdd = minOf(spaceAvailable, remaining)
                stack.count += toAdd
                remaining -= toAdd
                if (remaining == 0) return
            }
        }

        if (remaining > 0 && contents.size < size.x * size.y) {
            addNewItem(ItemStack(itemStack.itemType, remaining))
        }
    }

    fun addNewItem(itemStack: ItemStack) =
        contents.add(itemStack)

    fun getSelectedItemStack(): ItemStack? = contents.getOrNull(selectedSlot)

    fun getEmbed() = renderer.getEmbed()
    fun isEmpty(): Boolean = contents.isEmpty()
    fun getData(): InventoryData = InventoryData(size, contents)

    @Serializable
    data class InventoryData(
        val size: Vec,
        val contents: MutableList<ItemStack>,
    )

    companion object {
        fun fromData(data: InventoryData): Inventory = Inventory(data.size, data.contents)
    }
}