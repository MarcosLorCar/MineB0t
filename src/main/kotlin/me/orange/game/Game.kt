package me.orange.game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.orange.game.player.Player
import me.orange.game.utils.Pos
import me.orange.game.world.TileType
import me.orange.game.world.World
import net.dv8tion.jda.api.interactions.InteractionHook
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

object Game {
    val world: World = World(Random.nextLong())
    private val players: ConcurrentHashMap<Long, Player> = ConcurrentHashMap()
    private val inputHandlers: ConcurrentHashMap<Long, InputHandler> = ConcurrentHashMap()
    private var running = true

    private const val PLAYER_TIMEOUT = 30_000L

    private val scope = CoroutineScope(Dispatchers.Default)

    fun start() = scope.launch {
        while (running) {
            update()
            delay(1000) // 1 fps
        }
    }

    fun update() {
        val currentTime = System.currentTimeMillis()

        // Load/Unload chunks
        players.forEach { (id, player) ->
            // Re-Load chunks around loaded players
            world.ensureChunksLoadedAround(player.pos, async = true)

            // Apply gravity
            player.fall()

            // Update hooks
            player.hook?.let(::updateHook)

            // Player timed out
            if (currentTime - player.age >= PLAYER_TIMEOUT) {
                println("Player timed out")
                player.hook?.deleteOriginal()?.queue()
                players.remove(id)
                inputHandlers.remove(id)
            }
        }
    }

    fun addPlayer(id: Long, hook: InteractionHook): Player {
        val player = Player(
            id,
            age = System.currentTimeMillis(),
            hook = hook,
            world = world,
        )

        players[id] = player
        inputHandlers[id] = InputHandler(player)

        return player
    }

    fun updateHook(hook: InteractionHook) {
        val userId = hook.interaction.user.idLong
        val player = players.getOrPut(userId) { addPlayer(userId, hook) }

        val env = world.getEnvironment(player)
        val ui = player.getActions()

        hook.editOriginal(env)
            .setComponents(ui)
            .queue()
    }

    fun refreshPlayer(userId: Long) {
        val player = players[userId]
        player?.age = System.currentTimeMillis()
    }

    fun handleInput(hook: InteractionHook, string: String) {
        val userId = hook.interaction.user.idLong
        refreshPlayer(userId)
        inputHandlers[userId]?.handle(string)
    }

    fun breakTile(player: Player, pos: Pos) {
        world.setTile(pos, TileType.AIR)
    }
}
