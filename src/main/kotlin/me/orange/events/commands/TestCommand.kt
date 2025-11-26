package me.orange.events.commands

import me.orange.events.base.SlashCommand

object TestCommand : SlashCommand(
    id = "test2",
    description = "This is a test command",
    execute = { hook, event ->
        hook.setEphemeral(false)
        hook.editOriginal("<@javiercgom04> no va a ir a Londres sin su hermano").queue()
    }
)