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

            "inventory" -> handleInventory(inputArgs[0])

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
                player.inventory.selectedSlot = if (player.gameMode == GameMode.PLACE) {
                    nextPlaceableSlot(player.inventory.selectedSlot, delta, size)
                } else {
                    (player.inventory.selectedSlot + delta + size) % size
                }
            }
            "up", "down" -> {
                val delta = if (arg == "down") InventoryRenderer.INVENTORY_COLS else -InventoryRenderer.INVENTORY_COLS
                val size = player.inventory.contents.size
                player.inventory.selectedSlot = (player.inventory.selectedSlot + delta + size) % size
            }
        }

        player.queueAction { player ->
            player.hook
                ?.editOriginalEmbeds(player.inventory.getEmbed()!!)
                ?.setComponents(player.getInventoryActions())
                ?.queue()
        }
    }

    private fun nextPlaceableSlot(fromSlot: Int, delta: Int, size: Int): Int {
        var candidate = (fromSlot + delta + size) % size
        var steps = 0
        while (steps < size) {
            if (player.inventory.contents[candidate].item.getTile() != null) return candidate
            candidate = (candidate + delta + size) % size
            steps++
        }
        return fromSlot
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