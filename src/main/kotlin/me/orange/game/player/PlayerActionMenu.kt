package me.orange.game.player

import me.orange.Emojis
import me.orange.game.utils.GameMode
import me.orange.game.utils.Vec
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PlayerActionMenu(
    private val player: Player,
) {
    fun getActions(): MutableList<LayoutComponent> = with(player) {
        val actions = mutableListOf<LayoutComponent>()
        val style = if (gameMode == GameMode.BREAK) ButtonStyle.DANGER else ButtonStyle.SUCCESS

        // Row 1
        actions.add(
            ActionRow.of(
                getPlaceholderButton(),
                actionButton("up_left", "up_left", style),
                actionButton("up_up", "up", style),
                actionButton("up_right", "up_right", style),
                getPlaceholderButton(),
            )
        )

        // Row 2
        actions.add(
            ActionRow.of(
                moveButton(Vec(-1, 0), "left", "◀\uFE0F"),
                actionButton("left", "left", style),
                getModeButton(),
                actionButton("right", "right", style),
                moveButton(Vec(1, 0), "right", "▶\uFE0F"),
            )
        )

        // Row 3
        actions.add(
            ActionRow.of(
                getPlaceholderButton(),
                actionButton("down_left", "down_left", style),
                actionButton("down", "down", style),
                actionButton("down_right", "down_right", style),
                getPlaceholderButton(),
            )
        )

        return actions
    }

    @OptIn(ExperimentalUuidApi::class)
    fun getPlaceholderButton(): Button =
        Button.of(ButtonStyle.SECONDARY, Uuid.random().toString(), Emojis.getEmoji("null")).asDisabled()

    fun getModeButton(): Button = with(player) {
        val style = if (gameMode == GameMode.PLACE) ButtonStyle.SUCCESS else ButtonStyle.DANGER
        val emoji = if (gameMode == GameMode.PLACE) Emojis.getEmoji("place") else Emojis.getEmoji("break")
        val newMode = if (gameMode == GameMode.PLACE) "break" else "place"

        return Button.of(style, "changeMode_$newMode", emoji)
    }

    private fun moveButton(move: Vec, inputStr: String, emojiCode: String): Button = with(player) {
        Button.of(
            ButtonStyle.PRIMARY,
            "move_$inputStr",
            Emojis.getCustom("move_$inputStr"),
        ).withDisabled(!run {
            // Determine if button should be enabled

            val nextPos = pos + move

            val canWalk = canWalkThrough(nextPos)
            val canStepUp = canStepUp(pos, move)

            return@run (canWalk || canStepUp)
        })
    }

    private fun actionButton(inputStr: String, emojiCode: String, style: ButtonStyle): Button {
        return Button.of(style, "action_$inputStr", Emojis.getCustom(emojiCode))
    }

    fun canWalkThrough(vec: Vec): Boolean = with(player) {
        val tileBottom = world.getTile(vec) ?: return false
        val tileTop = world.getTile(vec + Vec(0, 1)) ?: return false

        return tileTop.airy && tileBottom.airy
    }
    fun canStepUp(pos: Vec, move: Vec): Boolean = canWalkThrough(pos.plus(0, 1)) && canWalkThrough((pos + move).plus(0, 1))
}