package me.orange.bot

object Config {
    val DEV_MODE = System.getenv("DEV_MODE")?.toBoolean() ?: false
    val PERSISTENCE_ENABLED = !DEV_MODE && (System.getenv("PERSISTENCE_ENABLED")?.toBoolean() ?: true)

    const val GAME_DATA_DIR = "data/games"
    const val PLAYER_DATA_DIR = "data/players"

    // Player
    const val SPAWNPOINT_DISPERSION = 7
    const val PLAYER_TIMEOUT_SECONDS = 30

    // Game loop
    const val FPS = 5
    const val AUTOSAVE_TICKS = FPS * 60 * 5

    // World
    const val CHUNK_UNLOAD_DELAY = 30_000L
}