package me.orange.game.craft

import me.orange.game.craft.CraftingStationType.CRAFTING_TABLE
import me.orange.game.craft.CraftingStationType.FURNACE

/**
 * Recipe definitions. `object`s init lazily in Kotlin, so [count] is touched once at startup
 * (see `MineB0t.start`) to force these `register` calls to run and populate [RecipeRegistry].
 */
@Suppress("unused") // recipe vals exist only to run register() at object init; read via RecipeRegistry
object Recipes {
    val CRAFTING_TABLE_RECIPE = RecipeRegistry.register("crafting_table") {
        ingredient("dirt", 4)
        output("crafting_table", 1)
    }

    val FURNACE_RECIPE = RecipeRegistry.register("furnace") {
        station(CRAFTING_TABLE)
        ingredient("stone", 8)
        output("furnace", 1)
    }

    val IRON_INGOT_RECIPE = RecipeRegistry.register("iron_ingot") {
        station(FURNACE)
        ingredient("iron_chunk", 3)
        output("iron_ingot", 1)
    }

    val count: Int get() = RecipeRegistry.recipes.size
}
