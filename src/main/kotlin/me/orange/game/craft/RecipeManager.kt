package me.orange.game.craft

import me.orange.game.player.Player

class RecipeManager(
    val player: Player
) {
    fun getSemiRecipes(): Set<Recipe> {
        val recipes = mutableSetOf<Recipe>()
        val items = player.inventory.getUniqueSet()

        for (item in items) {
            val matchingRecipes = RecipeRegistry.getRecipesByIngredient(item)
            recipes.addAll(matchingRecipes)
        }

        return recipes
    }
}
