package me.orange.game.craft

import me.orange.game.inventory.Item

object RecipeRegistry {
    val recipes = mutableListOf<Recipe>()
    var recipeMap = mutableMapOf<String, MutableSet<Recipe>>()

    fun registerRecipe(recipe: Recipe) {
        recipes.add(recipe)

        for (ingredient in recipe.ingredients) {
            recipeMap.getOrPut(ingredient.itemKey) { mutableSetOf() }.add(recipe)
        }
    }

    fun getRecipe(id: String): Recipe? {
        return recipes.find { it.id == id }
    }

    fun getRecipesByIngredient(item: Item): Set<Recipe> {
        return recipeMap[item.key] ?: emptySet()
    }
}
