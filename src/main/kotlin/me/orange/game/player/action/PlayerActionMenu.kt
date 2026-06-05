package me.orange.game.player.action

import me.orange.bot.Emojis
import me.orange.game.craft.RecipeRegistry
import me.orange.game.player.Player
import me.orange.game.player.GameMode
import me.orange.game.preferences.Preference
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

        val uiMode = player.game.preferencesManager.getPreference<String>(player.id, Preference.UI_MODE)
        val moreActions = uiMode == "extended" || uiMode == "extended_hotbar"
        val showHotbar = uiMode == "hotbar" || uiMode == "extended_hotbar"

        // Row 1 (extended modes only)
        if (moreActions) {
            actions.add(
                ActionRow.of(
                    getPlaceholderButton(),
                    actionButton("left_up_up", "up_left", style),
                    actionButton("up_up", "up", style),
                    actionButton("right_up_up", "up_right", style),
                    getInventoryButton(),
                )
            )
        }

        val row2Center = when {
            !moreActions -> actionButton("up_up", "up", style)
            gameMode == GameMode.PLACE -> forceBreakButton("up_up", "up")
            else -> getModeButton()
        }
        val row3Center = if (moreActions && gameMode == GameMode.BREAK) forcePlaceButton("down", "down") else getModeButton()

        // Row 1 (default) / Row 2 (more actions)
        actions.add(
            ActionRow.of(
                if (moreActions) dualActionButton("left_and_up_left", "left_and_up_left", style) else getPlaceholderButton(),
                actionButton("up_left", "left", style),
                row2Center,
                actionButton("up_right", "right", style),
                if (moreActions) dualActionButton("right_and_up_right", "right_and_up_right", style) else getInventoryButton(),
            )
        )

        // Row 2 / Row 3
        actions.add(
            ActionRow.of(
                moveButton(Vec(-1, 0), "left"),
                actionButton("left", "left", style),
                row3Center,
                actionButton("right", "right", style),
                moveButton(Vec(1, 0), "right"),
            )
        )

        // Row 3 / Row 4
        actions.add(
            ActionRow.of(
                getCraftingButton(),
                actionButton("down_left", "down_left", style),
                actionButton("down", "down", style),
                actionButton("down_right", "down_right", style),
                getInventoryPreviewButton(),
            )
        )

        // Bottom row: recent/ordered hotbar
        if (showHotbar) {
            actions.add(ActionRow.of(buildHotbarSlots().map { slotIndex ->
                if (slotIndex == null) return@map getPlaceholderButton()
                val stack = player.inventory.contents[slotIndex]
                Button.of(ButtonStyle.SECONDARY, "hotbar_$slotIndex", " ${stack.count}", stack.item.emoji)
            }))
        }

        return actions
    }

    private fun buildHotbarSlots(): List<Int?> {
        val result = mutableListOf<Int?>()
        val used = mutableSetOf(player.inventory.selectedSlot)

        for (key in player.recentItems) {
            if (result.size >= 5) break
            val idx = player.inventory.contents.indexOfFirst { it.itemKey == key }
            if (idx >= 0 && used.add(idx)) result.add(idx)
        }

        for (idx in player.inventory.contents.indices) {
            if (result.size >= 5) break
            if (used.add(idx)) result.add(idx)
        }

        while (result.size < 5) result.add(null)
        return result
    }

    fun getInventoryActions(): MutableList<LayoutComponent> {
        val actions = mutableListOf<LayoutComponent>()

        // Row 1: [info] [↑] [close]
        actions.add(ActionRow.of(
            getItemInfoButton(),
            inventoryNavButton("up"),
            Button.of(ButtonStyle.SECONDARY, "inventory_close", Emojis.get("return")),
        ))

        // Row 2: [←] [preview] [→]
        actions.add(ActionRow.of(
            inventoryNavButton("left"),
            getInventoryPreviewButton(),
            inventoryNavButton("right"),
        ))

        // Row 3: [recipes] [↓] [capacity]
        actions.add(ActionRow.of(
            getItemRecipesButton(),
            inventoryNavButton("down"),
            getCapacityButton(),
        ))

        return actions
    }

    private fun inventoryNavButton(direction: String): Button =
        Button.of(ButtonStyle.PRIMARY, "inventory_$direction", Emojis.get(direction))

    @OptIn(ExperimentalUuidApi::class)
    fun getPlaceholderButton(): Button =
        Button.of(ButtonStyle.SECONDARY, Uuid.random().toString(), Emojis.get("null")).asDisabled()

    fun getModeButton(): Button = with(player) {
        val style = if (gameMode == GameMode.PLACE) ButtonStyle.SUCCESS else ButtonStyle.DANGER
        val emoji = if (gameMode == GameMode.PLACE) Emojis.get("place") else Emojis.get("break")
        val newMode = if (gameMode == GameMode.PLACE) "break" else "place"

        return Button.of(style, "changeMode_$newMode", emoji)
    }

    fun getInventoryButton(): Button =
        Button.of(ButtonStyle.SECONDARY, "inventory_open", Emojis.get("backpack"))
        .withDisabled(player.inventory.isEmpty())

    fun getCraftingButton(): Button {
        // Theme the icon to whatever station the player is standing on (NONE → generic craft icon).
        val station = player.game.world.getCraftingStationAt(player.pos)
        return Button.of(ButtonStyle.SECONDARY, "craft_open", Emojis.get(station.emojiKey))
            .withDisabled(!player.recipeManager.hasViewableRecipes())
    }

    fun getInventoryPreviewButton(): Button {
        val selectedItemStack = player.inventory.getSelectedItemStack()
        return if (selectedItemStack == null)
            Button.of(ButtonStyle.SECONDARY, "inventoryPreview", Emojis.get("null")).asDisabled()
        else
            Button.of(ButtonStyle.SECONDARY, "inventoryPreview", " ${selectedItemStack.count}", selectedItemStack.item.emoji).asDisabled()
    }

    private fun getItemInfoButton(): Button {
        val hasItem = player.inventory.getSelectedItemStack() != null
        return Button.of(ButtonStyle.SECONDARY, "itemInfo", Emojis.get("info")).withDisabled(!hasItem)
    }

    private fun getCapacityButton(): Button {
        val used = player.inventory.contents.size
        val max = player.inventory.size.x * player.inventory.size.y
        return Button.of(ButtonStyle.SECONDARY, "inventoryCapacity", "$used/$max", Emojis.get("backpack")).asDisabled()
    }

    private fun getItemRecipesButton(): Button {
        val item = player.inventory.getSelectedItemStack()?.item
        val hasRecipes = item != null && RecipeRegistry.getRecipesByIngredient(item).isNotEmpty()
        return Button.of(ButtonStyle.SECONDARY, "itemRecipes", Emojis.get("crafting_table")).withDisabled(!hasRecipes)
    }

    private fun moveButton(move: Vec, inputStr: String): Button = with(player) {
        Button.of(
            ButtonStyle.PRIMARY,
            "move_$inputStr",
            Emojis.get("move_$inputStr"),
        ).withDisabled(!run {
            // Determine if the button should be enabled

            val nextPos = pos + move

            val canWalk = canWalkThrough(nextPos)
            val canStepUp = canStepUp(pos, move)

            return@run (canWalk || canStepUp)
        })
    }

    private fun dualActionButton(inputStr: String, emojiCode: String, style: ButtonStyle): Button =
        Button.of(style, "dual_$inputStr", Emojis.get(emojiCode))
            .withDisabled(player.gameMode == GameMode.PLACE && player.inventory.getSelectedItemStack()?.item?.getTile() == null)

    private fun forceBreakButton(inputStr: String, emojiCode: String): Button =
        Button.of(ButtonStyle.DANGER, "forceBreak_$inputStr", Emojis.get(emojiCode))

    private fun forcePlaceButton(inputStr: String, emojiCode: String): Button =
        Button.of(ButtonStyle.SUCCESS, "forcePlace_$inputStr", Emojis.get(emojiCode))
            .withDisabled(player.inventory.getSelectedItemStack()?.item?.getTile() == null)

    private fun actionButton(inputStr: String, emojiCode: String, style: ButtonStyle): Button {
        return Button.of(style, "action_$inputStr", Emojis.get(emojiCode))
            .withDisabled(!run {
                // return ENABLED state

                return@run when (player.gameMode) {
                    GameMode.BREAK -> true
                    GameMode.PLACE -> player.inventory.getSelectedItemStack()?.item?.getTile() != null
                }
            })
    }

    fun canWalkThrough(vec: Vec): Boolean = with(player) {
        val tileBottom = game.world.getTile(vec) ?: return false
        val tileTop = game.world.getTile(vec + Vec(0, 1)) ?: return false

        return tileTop.airy && tileBottom.airy
    }
    fun canStepUp(pos: Vec, move: Vec): Boolean = canWalkThrough(pos.plus(0, 1)) && canWalkThrough((pos + move).plus(0, 1))
}