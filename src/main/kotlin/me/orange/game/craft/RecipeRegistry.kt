package me.orange.game.craft

object RecipeRegistry {
    val recipes = mutableListOf<Recipe>()

    fun registerRecipe(recipe: Recipe) {
        recipes.add(recipe)
    }

    fun getRecipe(id: String): Recipe? {
        return recipes.find { it.id == id }
    }
}