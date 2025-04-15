package me.orange.events.commands

import me.orange.events.base.SlashCommand
import me.orange.game.inventory.Inventory
import me.orange.game.inventory.ItemStack
import me.orange.game.inventory.ItemType
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

object TestCommand : SlashCommand(
    name = "test",
    description = "This is a test command"
) {

    override fun execute(event: SlashCommandInteractionEvent) {
        val inventory = Inventory(
            contents = mutableListOf(
                ItemStack(ItemType.DIRT, 13),
                ItemStack(ItemType.DIRT, 6),
                ItemStack(ItemType.GRASS, 15),
                ItemStack(ItemType.GRASS, 15),
                ItemStack(ItemType.GRASS, 15),
                ItemStack(ItemType.GRASS, 15),
                ItemStack(ItemType.GRASS, 15),
                ItemStack(ItemType.GRASS, 15),
                ItemStack(ItemType.GRASS, 15),
            ),
        )

        event.replyEmbeds(inventory.getEmbed()!!)
            .queue()
    }
}