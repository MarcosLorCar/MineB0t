package me.orange.game.inventory

object Items {
    val GRASS          = ItemRegistry.register("grass")          { placeable("grass"); description("Surface block found on top of dirt") }
    val DIRT           = ItemRegistry.register("dirt")           { placeable("dirt"); description("Common underground block") }
    val STONE          = ItemRegistry.register("stone")          { placeable("stone"); description("Found deep underground") }
    val IRON_CHUNK     = ItemRegistry.register("iron_chunk")     { description("Raw iron ore, needs smelting") }
    val RED_SHROOM     = ItemRegistry.register("red_shroom")     { placeable("red_shroom_tile"); description("Wild red shroom, found in the surface") }
    @Suppress("unused")
    val IRON_INGOT     = ItemRegistry.register("iron_ingot")     { description("Smelted iron used in crafting") }
    val CRAFTING_TABLE = ItemRegistry.register("crafting_table") { placeable("crafting_table"); description("Unlocks crafting recipes") }
    val FURNACE        = ItemRegistry.register("furnace")        { placeable("furnace"); description("Used to smelt ores") }
    val COAL           = ItemRegistry.register("coal")           { description("Fuel for the furnace") }

    val count: Int get() = ItemRegistry.count
}
