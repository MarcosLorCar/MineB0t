package me.orange.game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.orange.game.player.Player
import me.orange.game.utils.Vec
import me.orange.game.utils.surroundingChunks
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

    suspend fun update() {
        val currentTime = System.currentTimeMillis()

        // Load/Unload chunks
        players.forEach { (id, player) ->
            // Re-Load chunks around loaded players
            world.ensureChunksLoadedAround(player.pos, async = true)

            // Update player
            player.update()

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

        world.chunkManager.players.getOrPut(player.pos.toChunkPos()){mutableListOf()}.add(id)
        players[id] = player
        inputHandlers[id] = InputHandler(player)

        return player
    }

    fun updateHook(hook: InteractionHook) {
        val userId = hook.interaction.user.idLong
        val player = players.getOrPut(userId) { addPlayer(userId, hook) }

        val env = runBlocking(scope.coroutineContext) {getView(player)}
        val ui = player.getActions()

        hook.editOriginal(env)
            .setComponents(ui)
            .queue()
    }

    private suspend fun getView(player: Player): String {
        val view = world.getEnvironment(player)

        addPlayersToView(player, view)

        return view.joinToString("\n") { it.joinToString("") }
    }

    private fun addPlayersToView(
        player: Player,
        view: MutableList<MutableList<String>>
    ) {
        player.pos.surroundingChunks(1).forEach { chunk ->
            world.chunkManager.players[chunk]?.forEach {
                if (it == player.id) return@forEach

                val other = players[it]

                other?.pos?.toEnvPos(player)?.let { pos ->
                    pos.y = (view.size - 1) - pos.y

                    // Body
                    if (pos.y in view.indices && pos.x in view[0].indices)
                        view[pos.y][pos.x] = "\uD83E\uDDBA"

                    // Head
                    val headPos = pos.plus(0, 1)
                    if (headPos.y in view.indices && headPos.x in view[0].indices)
                        view[headPos.y][headPos.x] = "\uD83D\uDC7D"
                }
            }
        }
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

    fun breakTile(player: Player, vec: Vec) {
        world.setTile(vec, TileType.AIR)
    }
}
