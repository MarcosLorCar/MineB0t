package me.orange.game.inventory

object Items {
    val GRASS          = ItemRegistry.register("grass")          { placeable("grass") }
    val DIRT           = ItemRegistry.register("dirt")           { placeable("dirt") }
    val STONE          = ItemRegistry.register("stone")          { placeable("stone") }
    val IRON_CHUNK     = ItemRegistry.register("iron_chunk")     { }
    @Suppress("unused")
    val IRON_INGOT     = ItemRegistry.register("iron_ingot")     { }
    val CRAFTING_TABLE = ItemRegistry.register("crafting_table") { placeable("crafting_table") }
    val FURNACE        = ItemRegistry.register("furnace")        { placeable("furnace") }
    val COAL           = ItemRegistry.register("coal")           { }

    val count: Int get() = ItemRegistry.count
}
