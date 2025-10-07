package me.orange.game.craft

import me.orange.game.inventory.ItemType

object RecipeRegistry {
    val recipes = mutableListOf<Recipe>()
    var recipeMap = mutableMapOf<ItemType, MutableSet<Recipe>>()

    fun registerRecipe(recipe: Recipe) {
        recipes.add(recipe)
        
        for (ingredient in recipe.ingredients) {
            recipeMap.getOrPut(ingredient.itemType) { mutableSetOf() }.add(recipe)
        }
    }

    fun getRecipe(id: String): Recipe? {
        return recipes.find { it.id == id }
    }

    fun getRecipesByIngredient(itemType: ItemType): Set<Recipe> {
        return recipeMap[itemType] ?: emptySet()
    }
}