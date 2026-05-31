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
            if (stack.itemKey == itemStack.itemKey && stack.count < stack.item.maxCount) {
                val spaceAvailable = stack.item.maxCount - stack.count
                val toAdd = minOf(spaceAvailable, remaining)
                stack.count += toAdd
                remaining -= toAdd
                if (remaining == 0) return
            }
        }

        if (remaining > 0 && contents.size < size.x * size.y) {
            addNewItem(ItemStack(itemStack.itemKey, remaining))
        }
    }

    fun addNewItem(itemStack: ItemStack) =
        contents.add(itemStack)

    fun getSelectedItemStack(): ItemStack? = contents.getOrNull(selectedSlot)

    fun countOf(itemKey: String): Int = contents.filter { it.itemKey == itemKey }.sumOf { it.count }

    /** True if [stack] can be added without overflowing (space in existing stacks or a free slot). */
    fun canFit(stack: ItemStack): Boolean {
        val spaceInExisting = contents.filter { it.itemKey == stack.itemKey }.sumOf { it.item.maxCount - it.count }
        if (spaceInExisting >= stack.count) return true
        return contents.size < size.x * size.y
    }

    /** Removes [count] of [itemKey] across stacks, dropping emptied stacks; clamps [selectedSlot]. */
    fun removeItems(itemKey: String, count: Int) {
        var remaining = count
        val iterator = contents.iterator()
        while (iterator.hasNext() && remaining > 0) {
            val stack = iterator.next()
            if (stack.itemKey != itemKey) continue
            val taken = minOf(stack.count, remaining)
            stack.count -= taken
            remaining -= taken
            if (stack.count == 0) iterator.remove()
        }
        if (selectedSlot >= contents.size) selectedSlot = maxOf(0, contents.size - 1)
    }

    fun getEmbed() = renderer.getEmbed()
    fun isEmpty(): Boolean = contents.isEmpty()
    fun getData(): InventoryData = InventoryData(size, contents)

    fun getUniqueSet(): Set<Item> {
        return contents.map { it.item }.toSet()
    }

    @Serializable
    data class InventoryData(
        val size: Vec,
        val contents: MutableList<ItemStack>,
    )

    companion object {
        fun fromData(data: InventoryData): Inventory = Inventory(data.size, data.contents)
    }
}