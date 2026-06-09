package me.orange.game

import kotlinx.coroutines.runBlocking
import me.orange.bot.Emojis
import me.orange.game.player.Player
import me.orange.game.utils.Vec
import me.orange.game.world.tile.Tiles
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerMovementTest {

    private lateinit var game: Game
    private lateinit var player: Player

    @BeforeTest
    fun setup() = runBlocking {
        Emojis.loadEmojis()
        // Tiles and Items are objects, they will be initialized on first access.
        // We need to make sure they are initialized after Emojis are loaded.
        val _tiles = Tiles

        game = Game("test_guild")
        player = Player(id = 123L, game = game, age = 0L)
        game.players[123L] = player

        game.world.ensureChunksLoadedAround(Vec(0, 0), false)
    }

    @Test
    fun testLadderPhysics() {
        // Clear local test area to air
        for (x in -2..2) {
            for (y in -2..2) {
                game.world.setTile(Vec(x, y), Tiles.AIR)
            }
        }

        // Place ladder at (0, 0)
        game.world.setTile(Vec(0, 0), Tiles.LADDER)

        // 1. Standing on top of a ladder: feet at (0, 1), ladder at (0, 0)
        player.pos.x = 0
        player.pos.y = 1
        player.fall()
        assertEquals(false, player.falling)
        assertEquals(Vec(0, 1), player.pos)

        // 2. Standing inside a ladder with air below: feet at (0, 0), ladder at (0, 0)
        player.pos.x = 0
        player.pos.y = 0
        player.fall()
        assertEquals(true, player.falling)
        assertEquals(Vec(0, -1), player.pos)

        // 3. Standing inside a ladder with another ladder below: feet at (0, 1), ladder at (0, 1) and (0, 0)
        game.world.setTile(Vec(0, 1), Tiles.LADDER)
        player.pos.x = 0
        player.pos.y = 1
        player.fall()
        assertEquals(false, player.falling)
        assertEquals(Vec(0, 1), player.pos)

        // 4. Passing horizontally through a ladder
        // Player at (0, 0) (ladder), tries to move right to (1, 0) where we place another ladder.
        game.world.setTile(Vec(1, 0), Tiles.LADDER)
        player.pos.x = 0
        player.pos.y = 0
        player.move(Vec(1, 0))
        assertEquals(Vec(1, 0), player.pos)
    }

    @Test
    fun testLadderTeleport() {
        // Clear local test area to air
        for (x in -2..2) {
            for (y in -5..10) {
                game.world.setTile(Vec(x, y), Tiles.AIR)
            }
        }

        // Set up a ladder segment from y=0 to y=5
        for (y in 0..5) {
            game.world.setTile(Vec(0, y), Tiles.LADDER)
        }

        // 1. Standing on top of the ladder at y=6
        player.pos.x = 0
        player.pos.y = 6
        player.handle("ladder_teleport")
        player.applyQueuedActions()
        // Should teleport to bottommost: y=0
        assertEquals(0, player.pos.y)

        // 2. Standing at bottom of the ladder at y=0
        player.pos.x = 0
        player.pos.y = 0
        player.handle("ladder_teleport")
        player.applyQueuedActions()
        // Should teleport to top (above top ladder tile since y=6 is walkable): y=6
        assertEquals(6, player.pos.y)

        // 3. Standing at middle: closer to top (e.g. y=4)
        player.pos.x = 0
        player.pos.y = 4
        player.handle("ladder_teleport")
        player.applyQueuedActions()
        // Should teleport to bottommost: y=0
        assertEquals(0, player.pos.y)

        // 4. Standing at middle: closer to bottom (e.g. y=1)
        player.pos.x = 0
        player.pos.y = 1
        player.handle("ladder_teleport")
        player.applyQueuedActions()
        // Should teleport to top: y=6
        assertEquals(6, player.pos.y)
    }

    @Test
    fun testFallWhenInsideBlock() {
        // Clear local test area to air
        for (x in -2..2) {
            for (y in -2..2) {
                game.world.setTile(Vec(x, y), Tiles.AIR)
            }
        }

        // Place a solid block at player feet (0, 1), but air below (0, 0)
        game.world.setTile(Vec(0, 1), Tiles.STONE)
        player.pos.x = 0
        player.pos.y = 1

        // Run fall
        player.fall()

        // The player should successfully fall to (0, 0) because there is air below,
        // even though they were inside a solid block (stone).
        assertEquals(true, player.falling)
        assertEquals(Vec(0, 0), player.pos)
    }
}
