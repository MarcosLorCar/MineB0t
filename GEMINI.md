# MineB0t Project Overview & Development Guide

MineB0t is a Discord bot that allows users to play a 2D Terraria-like sandbox game directly within Discord. The game features terrain generation, mining, crafting, inventory management, and persistent worlds.

---

## Core Technologies
- **Language:** Kotlin (JVM 21)
- **Discord Library:** [JDA (Java Discord API)](https://github.com/discord-jda/JDA)
- **Build System:** Gradle
- **Concurrency:** Kotlin Coroutines
- **Serialization:** kotlinx.serialization (JSON and CBOR for game data)
- **Noise Generation:** jnoise (for procedural world generation)
- **Logging:** SLF4J with Logback

---

## Build, Run, and Test Commands
All commands should be executed from the project root directory.

- **Build:** `./gradlew build`
- **Run:** `./gradlew run` (Requires the `DISCORD_BOT_TOKEN` environment variable to be set, or a `.env` file at the root containing the token)
- **Clean:** `./gradlew clean`
- **Run Tests:** `./gradlew test`

### Console Commands (while running)
The bot starts a console listener thread. Type commands in the terminal:
- `help`: List available console commands.
- `stop`: Gracefully stop the bot, save all data (guilds, players, chunks), and shutdown JDA.
- `save`: Manually trigger an autosave for all guild instances.
- `status`: Show bot status and online players.

---

## Project Structure & Key Types

### Root Package: `me.orange`
- **Main.kt** -> Entry point; calls `MineB0t.start()`.

### Bot & Configuration: `me.orange.bot`
- **MineB0t.kt**: Singleton orchestrating JDA, console commands, and the main coroutine scope (`MineB0t.launch`).
  - `start()`: Registers handlers, JDA, and hooks up the console scanner.
  - `stop()`: Handles graceful shutdown, synchronized on `stopped`.
  - Also registers JVM shutdown hook and schedules periodic auto-saving.
- **Emojis.kt**: Custom emoji registry loaded from Discord on startup. All tiles/UI elements resolve their visuals here.
- **Config.kt**: Filesystem paths and gameplay constants (e.g., base `data/` path).

### Event Routing: `me.orange.bot.events`
- **InteractionListener.kt**: Centralized JDA listener that catches slash commands, buttons, and select menus.
- **Interaction.kt**: Interface/Base for routing (e.g. `PlayCommand`, `PreferencesCommand`, `ChangeSettingInteraction`, `CraftSelectInteraction`).
- **InputInteraction.kt**: Handles gameplay button presses (movement, actions), routing them to `Game.handleInput()`.

### Game Engine: `me.orange.game`
- **GamesManager.kt**: Thread-safe manager tracking `Game` instances per Discord guild using `ConcurrentHashMap.computeIfAbsent`.
- **Game.kt**: Represents a guild's persistent world. Runs a tick loop at 5 FPS using Coroutines.
  - `update()`: Executes every tick. Updates online players, loads/unloads chunks, and handles player timeouts (30s).
  - `updateHook()`: Gate-cached update rendering for `WORLD` and `CRAFTING` views to prevent redundant Discord API updates.
  - `breakTile()` / `placeTile()`: Mutates world tiles, handles item drops, and consumes materials.
- **GameRenderer.kt**: Formats and renders the emoji grid representing the game world for a player.
- **gameData/GameDataManager.kt**: Saves and loads the seed and time of each guild's game state via CBOR.

### Player State: `me.orange.game.player`
- **OfflinePlayer.kt**: Persistent player state (ID, position, gameMode).
- **Player.kt**: Online player state inheriting from `OfflinePlayer`. Adds active Discord `InteractionHook`, inventory, `ViewState`, etc.
- **InputHandler.kt**: Parses input string actions from button clicks and routes them to movement/actions.
- **PlayerActionQueue.kt**: Gathers inputs/actions during the tick to apply them atomically on the next tick.
- **PlayerActionMenu.kt**: Builds JDA button rows for the standard game HUD (movement cross, mode toggle, inventory/crafting).
- **ViewState.kt**: Enum for current UI state (`WORLD`, `INVENTORY`, `CRAFTING`).

### Inventory & Items: `me.orange.game.inventory`
- **Item.kt**: Definition of items (key, emoji, maxCount, associated tileKey). Built using a DSL.
- **Items.kt**: Registry of all game items (e.g., `GRASS`, `DIRT`, `STONE`, `IRON_CHUNK`, `FURNACE`, `IRON_INGOT`, `COAL`).
- **Inventory.kt**: List of `ItemStack`s per player, selected slot, and helpers.
- **InventoryRenderer.kt**: Formats player inventory pages using JDA Embed fields (3-column layout).

### World & Tile Management: `me.orange.game.world`
- **World.kt**: Main world coordinator wrapping `ChunkManager`.
- **Chunk.kt**: A 16x16 grid of tile IDs.
- **ChunkManager.kt**: Handles asynchronous chunk loading/unloading (using `Dispatchers.IO`) with an idle TTL of 30 seconds.
- **ChunkDataManager.kt**: Persists chunks to CBOR files (`data/games/<guildId>/world/<x>.<y>.dat`).
- **OverworldGenerator.kt**: Implements world generation. Uses Perlin noise for terrain height, cave carving (coal patches, ore generation).
- **tile/Tile.kt**: Definition of tiles (breakable, airy, drops, crafting stations).
- **tile/TileRegistry.kt**: Stores tiles in a HashMap using `key.hashCode()` as the persistent tile ID.
- **tile/Tiles.kt**: Registry of all game tiles (e.g., `AIR`, `DIRT`, `GRASS`, `STONE`, `IRON_ORE`, `CRAFTING_TABLE`, `FURNACE`, `COAL_ORE`).

### Crafting System: `me.orange.game.craft`
- **CraftingStationType.kt**: Enum representing stations (`NONE`, `CRAFTING_TABLE`, `FURNACE`).
- **Recipe.kt**: Recipe details (station requirement, ingredient list, output stack).
- **Recipes.kt**: Lazy-loaded recipe definitions (must be touched at startup to trigger registration).
- **RecipeManager.kt**: Tracks player's current crafting page and handles validation/execution of item crafting.
- **CraftingRenderer.kt**: Renders the recipe list embed and interaction menus.

### Preferences: `me.orange.game.preferences`
- **Preference.kt**: Options configuration (e.g., coordinates visibility, action modes, head emoji).
- **PreferencesManager.kt**: Handles per-player preferences persistence.

---

## Development Conventions & Design Patterns

### Architecture & Concurrency
- **Per-Guild Separation**: Guilds run independent games. Use `GamesManager` to obtain the correct `Game` instance.
- **Game Loops**: Loop is tick-based (5 FPS). Avoid running blocking work in the tick loop. Use coroutines with JDA deferred events.
- **Immediate Acknowledgment**: Always acknowledge JDA interactions (`deferEdit()` or `deferReply()`) immediately to avoid the 3-second Gateway timeout. Complete heavy logic in coroutines on `Dispatchers.Default`.
- **Thread Safety**: Ensure all multi-user updates (e.g., `GamesManager`, active player lists) are guarded or use concurrent collections.

### Rendering & Cache Gating
- **Surgical View Updates**: Updating Discord message components/embeds is rate-limited and expensive.
- **UI Cache**: Use state caches (like `playerEnvUiCache` and `playerCraftUiCache`) to verify if the UI state has actually changed before invoking edit operations.

### Data Persistence
- **CBOR Serialization**: Save data (chunks, player files, game worlds) is serialized using CBOR for speed and size.
- **Stable Tile IDs**: Tile IDs are calculated using `key.hashCode()`. This ensures that updating the registration order or adding/removing tiles does not corrupt existing saves, unlike ordinal-based IDs.

---

## Core Constraints & Rules
- **Item Keys & Emojis**: Every registered `Item`'s key must exactly match a registered emoji key in `Emojis` to render properly.
- **Tile Keys in Items**: `tileKey` in `Item` must match a registered tile key in `Tiles` for the item to be placeable.
- **Initialization**: Kotlin `object` registries (like `Recipes`) load lazily. Ensure they are explicitly referenced during startup to trigger registration.
- **Recipe Mapping**: All recipes must be registered via `RecipeRegistry.registerRecipe` to correctly populate the ingredient lookup map.
- **View Rendering**: `ViewState.INVENTORY` is rendered on-demand in `InputHandler`. `WORLD` and `CRAFTING` are updated via `Game.updateHook` on the tick loop.