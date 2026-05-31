package me.orange.game.craft

import me.orange.game.inventory.ItemStack
import me.orange.game.player.Player

class RecipeManager(
    val player: Player
) {
    /** Current page in the crafting view; clamped at render time by `CraftingRenderer`. */
    var craftPage: Int = 0

    /** Recipes for which the player holds at least one ingredient item (drives the craft button + view). */
    fun getSemiRecipes(): Set<Recipe> {
        val recipes = mutableSetOf<Recipe>()
        val items = player.inventory.getUniqueSet()

        for (item in items) {
            val matchingRecipes = RecipeRegistry.getRecipesByIngredient(item)
            recipes.addAll(matchingRecipes)
        }

        return recipes
    }

    /** Stable, ordered list backing pagination + the select menu. */
    fun getViewableRecipes(): List<Recipe> = getSemiRecipes().sortedBy { it.id }

    /**
     * Attempts to craft [recipeId]: validates station, ingredient counts and output space,
     * then consumes ingredients, adds the output and persists. Returns whether it succeeded.
     */
    fun craft(recipeId: String): Boolean {
        val recipe = RecipeRegistry.getRecipe(recipeId) ?: return false

        if (recipe.requiredStation != CraftingStationType.NONE &&
            player.game.world.getCraftingStationAt(player.pos) != recipe.requiredStation
        ) return false

        if (recipe.ingredients.any { player.inventory.countOf(it.itemKey) < it.count }) return false
        if (!player.inventory.canFit(recipe.result)) return false

        recipe.ingredients.forEach { player.inventory.removeItems(it.itemKey, it.count) }
        player.inventory.addItem(ItemStack(recipe.result.itemKey, recipe.result.count))
        player.saveData()
        return true
    }
}
