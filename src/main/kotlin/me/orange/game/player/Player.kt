package me.orange.game.player

import me.orange.game.Game
import me.orange.game.player.data.PlayerDataManager
import me.orange.game.utils.GameMode
import me.orange.game.utils.Vec
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.LayoutComponent

class Player(
    val id: Long,
    val game: Game,
    var age: Long,
    var pos: Vec = Vec(0, 1),
    var hook: InteractionHook? = null,
    var gameMode: GameMode = GameMode.BREAK,
) {
    var falling = false

    companion object {
        fun loadPlayer(id: Long, game: Game): Player? {
            val data = PlayerDataManager.loadData(id, game)

            return if (data != null) {
                Player(
                    id = id,
                    game = game,
                    age = System.currentTimeMillis(),
                    pos = data.position,
                    gameMode = data.gameMode,
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
    fun canWalkThrough(vec: Vec): Boolean = actionMenu.canWalkThrough(vec)
    fun canStepUp(pos: Vec, move: Vec): Boolean = actionMenu.canStepUp(pos, move)
    fun fall(time: Long) = movement.fall(time)
    fun getActions(): MutableList<LayoutComponent> = actionMenu.getActions()
    fun queueAction(action: (Player) -> Unit) = actionQueue.queueAction(action)
    fun applyQueuedActions() = actionQueue.applyQueuedActions()
    fun placeTile(player: Player, pos: Vec) = game.placeTile(player, pos)
    fun breakTile(player: Player, vec: Vec) = game.breakTile(player, vec)
    fun handle(input: String) = inputHandler.handle(input)
    fun saveData() = playerDataManager.saveData()

    fun update(time: Long) {
        fall(time)
        applyQueuedActions()
    }

}