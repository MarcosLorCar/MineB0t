package me.orange.game

import me.orange.game.player.GameMode
import me.orange.game.player.Player
import me.orange.game.utils.Pos

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

    private fun handleBreak(string: String) {
        val pos = getPosFromDir(string)

        if (pos.y == 0)
            Game.breakTile(player, player.pos + pos + Pos(0, 1))

        Game.breakTile(player, player.pos + pos)
    }

    private fun handlePlace(dir: String) {

    }

    private fun handleChangeMode(input: String) {
        player.mode = when (input) {
            "place" -> GameMode.PLACE
            "break" -> GameMode.BREAK
            else -> return
        }
    }

    private fun handleMove(dir: String) {
        player.pos += getPosFromDir(dir)

        if (player.mode == GameMode.BREAK && player.world.getTile(player.pos)?.breakable == true) {
            Game.breakTile(player, player.pos)
            Game.breakTile(player, player.pos + Pos(0, 1))
        }
    }

    private fun getPosFromDir(dir: String): Pos =
        when (dir) {
            "down" -> Pos(0, -1)
            "left" -> Pos(-1, 0)
            "right" -> Pos(1, 0)
            "up" -> Pos(0, 2)
            else -> Pos(0, 0)
        }
}