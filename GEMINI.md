# MineB0t Project Overview

MineB0t is a Discord bot that allows users to play a 2D Terraria-like sandbox game directly within Discord. The game features terrain generation, mining, crafting, inventory management, and persistent worlds.

## Core Technologies
- **Language:** Kotlin (JVM 21)
- **Discord Library:** [JDA (Java Discord API)](https://github.com/discord-jda/JDA)
- **Build System:** Gradle
- **Concurrency:** Kotlin Coroutines
- **Serialization:** kotlinx.serialization (JSON and CBOR for game data)
- **Noise Generation:** jnoise (for procedural world generation)
- **Logging:** SLF4J with Logback

## Project Structure
- `src/main/kotlin/me/orange/`: Root package
    - `bot/`: Bot lifecycle, JDA setup, and configuration (`MineB0t.kt`, `Config.kt`)
    - `game/`: Core game engine
        - `world/`: World, chunk, and tile management
        - `player/`: Player state, actions, and movement
        - `inventory/`: Item and inventory systems
        - `craft/`: Recipe and crafting logic
        - `gameData/`: Data persistence
        - `utils/`: Common utilities (e.g., `Vec` for coordinates)
    - `events/`: JDA event handlers for slash commands and interactions
    - `console/`: Bot administration via terminal console commands
- `data/`: Directory where game and player data is persisted (not committed)

## Key Commands

### Building and Running
- **Build:** `./gradlew build`
- **Run:** `./gradlew run`
    - **Note:** Requires the `DISCORD_BOT_TOKEN` environment variable to be set.
- **Clean:** `./gradlew clean`

### Testing
- **Run Tests:** `./gradlew test` (Note: Currently minimal test coverage)

### Console Commands (while running)
- `help`: List available console commands
- `stop`: Gracefully stop the bot and save all data
- `save`: Manually trigger an autosave for all guilds
- `status`: Show bot status and online players

## Development Conventions

### Architecture Patterns
- **Per-Guild Instances:** Each Discord server (guild) has its own `Game` instance managed by `GamesManager`.
- **Game Loop:** Each `Game` runs its own tick-based loop (default 5 FPS) using Coroutines.
- **Chunked World:** The world is divided into chunks that are loaded/unloaded dynamically based on player proximity.
- **Emoji Rendering:** Visuals are rendered using custom emojis in Discord embeds. Emojis are registered and validated on startup (`Emojis.kt`).
- **Singletons:** Game-wide registries for tiles, items, and recipes use Kotlin `object` declarations (e.g., `Tiles`, `Items`, `Recipes`).

### Coding Style
- **Asynchronous Code:** Use Coroutines (`launch`, `async`, `suspend`) for non-blocking operations, especially I/O and Discord API calls.
- **Data Persistence:** Use `GameDataManager` and `ChunkDataManager` for saving/loading state.
- **Type Safety:** Leverage Kotlin's strong typing and serialization for data structures.

### Design Principles
- **Surgical Updates:** When updating Discord views, use cache-gating (e.g., `playerEnvUiCache`) to avoid redundant API calls and UI flickering.
- **Interaction Flow:** Use the `BaseInteraction` hierarchy for handling buttons, slash commands, and select menus.

## TODOs and Future Work
- Expand test coverage in `src/test/kotlin`.
- Implement more complex tile entities and world features.
- Optimize chunk loading/unloading logic.
- Enhance the rendering engine for better performance and visual variety.
