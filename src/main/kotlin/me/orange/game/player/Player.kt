package me.orange.game.player

import me.orange.game.utils.Pos
import me.orange.game.world.World
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

class Player(
    @Suppress("unused") val id: Long,
    val world: World,
    var age: Long,
    val hook: InteractionHook?
) {
    var pos : Pos = Pos(0, 5)
    var mode = GameMode.BREAK
    var zoom: Pair<Int, Int> = Pair(7, 5)
    val emojis = listOf(
        "\uD83D\uDC37",
        "\uD83D\uDC54"
    )

    fun fall() {
        world.getTile(pos - Pos(0, 1))?.let { tile ->
            if (tile.airy) {
                pos.move(0, -1)
            }
        }
    }

    fun getActions(): MutableList<LayoutComponent> {
        val actions = mutableListOf<LayoutComponent>()

        actions.add(ActionRow.of(
            getMoveButtons()
        ))

        actions.add(ActionRow.of(
            getActionButtons()
        ))

        return actions
    }

    fun getModeButton(): Button {
        val style = if (mode == GameMode.PLACE) ButtonStyle.SUCCESS else ButtonStyle.DANGER
        val emoji = if (mode == GameMode.PLACE) Emoji.fromUnicode("\uD83D\uDD28") else Emoji.fromUnicode("⛏\uFE0F")
        val newMode = if (mode == GameMode.PLACE) "break" else "place"

        return Button.of(style, "changeMode_$newMode", emoji)
    }

    private fun getMoveButtons(): List<Button> {
        return listOf(
            moveButton(Pos(-1, 0), "left", "◀\uFE0F"),
            moveButton(Pos(0, -1), "down", "\uD83D\uDD3D"),
            moveButton(Pos(1, 0), "right", "▶\uFE0F"),
        )
    }

    private fun getActionButtons(): List<Button> {
        return if(mode == GameMode.PLACE) listOf(
            actionButton(Pos(-1, 0), "place_left", "\uD83E\uDEF2", ButtonStyle.SUCCESS),
            actionButton(Pos(0, 0), "place_down", "\uD83E\uDEF3", ButtonStyle.SUCCESS),
            actionButton(Pos(1, 0), "place_right", "\uD83E\uDEF1", ButtonStyle.SUCCESS),
            getModeButton(),
        ) else listOf(
            actionButton(Pos(-1, 0), "break_left", "\uD83E\uDD1B", ButtonStyle.DANGER),
            actionButton(Pos(0, 2), "break_up", "✊", ButtonStyle.DANGER),
            actionButton(Pos(1, 0), "break_right", "\uD83E\uDD1C", ButtonStyle.DANGER),
            getModeButton(),
        )
    }

    private fun moveButton(move: Pos, inputStr: String, emojiCode: String): Button =
        Button.of(ButtonStyle.SECONDARY, "move_$inputStr", Emoji.fromUnicode(emojiCode)).withDisabled(run {
            val nextPos = pos + move

            return@run !((mode == GameMode.BREAK && canBreakThrough(nextPos)) ||
                    (mode == GameMode.PLACE && canWalkThrough(nextPos)))
        })

    private fun actionButton(direction: Pos, inputStr: String, emojiCode: String, style: ButtonStyle): Button =
        Button.of(style, "action_$inputStr", Emoji.fromUnicode(emojiCode)).withDisabled(run {
            return@run false
        })

    fun canWalkThrough(pos: Pos): Boolean {
        val tileBottom = world.getTile(pos) ?: return false
        val tileTop = world.getTile(pos + Pos(0, 1)) ?: return false

        return tileTop.airy && tileBottom.airy
    }

    fun canBreakThrough(pos: Pos): Boolean {
        val tileBottom = world.getTile(pos) ?: return false
        val tileTop = world.getTile(pos + Pos(0, 1)) ?: return false

        return (tileTop.breakable || tileTop.airy) && (tileBottom.breakable || tileBottom.airy)
    }
}