package me.orange.game

import kotlinx.coroutines.runBlocking
import me.orange.bot.Emojis
import me.orange.game.inventory.item.ItemStack
import me.orange.game.inventory.item.Items
import me.orange.game.player.Player
import me.orange.game.utils.Vec
import me.orange.game.world.tile.Tiles
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GamePlacementTest {

    private lateinit var game: Game
    private lateinit var player: Player

    @BeforeTest
    fun setup() = runBlocking {
        Emojis.loadEmojis()
        // Tiles and Items are objects, they will be initialized on first access.
        // We need to make sure they are initialized after Emojis are loaded.
        val _tiles = Tiles
        val _items = Items

        game = Game("test_guild")
        player = Player(id = 123L, game = game, age = 0L)
        game.players[123L] = player

        game.world.ensureChunksLoadedAround(Vec(0, 0), false)
    }

    @Test
    fun testAiryTileReplacementBelowPlayer() {
        val pos = Vec(0, 0)
        game.world.setTile(pos, Tiles.RED_SHROOM)
        player.pos.x = 0
        player.pos.y = 1 // Player feet at (0, 1), so (0, 0) is below them.

        player.inventory.addItem(ItemStack(Items.DIRT, 10))
        player.inventory.selectedSlot = 0

        val success = game.placeTile(player, pos)

        // After fix, this should STILL be RED_SHROOM and success should be false
        assertEquals(false, success)
        assertEquals(Tiles.RED_SHROOM, game.world.getTile(pos))
    }

    @Test
    fun testAiryTileReplacementNotBelowPlayer() {
        val pos = Vec(1, 1)
        game.world.setTile(pos, Tiles.RED_SHROOM)
        player.pos.x = 0
        player.pos.y = 1 // Player at (0, 1), target at (1, 1) is NOT below them.

        player.inventory.addItem(ItemStack(Items.DIRT, 10))
        player.inventory.selectedSlot = 0

        val success = game.placeTile(player, pos)

        // After the general fix, this should now FAIL (no longer replaces any non-air airy tile)
        assertEquals(false, success)
        assertEquals(Tiles.RED_SHROOM, game.world.getTile(pos))
    }

    @Test
    fun testAirReplacementBelowPlayer() {
        val pos = Vec(0, 0)
        game.world.setTile(pos, Tiles.AIR)
        player.pos.x = 0
        player.pos.y = 1

        player.inventory.addItem(ItemStack(Items.DIRT, 10))
        player.inventory.selectedSlot = 0

        val success = game.placeTile(player, pos)

        // Should replace AIR even if below
        assertEquals(true, success)
        assertEquals(Tiles.DIRT, game.world.getTile(pos))
    }
}

