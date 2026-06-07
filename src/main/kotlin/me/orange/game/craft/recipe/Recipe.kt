package me.orange.game.craft.recipe

import me.orange.game.craft.CraftingStationType
import me.orange.game.inventory.item.ItemStack

data class Recipe(
    val id: String,
    val ingredients: List<ItemStack>,
    val result: ItemStack,
    val requiredStation: CraftingStationType = CraftingStationType.NONE,
)
