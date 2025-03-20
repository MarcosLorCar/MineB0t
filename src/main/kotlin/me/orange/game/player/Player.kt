package me.orange.game.player

import me.orange.game.utils.Vec
import me.orange.game.world.World
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.LayoutComponent

class Player(
    val id: Long, val world: World, var age: Long, val hook: InteractionHook?
) {
    var falling = false
    var pos: Vec = Vec(0, 1)
    var mode = GameMode.BREAK
    var zoom: Pair<Int, Int> = Pair(7, 5)
    val emojis = listOf("\uD83D\uDC37", "\uD83D\uDC54")

    private val movement = PlayerMovement(this)
    private val actionMenu = PlayerActionMenu(this)
    private val actionQueue = PlayerActionQueue(this)

    fun move(vec: Vec) = movement.move(vec)
    fun move(x: Int, y: Int) = move(Vec(x, y))
    fun fall() = movement.fall()
    fun getActions(): MutableList<LayoutComponent> = actionMenu.getActions()
    fun queueAction(action: (Player) -> Unit) = actionQueue.queueAction(action)
    fun applyQueuedActions() = actionQueue.applyQueuedActions()
    fun placeBlock(vec: Vec) {}

    fun update() {
        fall()
        applyQueuedActions()
    }

}