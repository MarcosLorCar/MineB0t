package me.orange.game.player.action

import me.orange.game.player.Player
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Consumer

class PlayerActionQueue(
    private val player: Player,
) {
    private val actionQueue: Queue<Consumer<Player>> = ConcurrentLinkedQueue()

    fun applyQueuedActions() = with(player) {
        while (actionQueue.isNotEmpty()) {
            actionQueue.poll()?.accept(this)
        }
    }

    fun queueAction(action: (Player) -> Unit) {
        actionQueue.add(action)
    }
}