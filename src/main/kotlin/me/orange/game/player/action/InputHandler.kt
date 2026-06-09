package me.orange.game.player.action

import me.orange.bot.MineB0t
import me.orange.game.inventory.Inventory
import me.orange.game.inventory.InventoryRenderer
import me.orange.game.player.GameMode
import me.orange.game.player.Player
import me.orange.game.player.ViewState
import me.orange.game.utils.Vec
import me.orange.game.world.tile.Tiles

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
            "chest" -> handleChest(inputArgs[0])
            "ladder" -> handleLadder(inputArgs[0])
        }
    }

    private fun handleInventory(arg: String) = MineB0t.launch {
        if (player.inventory.isEmpty()) return@launch

        when (arg) {
            "open" -> {
                player.queueAction { player ->
                    player.viewState = ViewState.INVENTORY
                    player.game.playerInventoryUiCache.remove(player.id)
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
            player.game.renderInventory(player)
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

    private fun handleChest(arg: String) = MineB0t.launch {
        when (arg) {
            "open" -> {
                val chestPos = player.game.world.getChestPosAt(player.pos) ?: return@launch
                player.game.world.ensureChestAt(chestPos)
                player.queueAction { p ->
                    p.openChestPos = chestPos
                    p.chestSelectedSlot = 0
                    p.chestCursorOnPlayer = true
                    p.viewState = ViewState.CHEST
                    p.game.playerChestUiCache.remove(p.id)
                }
            }

            "close" -> player.queueAction { p ->
                p.openChestPos = null
                p.viewState = ViewState.WORLD
                p.game.playerEnvUiCache.remove(p.id)
                p.game.playerChestUiCache.remove(p.id)
            }

            "jumpInv" -> {
                if (!player.chestCursorOnPlayer) player.queueAction { p ->
                    p.chestCursorOnPlayer = true
                    p.game.renderChest(p)
                }
            }

            "jumpChest" -> {
                if (player.chestCursorOnPlayer) player.queueAction { p ->
                    p.chestCursorOnPlayer = false
                    p.game.renderChest(p)
                }
            }

            "navLeft", "navRight" -> {
                val delta = if (arg == "navRight") 1 else -1
                player.queueAction { p ->
                    if (p.chestCursorOnPlayer) {
                        val size = p.inventory.contents.size
                        if (size > 0)
                            p.inventory.selectedSlot = (p.inventory.selectedSlot + delta + size) % size
                    } else {
                        val chestData = p.game.world.getChestAt(p.openChestPos ?: return@queueAction) ?: return@queueAction
                        val size = chestData.inventory.contents.size
                        if (size > 0)
                            p.chestSelectedSlot = (p.chestSelectedSlot + delta + size) % size
                    }
                    p.game.renderChest(p)
                }
            }

            "navUp", "navDown" -> {
                val delta = if (arg == "navDown") 1 else -1
                val cols = InventoryRenderer.INVENTORY_COLS
                player.queueAction { p ->
                    if (p.chestCursorOnPlayer) {
                        val size = p.inventory.contents.size
                        if (size > 0) {
                            val col = p.inventory.selectedSlot % cols
                            val row = p.inventory.selectedSlot / cols
                            val totalRows = (size + cols - 1) / cols
                            val remainder = size % cols
                            val rowsInCol = if (remainder == 0 || col < remainder) totalRows else totalRows - 1
                            p.inventory.selectedSlot = ((row + delta + rowsInCol) % rowsInCol) * cols + col
                        }
                    } else {
                        val chestData = p.game.world.getChestAt(p.openChestPos ?: return@queueAction) ?: return@queueAction
                        val size = chestData.inventory.contents.size
                        if (size > 0) {
                            val col = p.chestSelectedSlot % cols
                            val row = p.chestSelectedSlot / cols
                            val totalRows = (size + cols - 1) / cols
                            val remainder = size % cols
                            val rowsInCol = if (remainder == 0 || col < remainder) totalRows else totalRows - 1
                            p.chestSelectedSlot = ((row + delta + rowsInCol) % rowsInCol) * cols + col
                        }
                    }
                    p.game.renderChest(p)
                }
            }

            "store" -> player.queueAction { p ->
                if (!p.chestCursorOnPlayer) return@queueAction
                val chestData = p.game.world.getChestAt(p.openChestPos ?: return@queueAction) ?: return@queueAction
                val selected = p.inventory.getSelectedItemStack() ?: return@queueAction
                val chestInv = Inventory.fromData(chestData.inventory)
                if (!chestInv.canFit(selected)) return@queueAction
                chestInv.addItem(selected)
                p.inventory.removeFromSlot(p.inventory.selectedSlot, selected.count)
                p.game.renderChest(p)
            }

            "retrieve" -> player.queueAction { p ->
                if (p.chestCursorOnPlayer) return@queueAction
                val chestData = p.game.world.getChestAt(p.openChestPos ?: return@queueAction) ?: return@queueAction
                val chestInv = Inventory.fromData(chestData.inventory)
                val selected = chestInv.contents.getOrNull(p.chestSelectedSlot) ?: return@queueAction
                if (!p.inventory.canFit(selected)) return@queueAction
                p.inventory.addItem(selected)
                chestInv.removeFromSlot(p.chestSelectedSlot, selected.count)
                if (p.chestSelectedSlot >= chestData.inventory.contents.size)
                    p.chestSelectedSlot = maxOf(0, chestData.inventory.contents.size - 1)
                p.game.renderChest(p)
            }
        }
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
            val world = player.game.world
            val targetPos = player.pos // Where the tile will end up (relative to new pos)
            val currentTile = world.getTile(targetPos) ?: return@queueAction
            val stack = player.inventory.getSelectedItemStack() ?: return@queueAction

            // Conditions to allow towering (instantly jumping up to place a block at your feet):
            // 1. Target tile must be AIR (per the new airy restriction)
            // 2. There must be room for your head after moving up
            // 3. You must have a placeable item
            if (currentTile != Tiles.AIR ||
                world.getTile(player.pos.plus(0, 2))?.isPassable == false ||
                stack.item.getTile() == null
            ) {
                return@queueAction
            }

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

    private fun handleLadder(arg: String) {
        if (arg != "teleport") return

        player.queueAction { player ->
            val world = player.game.world
            val x = player.pos.x

            // Search climbable tiles around player vertically
            val startY = listOf(player.pos.y, player.pos.y - 1, player.pos.y + 1)
                .firstOrNull { y -> world.getTile(Vec(x, y))?.climbable == true } ?: return@queueAction

            // Scan up and down to find the bounds of the ladder segment
            var topLadderY = startY
            while (world.getTile(Vec(x, topLadderY + 1))?.climbable == true) {
                topLadderY++
            }

            var bottomLadderY = startY
            while (world.getTile(Vec(x, bottomLadderY - 1))?.climbable == true) {
                bottomLadderY--
            }

            // Calculate midpoint
            val midpoint = (bottomLadderY + topLadderY) / 2.0

            // Determine target position
            val targetY = if (player.pos.y.toDouble() > midpoint) {
                // Player is closer to top (or standing on top). Teleport to bottom of segment.
                bottomLadderY
            } else {
                // Player is closer to bottom (or standing inside bottom tile). Teleport to top of segment.
                // Try to teleport to the block immediately above the top ladder tile (standing on top).
                // If that's blocked, teleport inside the topmost ladder tile itself.
                if (player.canWalkThrough(Vec(x, topLadderY + 1))) {
                    topLadderY + 1
                } else {
                    topLadderY
                }
            }

            // Only teleport if position actually changes and the target is walkable
            if (targetY != player.pos.y && player.canWalkThrough(Vec(x, targetY))) {
                val oldChunk = player.pos.toChunkPos()
                player.pos.y = targetY
                val newChunk = player.pos.toChunkPos()
                if (newChunk != oldChunk) {
                    world.chunkManager.removePlayer(player.id)
                    world.chunkManager.players.getOrPut(newChunk) { mutableListOf() }.add(player.id)
                }
                player.game.playerEnvUiCache.remove(player.id)
            }
        }
    }
}