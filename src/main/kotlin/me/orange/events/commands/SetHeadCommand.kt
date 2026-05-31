package me.orange.events.commands

import me.orange.events.base.SlashCommand
import me.orange.game.GamesManager
import me.orange.game.preferences.Preference
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

object SetHeadCommand : SlashCommand(
    id = "settings-head",
    description = "Set your head emoji to any emoji you like",
    options = listOf(
        OptionData(OptionType.STRING, "emoji", "The emoji to use as your head", true)
    ),
    execute = execute@{ hook, event ->
        event as SlashCommandInteractionEvent
        val raw = event.getOption("emoji")?.asString?.trim() ?: return@execute

        if (!isValidHeadEmoji(raw)) {
            hook.editOriginal("Invalid input. Please provide a single emoji (e.g. 🦊 or a custom Discord emoji).").queue()
            return@execute
        }

        val game = GamesManager.getGame(event.guild!!.id)
        game.preferencesManager.setPreference(event.user.idLong, Preference.HEAD_EMOJI, raw)
        hook.editOriginal("Head emoji set to $raw").queue()
    }
)

private fun isValidHeadEmoji(input: String): Boolean {
    if (input.isEmpty()) return false
    // Custom Discord emoji: <:name:id> or <a:name:id>
    if (input.matches(Regex("<a?:[a-zA-Z0-9_]+:\\d+>"))) return true
    // Single unicode grapheme cluster with first codepoint in emoji range (above basic Latin)
    val bi = java.text.BreakIterator.getCharacterInstance()
    bi.setText(input)
    bi.first()
    val end = bi.next()
    if (end == java.text.BreakIterator.DONE || end != input.length) return false
    return input.codePointAt(0) > 0x2000
}
