package me.orange.game

import kotlinx.coroutines.delay
import me.orange.bot.Config
import me.orange.bot.MineB0t
import me.orange.game.chest.ChestRenderer
import me.orange.game.craft.CraftingRenderer
import me.orange.game.gameData.GameDataManager
import me.orange.game.inventory.item.ItemStack
import me.orange.game.player.Player
import me.orange.game.player.ViewState
import me.orange.game.player.offline.OfflinePlayer
import me.orange.game.preferences.Preference
import me.orange.game.preferences.PreferencesManager
import me.orange.game.utils.Vec
import me.orange.game.world.World
import me.orange.game.world.tile.TileInteraction
import me.orange.game.world.tile.Tiles
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.InteractionHook
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class Game(
    val guildId : String,
    seed: Long? = null,
    val gameDataDir: String = "${Config.GAME_DATA_DIR}/$guildId",
    var time: Long = 0
) {
    val preferencesManager get() = PreferencesManager
    var guildName: String = guildId
    var world: World = World(this, seed ?: Random.nextLong())
    val gameDataManager = GameDataManager(this)
    val players: ConcurrentHashMap<Long, OfflinePlayer> = ConcurrentHashMap()
    val playerEnvUiCache: MutableMap<Long, String> = ConcurrentHashMap()
    val playerCraftUiCache: MutableMap<Long, String> = ConcurrentHashMap()
    val playerChestUiCache: MutableMap<Long, String> = ConcurrentHashMap()
    val playerInventoryUiCache: MutableMap<Long, String> = ConcurrentHashMap()
    private val gameRenderer: GameRenderer = GameRenderer(this)
    private var running = true

    companion object {
        private const val FRAME_MS = 1000L / Config.FPS
    }

    suspend fun run() {
        while (running) {
            val start = System.currentTimeMillis()
            update()
            val elapsed = System.currentTimeMillis() - start
            delay(maxOf(0L, FRAME_MS - elapsed).milliseconds)
        }
    }

    suspend fun update() {
        time++

        players.forEach { (id, player) ->
            if (player !is Player) return@forEach

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

        // Gravity for offline players (only when their chunk is already loaded)
        players.forEach { (id, player) ->
            if (player is Player) return@forEach
            val feet = world.getTile(player.pos) ?: return@forEach
            val oldChunk = player.pos.toChunkPos()
            if (feet.climbable) {
                // Standing/holding onto climbable, do not move/fall
            } else if (!feet.isPassable) {
                // Inside a block — push up one tile
                player.pos.move(0, 1)
            } else {
                val below = world.getTile(player.pos.minus(0, 1)) ?: return@forEach
                if (below.isPassable && !below.climbable) {
                    // Floating — fall down one tile
                    player.pos.move(0, -1)
                }
            }
            val newChunk = player.pos.toChunkPos()
            if (newChunk != oldChunk) {
                world.chunkManager.players[oldChunk]?.remove(id)
                world.chunkManager.players.getOrPut(newChunk) { mutableListOf() }.add(id)
            }
        }

        world.chunkManager.unloadUnusedChunks()

        if (time % Config.AUTOSAVE_TICKS == 0L) saveAll()
    }

     fun timeoutPlayer(player: Player, id: Long, force: Boolean = false) {
        val timeoutTicks = preferencesManager.getPreference<Int>(player.id, Preference.PLAYER_TIMEOUT) * Config.FPS
        if (!force && time - player.age < timeoutTicks) return

         MineB0t.log("Player ${player.name} (${player.id}) timed out in guild $guildName ($guildId)")
         player.hook?.deleteOriginal()?.queue()
         players[id] = OfflinePlayer(player.id, player.pos, player.gameMode)

         playerEnvUiCache.remove(id)
         playerCraftUiCache.remove(id)
         playerChestUiCache.remove(id)
         playerInventoryUiCache.remove(id)

         MineB0t.launch {
            player.saveData()
        }
    }

    suspend fun addPlayer(id: Long, hook: InteractionHook): Player {
        val savedPlayer = Player.loadPlayer(id, this)
        val isNew = savedPlayer == null
        val player = savedPlayer ?: Player(
            id,
            pos = world.generateSpawnPoint(),
            age = time,
            hook = hook,
            game = this,
        ).apply {
            if (Config.DEV_MODE) {
                Config.STARTING_KIT.forEach { (item, count) ->
                    inventory.addItem(ItemStack(item, count))
                }
            }
        }
        player.name = hook.interaction.user.name
        guildName = hook.interaction.guild?.name ?: guildId
        MineB0t.log("Player ${player.name} ($id) ${if (isNew) "joined" else "reconnected to"} guild $guildName ($guildId)")

        // Ensure the neighbourhood is loaded, then settle to a safe position in case the
        // saved location is now inside a block or floating (e.g. terrain changed while offline).
        world.ensureChunksLoadedAround(player.pos, async = false)
        val settled = world.settle(player.pos)
        player.pos.x = settled.x
        player.pos.y = settled.y

        // Drop any stale entry (e.g. a timed-out OfflinePlayer) before re-registering this id
        world.chunkManager.removePlayer(id)
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

    private suspend fun updatePlayerView(player: Player, force: Boolean = false) {
        val showCoords: Boolean = preferencesManager.getPreference(player.id, Preference.SHOW_COORDINATES)
        val env = gameRenderer.getView(player)
        // Key on both the world view and the UI-affecting state so that UI-only
        // changes (mode, selected slot, stack count) still force a redraw.
        val selected = player.inventory.getSelectedItemStack()
        val feedbackActive = player.feedback != null && time <= player.feedbackExpiry
        if (!feedbackActive) {
            player.feedback = null
            player.clearFeedbackItems()
        }
        val cacheKey = "$env${player.gameMode}|${player.inventory.selectedSlot}|" +
                "${selected?.itemKey}|${selected?.count}|${player.inventory.contents.size}|$feedbackActive"
        val cache = playerEnvUiCache[player.id]

        if (cache == null || cache != cacheKey || force) {
            val ui = player.getActions()

            // Title is always present to keep embed height stable.
            // Priority: active feedback -> coordinates (if enabled) -> zero-width space placeholder.
            val title = if (feedbackActive) player.feedback!!
                else if (showCoords) player.pos.toString() else "​"

            val embed = EmbedBuilder()
                .setTitle(title)
                .setDescription(env)
                .build()

            player.hook?.editOriginalEmbeds(embed)
                ?.setComponents(ui)
                ?.queue()

            playerEnvUiCache[player.id] = cacheKey
        }
    }

    /**
     * Renders the crafting view, cache-gated on page + station + inventory so the tick loop can call
     * this every frame without rebuilding (and resetting) the select menu twice a second.
     */
    private fun updateCraftingView(player: Player, force: Boolean = false) {
        val rendered = CraftingRenderer(player).render() ?: return
        val (embed, components) = rendered

        val station = world.getCraftingStationAt(player.pos)
        val invSignature = player.inventory.contents.joinToString(",") { "${it.itemKey}:${it.count}" }
        val cacheKey = "${player.recipeManager.craftPage}|$station|$invSignature"

        if (!force && playerCraftUiCache[player.id] == cacheKey) return

        player.hook?.editOriginalEmbeds(embed)?.setComponents(components)?.queue()
        playerCraftUiCache[player.id] = cacheKey
    }

    suspend fun updateHook(hook: InteractionHook, force: Boolean = false, showWorld: Boolean = false) {
        val userId = hook.interaction.user.idLong
        val player = getOrCreatePlayer(userId, hook)

        if (showWorld) {
            player.viewState = ViewState.WORLD
        }

        when (player.viewState) {
            ViewState.WORLD -> updatePlayerView(player, force || showWorld)
            ViewState.CRAFTING -> updateCraftingView(player, force)
            ViewState.CHEST -> updateChestView(player, force)
            ViewState.INVENTORY -> updateInventoryView(player, force)
        }
    }

    fun renderCrafting(player: Player, force: Boolean = true) = updateCraftingView(player, force)
    fun renderChest(player: Player, force: Boolean = true) = updateChestView(player, force)
    fun renderInventory(player: Player, force: Boolean = true) = updateInventoryView(player, force)

    private fun updateInventoryView(player: Player, force: Boolean = false) {
        val inv = player.inventory
        val invSignature = "${inv.selectedSlot}|${inv.contents.joinToString(",") { "${it.itemKey}:${it.count}" }}"

        if (!force && playerInventoryUiCache[player.id] == invSignature) return

        val embed = inv.getEmbed() ?: return
        val components = player.getInventoryActions()

        player.hook?.editOriginalEmbeds(embed)?.setComponents(components)?.queue()
        playerInventoryUiCache[player.id] = invSignature
    }

    private fun updateChestView(player: Player, force: Boolean = false) {
        val chestPos = player.openChestPos
        if (chestPos == null || world.getChestAt(chestPos) == null) {
            player.openChestPos = null
            player.viewState = ViewState.WORLD
            playerEnvUiCache.remove(player.id)
            playerChestUiCache.remove(player.id)
            return
        }

        val rendered = ChestRenderer(player).render() ?: return
        val (playerEmbed, chestEmbed, components) = rendered

        val chestData = world.getChestAt(chestPos)!!
        val invSig = "${player.inventory.selectedSlot}|${player.inventory.contents.joinToString(",") { "${it.itemKey}:${it.count}" }}"
        val chestSig = "${player.chestSelectedSlot}|${chestData.inventory.contents.joinToString(",") { "${it.itemKey}:${it.count}" }}"
        val cacheKey = "${player.chestCursorOnPlayer}|$invSig|$chestSig"

        if (!force && playerChestUiCache[player.id] == cacheKey) return

        player.hook?.editOriginalEmbeds(playerEmbed, chestEmbed)?.setComponents(components)?.queue()
        playerChestUiCache[player.id] = cacheKey
    }

    fun refreshPlayer(player: Player) {
        player.age = time
    }

    fun handleInput(hook: InteractionHook, string: String) {
        val userId = hook.interaction.user.idLong
        val player = players[userId] ?: return
        if (player !is Player) return
        refreshPlayer(player)
        player.hook = hook
        player.handle(string)
    }

    fun breakTile(player: Player, pos: Vec) {
        val tile = world.getTile(pos) ?: return
        if (!tile.breakable) return

        val chestData = if (tile.interaction is TileInteraction.Chest) world.getChestAt(pos) else null

        if (chestData != null) {
            chestData.inventory.contents.forEach { stack -> player.inventory.addItem(stack) }
            world.removeChestAt(pos)
            players.values.filterIsInstance<Player>().filter { it.openChestPos == pos }.forEach { p ->
                p.openChestPos = null
                p.viewState = ViewState.WORLD
                playerEnvUiCache.remove(p.id)
                playerChestUiCache.remove(p.id)
            }
        }

        tile.drop?.let { drop ->
            val stack = ItemStack(drop.item, drop.count)
            player.inventory.addItem(stack)
            if (preferencesManager.getPreference<Boolean>(player.id, Preference.ITEM_PICKUP_FEEDBACK)) {
                player.addPickupFeedback(stack)
            }
        }
        tile.onBreak(world, pos)
    }

    fun placeTile(player: Player, pos: Vec): Boolean {
        val currentTile = world.getTile(pos) ?: return false
        if (!currentTile.airy) return false

        // Only allow replacing strictly AIR (prevents destroying grass/mushrooms/etc accidentally)
        if (currentTile != Tiles.AIR) return false

        val stack = player.inventory.getSelectedItemStack() ?: return false
        val tile = stack.item.getTile() ?: return false

        if (!world.setTile(pos, tile)) return false

        player.inventory.removeFromSlot(player.inventory.selectedSlot, 1)
        return true
    }

    fun saveAll() {
        if (!Config.PERSISTENCE_ENABLED) return
        val onlineCount = players.count { it.value is Player }
        MineB0t.log("Saving $guildName ($guildId): $onlineCount online player(s)")

        players.filter { it.value is Player }.forEach { (_, player) ->
            (player as Player).saveData()
        }

        world.chunkManager.saveChunks()

        gameDataManager.saveGameData()
    }
}
