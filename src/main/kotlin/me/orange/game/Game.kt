package me.orange.game

import kotlinx.coroutines.*
import me.orange.game.GameRenderer.getView
import me.orange.game.player.InputHandler
import me.orange.game.player.Player
import me.orange.game.utils.Vec
import me.orange.game.world.TileType
import me.orange.game.world.World
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.InteractionHook
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

object Game {
    val scope = CoroutineScope(Dispatchers.Default)
    val world: World = World(Random.nextLong(), scope)
    var time: Long = 0
    val players: ConcurrentHashMap<Long, Player> = ConcurrentHashMap()
    private val playerEnvUiCache: MutableMap<Long, String> = HashMap()
    private val inputHandlers: ConcurrentHashMap<Long, InputHandler> = ConcurrentHashMap()
    private var running = true

    private const val PLAYER_TIMEOUT = 30_000L


    fun start() = scope.launch {
        while (running) {
            update()
            delay(1000) // 1 fps
        }
    }

    suspend fun update() {
        time++
        val currentTime = System.currentTimeMillis()

        // Load/Unload chunks
        players.forEach { (id, player) ->
            // Re-Load chunks around loaded players
            world.ensureChunksLoadedAround(player.pos, async = true)

            // Update player
            player.update(time)

            // Update hooks
            player.hook?.let {
                updateHook(it)
            }

            // Player timed out
            timeoutPlayer(currentTime, player, id)
        }
    }

    private fun timeoutPlayer(currentTime: Long, player: Player, id: Long) {
        if (currentTime - player.age < PLAYER_TIMEOUT) return

        println("Player timed out")
        player.hook?.deleteOriginal()?.queue()
        players.remove(id)

        scope.launch {
            player.saveData()
        }
    }

    fun addPlayer(id: Long, hook: InteractionHook): Player {
        val player = Player.loadPlayer(id) ?: Player(
            id,
            age = System.currentTimeMillis(),
            hook = hook,
            world = world,
        )

        world.chunkManager.players.getOrPut(player.pos.toChunkPos()){mutableListOf()}.add(id)
        players[id] = player

        return player
    }

    suspend fun updateHook(hook: InteractionHook, force: Boolean = false) {
        val userId = hook.interaction.user.idLong

        val player = players.getOrPut(userId) { addPlayer(userId, hook) }

        player.hook = hook

        val env = getView(player)

        val cache = playerEnvUiCache[userId]

        if (cache == null || cache != env || force) {
            val ui = player.getActions()

            val embed = EmbedBuilder()
                .setDescription(env)
                .build()

            hook.editOriginalEmbeds(embed)
                .setComponents(ui)
                .queue()

            playerEnvUiCache[userId] = env
        }
    }

    fun refreshPlayer(player: Player) {
        player.age = System.currentTimeMillis()
    }

    fun handleInput(hook: InteractionHook, string: String) {
        val userId = hook.interaction.user.idLong
        val player = players[userId] ?: return
        refreshPlayer(player)
        player.handle(string)
    }

    fun breakTile(player: Player, vec: Vec) {
        world.setTile(vec, TileType.AIR)
    }

    fun placeTile(player: Player, pos: Vec) {
        if (world.getTile(pos)?.airy == true)
            world.setTile(pos, TileType.DIRT)
    }
}
