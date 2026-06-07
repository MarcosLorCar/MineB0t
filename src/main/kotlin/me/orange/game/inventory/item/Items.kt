package me.orange.game.inventory.item

import me.orange.bot.Emojis
import me.orange.game.world.tile.Tiles

object Items {
    val GRASS          = ItemRegistry.register("grass",          Emojis.get("grass"))          { placeable { Tiles.GRASS };          description("Surface block found on top of dirt") }
    val DIRT           = ItemRegistry.register("dirt",           Emojis.get("dirt"))           { placeable { Tiles.DIRT };           description("Common underground block") }
    val STONE          = ItemRegistry.register("stone",          Emojis.get("stone"))          { placeable { Tiles.STONE };          description("Found deep underground") }
    val IRON_CHUNK     = ItemRegistry.register("iron_chunk",     Emojis.get("iron_chunk"))     { description("Raw iron ore, needs smelting") }
    val RED_SHROOM     = ItemRegistry.register("red_shroom",     Emojis.get("red_shroom"))     { placeable { Tiles.RED_SHROOM };     description("Wild red shroom, found in the surface") }
    val LOG            = ItemRegistry.register("log",            Emojis.get("log"))            { placeable { Tiles.LOG };            description("Wood log from trees") }
    @Suppress("unused")
    val IRON_INGOT     = ItemRegistry.register("iron_ingot",     Emojis.get("iron_ingot"))     { description("Smelted iron used in crafting") }
    val FURNACE        = ItemRegistry.register("furnace",        Emojis.get("furnace"))        { placeable { Tiles.FURNACE };        description("Used to smelt ores") }
    val COAL           = ItemRegistry.register("coal",           Emojis.get("coal"))           { description("Fuel for the furnace") }
    val CHEST          = ItemRegistry.register("chest",          Emojis.get("chest"))          { placeable { Tiles.CHEST };          description("Storage container") }

    val count: Int get() = ItemRegistry.count
}
