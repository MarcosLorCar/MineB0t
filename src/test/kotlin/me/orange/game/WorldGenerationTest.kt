package me.orange.game

import kotlinx.coroutines.runBlocking
import me.orange.bot.Emojis
import me.orange.game.utils.Vec
import me.orange.game.world.tile.Tiles
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class WorldGenerationTest {

    @BeforeTest
    fun setup() {
        Emojis.loadEmojis()
    }

    @Test
    fun testCratesGenerateInCaves() = runBlocking {
        val guildId = "test_guild_" + java.util.UUID.randomUUID().toString()
        val game = Game(guildId)
        try {
            // Load a 5x5 chunk area to ensure a large enough area is generated/decorated
            for (cx in -2..2) {
                for (cy in -2..2) {
                    game.world.ensureChunksLoadedAround(Vec(cx * 16, cy * 16), false)
                }
            }

            var crateCount = 0
            for (cx in -2..2) {
                for (cy in -2..2) {
                    val chunk = game.world.getChunk(Vec(cx, cy))
                    if (chunk != null) {
                        for (x in 0..15) {
                            for (y in 0..15) {
                                if (chunk.getTile(x, y) == Tiles.CRATE) {
                                    crateCount++
                                }
                            }
                        }
                    }
                }
            }

            println("Generated $crateCount crates in the 5x5 chunk area.")
            assertTrue(crateCount > 0, "Should generate at least one crate in the generated world chunks")
        } finally {
            java.io.File(game.gameDataDir).deleteRecursively()
        }
    }
}
