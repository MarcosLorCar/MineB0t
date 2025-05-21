package me.orange.game.craft

import me.orange.game.player.Player

class RecipeManager(
    val player: Player
) {
    var selectedSlot: Int = 0

    fun getSemiRecipes(): List<Recipe> {
        val recipes = mutableListOf<Recipe>()
        val items = player.inventory.getUniqueSet()

        for (recipe in RecipeRegistry.recipes) {
            for (ingredient in recipe.ingredients) {
                if (items.contains(ingredient.itemType)) {
                    recipes.add(recipe)
                    break
                }
            }
        }

        return recipes
    }
}
