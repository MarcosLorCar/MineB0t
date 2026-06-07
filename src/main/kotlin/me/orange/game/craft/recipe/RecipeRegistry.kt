package me.orange.game.craft.recipe

import me.orange.game.inventory.item.Item
import me.orange.game.inventory.item.ItemStack

object RecipeRegistry {
    val recipes = mutableListOf<Recipe>()
    var recipeMap = mutableMapOf<String, MutableSet<Recipe>>()

    fun registerRecipe(recipe: Recipe) {
        recipes.add(recipe)

        for (ingredient in recipe.ingredients) {
            recipeMap.getOrPut(ingredient.itemKey) { mutableSetOf() }.add(recipe)
        }
    }

    /** Builder-DSL entry point mirroring `Tile.Builder` / `Item.Builder`; routes through [registerRecipe]. */
    fun register(id: String, block: Builder.() -> Unit): Recipe {
        val recipe = Builder(id).apply(block).build()
        registerRecipe(recipe)
        return recipe
    }

    class Builder(private val id: String) {
        private val ingredients = mutableListOf<ItemStack>()
        private var result: ItemStack? = null
        private var station: CraftingStationType = CraftingStationType.NONE

        fun station(type: CraftingStationType) = apply { station = type }
        fun ingredient(itemKey: String, count: Int = 1) = apply { ingredients.add(ItemStack(itemKey, count)) }
        fun output(itemKey: String, count: Int = 1) = apply { result = ItemStack(itemKey, count) }

        fun build(): Recipe = Recipe(
            id = id,
            ingredients = ingredients.toList(),
            result = result ?: error("Recipe '$id' has no output"),
            requiredStation = station,
        )
    }

    fun getRecipe(id: String): Recipe? {
        return recipes.find { it.id == id }
    }

    fun getRecipesByIngredient(item: Item): Set<Recipe> {
        return recipeMap[item.key] ?: emptySet()
    }
}
