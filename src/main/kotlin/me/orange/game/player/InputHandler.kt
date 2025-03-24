package me.orange.game.player

import me.orange.game.utils.GameMode
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