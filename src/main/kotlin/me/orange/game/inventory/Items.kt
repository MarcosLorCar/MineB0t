package me.orange.game.inventory

object Items {
    private val registry = mutableListOf<Item>()
    private val keyMap = mutableMapOf<String, Item>()

    val GRASS          = register("grass")          { placeable("grass") }
    val DIRT           = register("dirt")           { placeable("dirt") }
    val STONE          = register("stone")          { placeable("stone") }
    val IRON_CHUNK     = register("iron_chunk")     { }
    @Suppress("unused")
    val IRON_INGOT     = register("iron_ingot")     { }
    val CRAFTING_TABLE = register("crafting_table") { placeable("crafting_table") }
    val FURNACE        = register("furnace")        { placeable("furnace") }

    fun register(key: String, block: Item.Builder.() -> Unit): Item {
        val item = Item.Builder(key).apply(block).build()
        registry.add(item)
        keyMap[key] = item
        return item
    }

    fun get(key: String): Item = keyMap[key] ?: error("Unknown item key '$key'")
}
