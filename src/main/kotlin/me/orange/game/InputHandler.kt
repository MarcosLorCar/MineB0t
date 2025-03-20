@file:Suppress("EmptyMethod", "EmptyMethod", "EmptyMethod")

package me.orange.game

import me.orange.game.player.GameMode
import me.orange.game.player.Player
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
        }
    }

    private fun handleAction(args: List<String>) {
        val actionType = args[0]

        when (actionType) {
            "place" -> handlePlace(args[1])

            "break" -> handleBreak(args[1])
        }
    }

    private fun handleBreak(string: String) = player.queueAction { player ->
        val pos = getVecFromDir(string)

        if (pos.y == 0)
            Game.breakTile(player, player.pos + pos + Vec(0, 1))
        else
            player.falling = true

        Game.breakTile(player, player.pos + pos)
    }

    private fun handlePlace(dir: String) = player.queueAction { player ->
        val vec = getVecFromDir(dir)

        if (vec == Vec(0, -1)) {
            if (player.world.getTile(player.pos.plus(0, 2))?.airy == false) return@queueAction

            player.move(0, 1)
        }

        Game.placeTile(player, player.pos + vec)
    }

    private fun handleChangeMode(input: String) {
        player.mode = when (input) {
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
            "up" -> Vec(0, 2)
            else -> Vec(0, 0)
        }
}