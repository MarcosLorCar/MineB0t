package me.orange.game.chest

import me.orange.bot.Emojis
import me.orange.game.inventory.Inventory
import me.orange.game.inventory.InventoryRenderer
import me.orange.game.player.Player
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

class ChestRenderer(private val player: Player) {

    fun render(): Triple<MessageEmbed, MessageEmbed, List<LayoutComponent>>? {
        val chestPos = player.openChestPos ?: return null
        val chestData = player.game.world.getChestAt(chestPos) ?: return null
        val chestInv = Inventory.fromData(chestData.inventory)

        val playerEmbed = buildEmbed(
            title = "${Emojis.getFormatted("backpack")} Inventory",
            inventory = player.inventory,
            activeSlot = player.inventory.selectedSlot,
            showCursor = player.chestCursorOnPlayer,
        )
        val chestEmbed = buildEmbed(
            title = "${Emojis.getFormatted("chest")} Chest",
            inventory = chestInv,
            activeSlot = player.chestSelectedSlot,
            showCursor = !player.chestCursorOnPlayer,
        )

        return Triple(playerEmbed, chestEmbed, buildButtons())
    }

    private fun buildEmbed(title: String, inventory: Inventory, activeSlot: Int, showCursor: Boolean): MessageEmbed {
        val builder = EmbedBuilder().setTitle(title).setColor(InventoryRenderer.INVENTORY_COLOR)
        val air = Emojis.getFormatted("air")
        val cursor = Emojis.getFormatted("left")

        if (inventory.isEmpty()) {
            builder.setDescription("*Empty*")
        } else {
            inventory.contents.forEachIndexed { index, stack ->
                val indicator = if (showCursor && activeSlot == index) cursor else air
                val digits = stack.count.toString().map { c -> Emojis.getNumber(c.digitToInt()) }
                val countRow = if (digits.size == 1) "${digits[0]}$air" else digits.joinToString("")
                builder.addField("${stack.item.emoji.formatted}$indicator", countRow, true)
            }
            val remainder = inventory.contents.size % InventoryRenderer.INVENTORY_COLS
            if (remainder != 0) {
                repeat(InventoryRenderer.INVENTORY_COLS - remainder) {
                    builder.addField("$air$air", "$air$air", true)
                }
            }
        }

        return builder.build()
    }

    private fun buildButtons(): List<LayoutComponent> {
        val onPlayer = player.chestCursorOnPlayer
        val chestPos = player.openChestPos
        val chestEmpty = chestPos?.let { player.game.world.getChestAt(it)?.inventory?.contents?.isEmpty() } ?: true
        val playerEmpty = player.inventory.isEmpty()

        return listOf(
            ActionRow.of(
                Button.of(if (onPlayer) ButtonStyle.SUCCESS else ButtonStyle.SECONDARY, "chest_jumpInv", Emojis.get("backpack"))
                    .withDisabled(onPlayer),
                Button.of(ButtonStyle.PRIMARY, "chest_navUp", Emojis.get("up")),
                Button.of(ButtonStyle.SECONDARY, "chest_store", Emojis.get("store"))
                    .withDisabled(!onPlayer || playerEmpty),
            ),
            ActionRow.of(
                Button.of(ButtonStyle.PRIMARY, "chest_navLeft", Emojis.get("left")),
                Button.of(ButtonStyle.SECONDARY, "chest_close", Emojis.get("return")),
                Button.of(ButtonStyle.PRIMARY, "chest_navRight", Emojis.get("right")),
            ),
            ActionRow.of(
                Button.of(if (!onPlayer) ButtonStyle.SUCCESS else ButtonStyle.SECONDARY, "chest_jumpChest", Emojis.get("chest"))
                    .withDisabled(!onPlayer),
                Button.of(ButtonStyle.PRIMARY, "chest_navDown", Emojis.get("down")),
                Button.of(ButtonStyle.SECONDARY, "chest_retrieve", Emojis.get("retrieve"))
                    .withDisabled(onPlayer || chestEmpty),
            ),
        )
    }
}
