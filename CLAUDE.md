PROJECT: Discord bot running a Terraria-like 2D game inside Discord messages. Players interact via button clicks; world rendered as emoji grid in Discord embeds. One Game instance per Discord guild.

BUILD: `./gradlew build` | `DISCORD_BOT_TOKEN=<token> ./gradlew run` | JDK 21 required | reads DISCORD_BOT_TOKEN from env or .env file at startup

PACKAGES AND KEY TYPES:

me.orange.bot:
- Main.kt → MineB0t.start()
- MineB0t: singleton, owns JDA instance + shared CoroutineScope. Use MineB0t.launch{} for coroutine work surviving game ticks. MineB0t.log() for logging.
- Emojis: string key → Discord Emoji. Emojis.getCustom(key) loads by Discord ID. All tiles/UI use this.
- Config: filesystem paths + gameplay constants. GAME_DATA_DIR is base path.

me.orange.events:
- EventHandler: registers all JDA listeners at startup
- BaseInteraction → SlashCommand / ButtonInteraction / StringSelectInteraction
- InputInteraction: main button handler, routes button IDs (e.g. "move_left", "action_up_right", "inventory_open") → Game.handleInput()
- PlayInteraction, ChangeSettingInteraction, SelectSettingInteraction: other button/select handlers
- commands/: PlayCommand, PreferencesCommand, TestCommand (SlashCommand subclasses)
- New player action: add InputInteraction in EventHandler + button in PlayerActionMenu + string case in InputHandler

me.orange.game:
- GamesManager: singleton map guildId→Game, lazy init on first interaction
- Game(guildId, seed?, gameDataDir, time): owns world, players, renderer, preferences
  - players: ConcurrentHashMap<Long, OfflinePlayer> (Player extends OfflinePlayer)
  - FPS=2, PLAYER_TIMEOUT_SECONDS=30
  - run(): coroutine loop, calls update() then delays
  - update(): tick++, per online Player: ensureChunksLoadedAround, player.update(), updateHook, timeoutPlayer; then chunkManager.unloadUnusedChunks()
  - updateHook(hook, force, showWorld): resolves/creates Player; routes by viewState: WORLD→updatePlayerView, CRAFTING→updateCraftingView (cache-gated on page+station+inventory signature so tick loop doesn't reset the select menu), else no-op. renderCrafting(player) = forced updateCraftingView (called from queued actions). playerCraftUiCache mirrors playerEnvUiCache.
  - updatePlayerView: uses playerEnvUiCache (keyed on env+gameMode+selectedSlot+itemKey+count+inventorySize) to avoid redundant Discord API calls
  - handleInput(hook, string): routes to player.handle(string) if player is online
  - breakTile(player, pos): checks breakable, sets AIR, adds drop to inventory
  - placeTile(player, pos): checks airy+selectedItem.getTile(), sets tile, decrements stack
  - saveAll(): saves all online players, chunks, gameData
- GameRenderer: renders world view string for a Player (called by Game.updatePlayerView)
- gameData/GameDataManager: saves/loads game.dat (seed + time) via CBOR

me.orange.game.player:
- OfflinePlayer(id, pos, gameMode): persisted state
- Player extends OfflinePlayer: adds hook: InteractionHook?, inventory: Inventory, viewState: ViewState, age: Long
  - Player.loadPlayer(id, game): loads from disk or returns null
  - player.handle(string): delegates to InputHandler
  - player.getActions(): returns Discord ActionRows (button UI)
  - player.saveData(): persists via PlayerDataManager
- PlayerActionQueue: collects actions during button handling, applies on next tick
- InputHandler: string-based action dispatch
- PlayerActionMenu: builds Discord button rows
- ViewState: WORLD | INVENTORY | CRAFTING (only WORLD currently renders via updateHook)
- GameMode: enum for player game mode
- PlayerMovement: movement logic
- data/PlayerDataManager: per-player CBOR persistence

me.orange.game.inventory:
- Item(key, emoji, maxCount=16, tileKey?): item definition. item.getTile() → Tile? via tileKey
- Item.Builder DSL: placeable(tileKey), maxCount(n), onUse(block)
- Items: singleton registry. Fields: GRASS, DIRT, STONE, IRON_CHUNK. register(key, block) appends to list + keyMap. Items.get(key) lookup.
- ItemStack(item, count): mutable stack in inventory
- Inventory: holds MutableList<ItemStack>, selectedSlot, getSelectedItemStack()
- InventoryRenderer: renders inventory view

me.orange.game.world:
- World(game, seed): wraps ChunkManager, provides getTile(worldVec)/setTile(worldVec, tile), generateSpawnPoint(), ensureChunksLoadedAround(pos, async)
- Chunk: 16×16 tiles as MutableList<MutableList<Int>> (tile IDs by position)
- ChunkManager: async load (Dispatchers.IO), cache, unload (30s idle TTL), tracks players per chunkPos
- ChunkDataManager: CBOR persistence for chunks → data/games/<guildId>/world/<x>.<y>.dat
- ChunkGenerator: interface for world generation
- OverworldGenerator: implements ChunkGenerator. Perlin noise for terrain height + cave carving. STONE_LAYER_DEPTH=4 tiles below surface → stone. Caves where noise > CAVE_THRESHOLD=0.25.

me.orange.game.world.tile:
- Tile(key, id, breakable, airy, drop?, onBreak): immutable tile definition
- Tile.Builder DSL: breakable(), airy(), drops(item, count), onBreak(block)
- Tiles: singleton registry. Entries IN ORDER (IDs are positional — reordering corrupts saved chunks):
  NULL(0), AIR(1), DIRT(2), GRASS(3), STONE(4), IRON_ORE(5)
  DIRT/GRASS/STONE/IRON_ORE are breakable and have drops.
  AIR is airy. NULL is neither.
- Adding a tile: append entry to Tiles (never reorder), add emoji in Emojis.loadEmojis(), add Item in Items if it drops something

me.orange.game.craft:
- CraftingStationType: enum NONE|CRAFTING_TABLE|FURNACE, each carries emojiKey (resolved via Emojis.get custom-or-unicode)
- Recipe: id, ingredients (List<ItemStack>), result (ItemStack), requiredStation (CraftingStationType=NONE)
- RecipeRegistry: recipes list + recipeMap (ingredient itemKey → Set<Recipe>). registerRecipe(recipe) populates recipeMap (the only thing that does). DSL: register(id){ station(t); ingredient(key,n); output(key,n) } builds + routes through registerRecipe. getRecipe(id), getRecipesByIngredient(item).
- Recipes: object holding recipe definitions (crafting_table, furnace, iron_ingot). objects init lazily → Recipes.count is touched in MineB0t.start to force registration.
- RecipeManager (per-player): craftPage state. getSemiRecipes()=recipes player has ≥1 ingredient for. getViewableRecipes()=semi sorted by id (pagination+select source). craft(recipeId): validates station via World.getCraftingStationAt(pos), ingredient counts, output fits (Inventory.canFit); consumes (Inventory.removeItems across stacks), adds result, player.saveData().
- CraftingRenderer (per-player, built per-call): render()→Pair<MessageEmbed, components> or null. Clamps craftPage. Lines: `[id] **Nx** :in: + ... ➔ **Nx** :out:`. Components: nav row (craft_prev/craft_close/craft_next) + StringSelectMenu id="craft_select" (options=page recipe ids).
- Station detection: World.getCraftingStationAt(pos) checks pos+(0,-1) (under feet), pos, pos+(0,1); first non-NONE wins. hasCraftingStation = !=NONE.
- Crafting tiles: CRAFTING_TABLE, FURNACE (appended in Tiles, placeable, drop matching item, craftingStation set). Items: CRAFTING_TABLE, FURNACE, IRON_INGOT (unicode emoji fallback in Emojis.emojis map).

me.orange.game.preferences:
- Preference: enum with name, default value, valueType (KClass)
- PreferencesManager (per-game): playerPreferences map<Long, Map<Preference,Any>>. getPreference<T>(id, pref) returns typed value or default. setPreference parses and stores.

me.orange.game.utils:
- Vec: 2D integer vector, toChunkPos() conversion
- MathUtils: utility math

PERSISTENCE: kotlinx.serialization CBOR. Layout:
data/games/<guildId>/game.dat → seed + time
data/games/<guildId>/world/<x>.<y>.dat → chunk tile data
Per-player data managed by PlayerDataManager (path under gameDataDir)
GamesManager.saveAll() called on stdin "stop" command.

CONSTRAINTS:
- Tile IDs are positional in Tiles registry — never reorder
- Item keys must match emoji keys in Emojis
- tileKey in Item must match a registered key in Tiles
- ViewState.INVENTORY renders on-demand in InputHandler (not via updateHook). WORLD and CRAFTING render via updateHook.
- recipeMap only populated by RecipeRegistry.registerRecipe — any new DSL/registration path must route through it or getRecipesByIngredient returns empty
- Recipes object must be force-touched at startup (objects init lazily) or no recipes register
