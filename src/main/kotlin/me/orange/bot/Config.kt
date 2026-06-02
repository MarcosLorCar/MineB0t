package me.orange.bot

object Config {
    const val GAME_DATA_DIR = "data/games"

    // Player
    const val SPAWNPOINT_DISPERSION = 7
    const val PLAYER_TIMEOUT_SECONDS = 30

    // Game loop
    const val FPS = 5
    const val AUTOSAVE_TICKS = FPS * 60 * 5

    // World
    const val CHUNK_UNLOAD_DELAY = 30_000L
}