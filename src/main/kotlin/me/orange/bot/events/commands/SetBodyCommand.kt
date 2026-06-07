package me.orange.bot.events.commands

import me.orange.bot.events.base.SlashCommand
import me.orange.game.GamesManager
import me.orange.game.preferences.Preference
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.text.BreakIterator

object SetBodyCommand : SlashCommand(
    id = "set-body",
    description = "Set your body emoji to any emoji you like",
    options = listOf(
        OptionData(OptionType.STRING, "emoji", "The emoji to use as your body", true)
    ),
    execute = execute@{ hook, event ->
        val slashEvent = event as SlashCommandInteractionEvent
        val raw = slashEvent.getOption("emoji")?.asString?.trim() ?: return@execute

        if (!isValidBodyEmoji(raw)) {
            hook.editOriginal("Invalid input. Please provide a single emoji (e.g. 🦺 or a custom Discord emoji).").queue()
            return@execute
        }

        val game = GamesManager.getGame(slashEvent.guild!!.id)
        val userId = slashEvent.user.idLong
        game.preferencesManager.setPreference(userId, Preference.BODY_EMOJI, raw)
        game.preferencesManager.savePreferences(userId)
        game.playerEnvUiCache.remove(userId)
        hook.editOriginal("Body emoji set to $raw").queue()
    }
)

private fun isValidBodyEmoji(input: String): Boolean {
    if (input.isEmpty()) return false
    if (input.matches(Regex("<a?:[a-zA-Z0-9_]+:\\d+>"))) return true
    val bi = BreakIterator.getCharacterInstance()
    bi.setText(input)
    bi.first()
    val end = bi.next()
    if (end == BreakIterator.DONE || end != input.length) return false
    return input.codePointAt(0) > 0x2000
}
