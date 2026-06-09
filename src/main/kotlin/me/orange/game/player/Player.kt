package me.orange.game.player

import me.orange.game.Game
import me.orange.game.craft.recipe.RecipeManager
import me.orange.game.inventory.Inventory
import me.orange.game.player.action.InputHandler
import me.orange.game.player.action.PlayerActionMenu
import me.orange.game.player.action.PlayerActionQueue
import me.orange.game.player.data.PlayerDataManager
import me.orange.game.player.offline.OfflinePlayer
import me.orange.game.preferences.PreferencesManager
import me.orange.game.utils.Vec
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.LayoutComponent

class Player(
    id: Long,
    pos: Vec = Vec(0, 1),
    gameMode: GameMode = GameMode.BREAK,
    val game: Game,
    var age: Long,
    var hook: InteractionHook? = null,
    val inventory: Inventory = Inventory(),
    knownRecipes: Set<String> = emptySet(),
    recentItems: List<String> = emptyList(),
) : OfflinePlayer(id, pos, gameMode) {
    val recipeManager = RecipeManager(this, knownRecipes)
    var falling = false
    var viewState: ViewState = ViewState.WORLD
        set(value) {
            if (field != value) {
                feedback = null
                feedbackItems.clear()
            }
            field = value
        }
    val recentItems: MutableList<String> = recentItems.toMutableList()
    var name: String = "unknown"
    var feedback: String? = null
    var feedbackExpiry: Long = 0
    private val feedbackItems: MutableList<me.orange.game.inventory.item.ItemStack> = mutableListOf()
    var openChestPos: Vec? = null
    var chestSelectedSlot: Int = 0
    var chestCursorOnPlayer: Boolean = true

    companion object {
        fun loadPlayer(id: Long, game: Game): Player? {
            val data = PlayerDataManager.loadData(id, game)

            return if (data != null) {
                PreferencesManager.loadPreferences(id, data.preferences)
                Player(
                    id = id,
                    game = game,
                    age = game.time,
                    pos = data.position,
                    gameMode = data.gameMode,
                    inventory = Inventory.fromData(data.inventoryData),
                    knownRecipes = data.knownRecipes,
                    recentItems = data.recentItems,
                )
            } else null
        }

        val emojis = listOf(
            "\uD83D\uDC37",
            "\uD83D\uDC54"
        )

        var zoom: Pair<Int, Int> = Pair(7, 5)
    }

    private val movement = PlayerMovement(this)
    private val inputHandler = InputHandler(this)
    private val actionMenu = PlayerActionMenu(this)
    private val actionQueue = PlayerActionQueue(this)
    private val playerDataManager = PlayerDataManager(this)

    fun move(vec: Vec) = movement.move(vec)
    fun move(x: Int, y: Int) = move(Vec(x, y))
    fun canWalkThrough(vec: Vec, ignoreHead: Boolean = false): Boolean = actionMenu.canWalkThrough(vec, ignoreHead)
    fun canStepUp(pos: Vec, move: Vec): Boolean = actionMenu.canStepUp(pos, move)
    fun fall() = movement.fall()
    fun getActions(): MutableList<LayoutComponent> = actionMenu.getActions()
    fun getInventoryActions(): MutableList<LayoutComponent> = actionMenu.getInventoryActions()
    fun queueAction(action: (Player) -> Unit) = actionQueue.queueAction(action)
    fun applyQueuedActions() = actionQueue.applyQueuedActions()
    fun placeTile(player: Player, pos: Vec): Boolean = game.placeTile(player, pos)
    fun breakTile(player: Player, vec: Vec) = game.breakTile(player, vec)
    fun handle(input: String) = inputHandler.handle(input)
    fun saveData() = playerDataManager.saveData()

    fun clearFeedbackItems() = feedbackItems.clear()

    fun addPickupFeedback(itemStack: me.orange.game.inventory.item.ItemStack) {
        if (game.time > feedbackExpiry) {
            feedbackItems.clear()
        }

        val existing = feedbackItems.find { it.itemKey == itemStack.itemKey }
        if (existing != null) {
            existing.count += itemStack.count
        } else {
            feedbackItems.add(me.orange.game.inventory.item.ItemStack(itemStack.itemKey, itemStack.count))
        }

        feedback = feedbackItems.joinToString(", ") { "+ ${it.count}x ${it.item.emoji.formatted}" }
        feedbackExpiry = game.time + me.orange.bot.Config.FPS * 3
    }

    fun update() {
        fall()
        applyQueuedActions()
    }


}