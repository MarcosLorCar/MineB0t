package me.orange.game.player.action

import me.orange.bot.MineB0t
import me.orange.game.inventory.InventoryRenderer
import me.orange.game.player.GameMode
import me.orange.game.player.Player
import me.orange.game.player.ViewState
import me.orange.game.utils.Vec

class InputHandler(
    val player: Player
) {
    fun handle(input: String) {
        val inputType = input.split("_")[0]
        val inputArgs = input.split("_").drop(1)

        when (inputType) {
            "move" -> handleMove(inputArgs[0])

            "changeMode" -> handleChangeMode(inputArgs[0])

            "action" -> handleAction(inputArgs)
            "forceBreak" -> handleBreak(inputArgs)
            "forcePlace" -> handlePlace(inputArgs)
            "dual" -> {
                val sep = inputArgs.indexOf("and")
                if (sep < 0) return
                val vecH = inputArgs.take(sep).fold(Vec(0, 0)) { acc, s -> acc + getVecFromDir(s) }
                val vecD = inputArgs.drop(sep + 1).fold(Vec(0, 0)) { acc, s -> acc + getVecFromDir(s) }
                handleDualAction(vecH, vecD)
            }

            "inventory" -> handleInventory(inputArgs[0])
            "hotbar" -> handleHotbar(inputArgs[0])

            "craft" -> handleCraft(inputArgs[0])
        }
    }

    private fun handleInventory(arg: String) = MineB0t.launch {
        if (player.inventory.isEmpty()) return@launch

        when (arg) {
            "open" -> {
                player.queueAction { player ->
                    player.viewState = ViewState.INVENTORY
                }
            }
            "close" -> {
                recordSelectedAsRecent()
                player.viewState = ViewState.WORLD
                player.queueAction {
                    // Reset this player's view cache so that he gets the world rendered at least once
                    it.game.playerEnvUiCache.remove(player.id)
                }
                return@launch
            }
            "left", "right" -> {
                val delta = if (arg == "right") 1 else -1
                val size = player.inventory.contents.size
                player.inventory.selectedSlot = (player.inventory.selectedSlot + delta + size) % size
            }
            "up", "down" -> {
                val delta = if (arg == "down") 1 else -1
                val cols = InventoryRenderer.INVENTORY_COLS
                val size = player.inventory.contents.size
                val col = player.inventory.selectedSlot % cols
                val row = player.inventory.selectedSlot / cols
                val totalRows = (size + cols - 1) / cols
                val remainder = size % cols
                val rowsInCol = if (remainder == 0 || col < remainder) totalRows else totalRows - 1
                player.inventory.selectedSlot = ((row + delta + rowsInCol) % rowsInCol) * cols + col
            }
        }

        player.queueAction { player ->
            player.hook
                ?.editOriginalEmbeds(player.inventory.getEmbed()!!)
                ?.setComponents(player.getInventoryActions())
                ?.queue()
        }
    }

    private fun handleHotbar(arg: String) {
        val slotIndex = arg.toIntOrNull() ?: return
        if (slotIndex < 0 || slotIndex >= player.inventory.contents.size) return
        recordSelectedAsRecent()
        player.queueAction { p -> p.inventory.selectedSlot = slotIndex }
    }

    private fun recordSelectedAsRecent() {
        val key = player.inventory.getSelectedItemStack()?.itemKey ?: return
        player.recentItems.remove(key)
        player.recentItems.add(0, key)
        if (player.recentItems.size > 5) player.recentItems.removeAt(5)
    }

    private fun handleCraft(arg: String) = MineB0t.launch {
        // close must always work — even if crafting drained the last ingredient (no semi-recipes left),
        // otherwise the player is trapped in the crafting view.
        if (arg == "close") {
            player.queueAction {
                it.viewState = ViewState.WORLD
                // Reset both caches so the world is rendered at least once after leaving crafting
                it.game.playerEnvUiCache.remove(it.id)
                it.game.playerCraftUiCache.remove(it.id)
            }
            return@launch
        }

        if (!player.recipeManager.hasViewableRecipes()) return@launch

        when (arg) {
            "open" -> player.queueAction {
                it.recipeManager.craftPage = 0
                it.viewState = ViewState.CRAFTING
                it.game.playerCraftUiCache.remove(it.id)
            }
            "prev" -> player.queueAction { it.recipeManager.craftPage--; it.game.renderCrafting(it) }
            "next" -> player.queueAction { it.recipeManager.craftPage++; it.game.renderCrafting(it) }
        }
    }

    private fun handleAction(args: List<String>) {
        val actionType = player.gameMode
        val actionArgs = args

        when (actionType) {
            GameMode.PLACE -> handlePlace(actionArgs)

            GameMode.BREAK -> handleBreak(actionArgs)
        }
    }

    private fun handleDualAction(vecH: Vec, vecD: Vec) = player.queueAction { player ->
        when (player.gameMode) {
            GameMode.BREAK -> {
                player.breakTile(player, player.pos + vecH)
                player.breakTile(player, player.pos + vecD)
            }
            GameMode.PLACE -> {
                val item = player.inventory.getSelectedItemStack()?.item ?: return@queueAction
                player.placeTile(player, player.pos + vecH)
                // Only place diagonally if the same item still exists in inventory (prevents
                // auto-selected next stack from being consumed as the second placement)
                val idx = player.inventory.contents.indexOfFirst { it.item == item }
                if (idx >= 0) {
                    player.inventory.selectedSlot = idx
                    player.placeTile(player, player.pos + vecD)
                }
            }
        }
    }

    private fun handleBreak(args: List<String>) = player.queueAction { player ->
        val vec = args.fold(Vec(0, 0)) { acc, arg ->
            acc + getVecFromDir(arg)
        }

        player.breakTile(player, player.pos + vec)
    }

    private fun handlePlace(args: List<String>) = player.queueAction { player ->
        val vec = args.fold(Vec(0, 0)) { acc, arg ->
            acc + getVecFromDir(arg)
        }

        if (vec == Vec(0, -1)) {
            if (player.game.world.getTile(player.pos.plus(0, 2))?.airy == false) return@queueAction

            player.move(0, 1)
        }

        player.placeTile(player, player.pos + vec)
    }

    private fun handleChangeMode(input: String) {
        player.gameMode = when (input) {
            "place" -> GameMode.PLACE
            "break" -> GameMode.BREAK
            else -> return
        }
    }

    private fun handleMove(dir: String) = player.queueAction { player ->
        player.move(getVecFromDir(dir))
    }

    private fun getVecFromDir(dir: String): Vec =
        when (dir) {
            "down" -> Vec(0, -1)
            "left" -> Vec(-1, 0)
            "right" -> Vec(1, 0)
            "up" -> Vec(0, 1)
            else -> Vec(0, 0)
        }
}