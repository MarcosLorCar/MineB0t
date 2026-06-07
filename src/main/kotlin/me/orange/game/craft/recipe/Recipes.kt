package me.orange.game.craft.recipe

import me.orange.game.craft.recipe.CraftingStationType.FURNACE

/**
 * Recipe definitions. `object`s init lazily in Kotlin, so [count] is touched once at startup
 * (see `MineB0t.start`) to force these `register` calls to run and populate [RecipeRegistry].
 */
@Suppress("unused") // recipe vals exist only to run register() at object init; read via RecipeRegistry
object Recipes {
    val FURNACE_RECIPE = RecipeRegistry.register("furnace") {
        ingredient("stone", 16)
        output("furnace", 1)
    }

    val CHEST_RECIPE = RecipeRegistry.register("chest") {
        ingredient("iron_ingot", 1)
        ingredient("log", 4)
        output("chest", 1)
    }

    val IRON_INGOT_RECIPE = RecipeRegistry.register("iron_ingot") {
        station(FURNACE)
        ingredient("coal", 2)
        ingredient("iron_chunk", 2)
        output("iron_ingot", 1)
    }

    val count: Int get() = RecipeRegistry.recipes.size
}
