package me.orange.bot.events.base

import kotlinx.coroutines.CoroutineScope
import me.orange.bot.MineB0t
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback

interface Interaction {
    val id: String
    val edit: Boolean
    val execute: suspend (InteractionHook, Any) -> Unit

    fun matches(id: String, event: Any): Boolean = this.id == id

    fun handle(event: Any) {
        if (edit) (event as IMessageEditCallback)
            .deferEdit()
            .queue { handleHook(it, event) }

        else (event as IReplyCallback)
            .deferReply()
            .setEphemeral(true)
            .queue { handleHook(it, event) }
    }

    private fun handleHook(hook: InteractionHook, event: Any) = MineB0t.launch {
        execute(hook, event)
    }
}

abstract class SlashCommand(
    override val id: String,
    val description: String,
    val options: List<net.dv8tion.jda.api.interactions.commands.build.OptionData> = emptyList(),
    override val execute: suspend (InteractionHook, Any) -> Unit
) : Interaction {
    override val edit: Boolean = false
    override fun matches(id: String, event: Any): Boolean = event is SlashCommandInteractionEvent && this.id == id
}

abstract class ButtonInteraction(
    override val id: String,
    override val edit: Boolean = true,
    override val execute: suspend (InteractionHook, Any) -> Unit
) : Interaction {
    override fun matches(id: String, event: Any): Boolean = event is ButtonInteractionEvent && this.id == id
}

abstract class StringSelectInteraction(
    override val id: String,
    override val edit: Boolean = true,
    override val execute: suspend (InteractionHook, Any) -> Unit
) : Interaction {
    override fun matches(id: String, event: Any): Boolean = event is StringSelectInteractionEvent && this.id == id
}
