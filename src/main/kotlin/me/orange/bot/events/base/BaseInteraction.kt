package me.orange.bot.events.base

import me.orange.bot.MineB0t
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback

abstract class BaseInteraction(
    val id: String,
    val getIdentifier: (Event) -> String?,
    val edit: Boolean,
    val execute : suspend (InteractionHook, Event) -> Unit
) : ListenerAdapter() {
    override fun onGenericEvent(event: GenericEvent) {
        handleEvent(event as Event)
    }

    fun handleEvent(event: Event) {
        if (getIdentifier(event) != id) return

        if (edit) (event as IMessageEditCallback)
            .deferEdit()
            .queue { handleHook(it, event) }

        else (event as IReplyCallback)
            .deferReply()
            .setEphemeral(true)
            .queue { handleHook(it, event) }
    }

    fun handleHook(hook: InteractionHook, event: Event) = MineB0t.launch { execute(hook, event) }
}