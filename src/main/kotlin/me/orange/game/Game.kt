package me.orange.game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.orange.bot.Config
import me.orange.game.data.GameDataManager
import me.orange.game.inventory.ItemStack
import me.orange.game.player.Player
import me.orange.game.player.ViewState
import me.orange.game.player.action.InputHandler
import me.orange.game.player.offline.OfflinePlayer
import me.orange.game.utils.Vec
import me.orange.game.world.TileType
import me.orange.game.world.World
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.InteractionHook
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class Game(
    guildId : String,
    seed: Long? = null,
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    val gameDataDir: String = "${Config.GAME_DATA_DIR}/$guildId",
    var time: Long = 0
) {
    var world: World = World(this, seed ?: Random.nextLong(), scope)
    val gameDataManager = GameDataManager(this)
    val players: ConcurrentHashMap<Long, OfflinePlayer> = ConcurrentHashMap()
    val playerEnvUiCache: MutableMap<Long, String> = HashMap()
    private val gameRenderer: GameRenderer = GameRenderer(this)
    private val inputHandlers: ConcurrentHashMap<Long, InputHandler> = ConcurrentHashMap()
    private var running = true

    companion object {
        private const val FPS = 2
        private const val PLAYER_TIMEOUT = 30 // Seconds
    }

    suspend fun run() {
        while (running) {
            update()
            delay((1000 / FPS).toLong())
        }
    }

    suspend fun update() {
        time++

        players.forEach { (id, player) ->
            if (player !is Player) return@forEach

            // Re-Load chunks around loaded players
            world.ensureChunksLoadedAround(player.pos, async = true)

            // Update player
            player.update()

            // Update hooks
            player.hook?.let {
                updateHook(it)
            }

            /*
            // Test
            val str = StringBuilder("Players near ${player.id}:")
            player.pos.toChunkPos().let { chunkPos ->
                world.chunkManager.players[chunkPos]?.forEach { otherPlayer ->
                    str.append(" $otherPlayer")
                }
            }
            println(str.toString()) */

            // Player timed out
            timeoutPlayer(player, id)
        }

        world.chunkManager.unloadUnusedChunks()
    }

     fun timeoutPlayer(player: Player, id: Long, force: Boolean = false) {
        if (!force && time - player.age < (PLAYER_TIMEOUT * FPS)) return

        println("Player timed out")
        player.hook?.deleteOriginal()?.queue()
        players.put(id, OfflinePlayer(player.id, player.pos, player.gameMode))

        scope.launch {
            player.saveData()
        }
    }

    suspend fun addPlayer(id: Long, hook: InteractionHook): Player {
        val player = Player.loadPlayer(id, this) ?: Player(
            id,
            pos = world.generateSpawnPoint(),
            age = time,
            hook = hook,
            game = this,
        )

        world.chunkManager.players.getOrPut(player.pos.toChunkPos()){mutableListOf()}.add(id)
        players[id] = player

        return player
    }

    private suspend fun getOrCreatePlayer(userId: Long, hook: InteractionHook): Player {
        var player = players.getOrPut(userId) { addPlayer(userId, hook) }
        if (player !is Player) player = addPlayer(userId, hook)
        player.hook = hook
        return player
    }

    suspend fun showWorldToHook(hook: InteractionHook) {
        val userId = hook.interaction.user.idLong
        var player = getOrCreatePlayer(userId, hook)
        player.viewState = ViewState.WORLD
        updateHook(hook, true)
    }

    suspend fun updateHook(hook: InteractionHook, force: Boolean = false) {
        val userId = hook.interaction.user.idLong
        var player = getOrCreatePlayer(userId, hook)

        if (player.viewState == ViewState.WORLD) {
            val env = gameRenderer.getView(player)

            val cache = playerEnvUiCache[userId]

            if (cache == null || cache != env || force) {
                val ui = player.getActions()

                val embed = EmbedBuilder()
                    .setDescription(env.plus("\n ${player.pos}"))
                    .build()

                player.hook?.editOriginalEmbeds(embed)
                    ?.setComponents(ui)
                    ?.queue()

                playerEnvUiCache[userId] = env
            }
        }
    }

    fun refreshPlayer(player: Player) {
        player.age = time
    }

    fun handleInput(hook: InteractionHook, string: String) {
        val userId = hook.interaction.user.idLong
        val player = players[userId] ?: return
        if (player !is Player) return
        refreshPlayer(player)
        player.handle(string)
    }

    fun breakTile(player: Player, pos: Vec) {
        world.getTile(pos)?.item?.let { item ->
            player.inventory.addItem(ItemStack(item, 1))
        }
        world.setTile(pos, TileType.AIR)
    }

    fun placeTile(player: Player, pos: Vec) {
        if (world.getTile(pos)?.airy != true) return
        val stack = player.inventory.getSelectedItemStack() ?: return

        world.setTile(pos, stack.itemType.getTileType())
        stack.count--
        if (stack.count == 0) {
            player.inventory.contents.remove(stack)
            if (player.inventory.getSelectedItemStack() == null)
                player.inventory.selectedSlot = 0
        }
    }

    fun saveAll() {
        players.filter { it.value is Player }.forEach { (id, player) ->
            (player as Player).saveData()
        }

        world.chunkManager.saveChunks()

        gameDataManager.saveGameData()
    }
}
