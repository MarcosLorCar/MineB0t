package me.orange.game.craft

import me.orange.game.inventory.ItemStack
import me.orange.game.player.Player

class RecipeManager(
    val player: Player,
    knownRecipes: Set<String> = emptySet(),
) {
    var craftPage: Int = 0
    val knownRecipes: MutableSet<String> = knownRecipes.toMutableSet()

    data class RecipeCategories(
        val craftable: List<Recipe>,
        val semi: List<Recipe>,
        val known: List<Recipe>,
    ) {
        val all: List<Recipe> get() = craftable + semi + known
        fun isEmpty(): Boolean = all.isEmpty()
    }

    private fun isCraftable(recipe: Recipe): Boolean =
        recipe.ingredients.all { player.inventory.countOf(it.itemKey) >= it.count }

    private fun getSemiRecipes(): Set<Recipe> {
        val result = mutableSetOf<Recipe>()
        for (item in player.inventory.getUniqueSet()) {
            result.addAll(RecipeRegistry.getRecipesByIngredient(item))
        }
        result.forEach { knownRecipes.add(it.id) }
        return result
    }

    fun getViewableRecipes(): RecipeCategories {
        val semi = getSemiRecipes()
        val craftable = semi.filter { isCraftable(it) }.sortedBy { it.id }
        val semiOnly = semi.filter { !isCraftable(it) }.sortedBy { it.id }
        val known = knownRecipes
            .mapNotNull { RecipeRegistry.getRecipe(it) }
            .filter { it !in semi }
            .sortedBy { it.id }
        return RecipeCategories(craftable, semiOnly, known)
    }

    /** Returns true if there is anything to display in the crafting view. Also discovers new recipes. */
    fun hasViewableRecipes(): Boolean {
        getSemiRecipes()
        return knownRecipes.isNotEmpty()
    }

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
