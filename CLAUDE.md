PROJECT: Discord bot running a Terraria-like 2D game inside Discord messages. Players interact via button clicks; world rendered as emoji grid in Discord embeds. One Game instance per Discord guild.

BUILD: `./gradlew build` | `DISCORD_BOT_TOKEN=<token> ./gradlew run` | JDK 21 required | reads DISCORD_BOT_TOKEN from env or .env file at startup

PACKAGES AND KEY TYPES:

me.orange.bot:
- Main.kt → MineB0t.start()
- MineB0t: singleton, owns JDA instance + shared CoroutineScope. Use MineB0t.launch{} for coroutine work surviving game ticks. MineB0t.log() for logging.
  - startCommandListener(): console loop with > prompt; commands: stop/save/status/help
  - installPromptAwareOutput(): wraps System.out so log lines don't clobber the prompt
  - stop(): synchronized, guarded by `stopped` flag; saves + shuts down JDA
  - shutdown hook + auto-save every AUTOSAVE_TICKS (FPS*60*5) also call saveAll()
- Emojis: string key → Discord Emoji. Emojis.getCustom(key) loads by Discord ID. All tiles/UI use this.
- Config: filesystem paths + gameplay constants. GAME_DATA_DIR is base path.

me.orange.events:
- EventHandler: registers all JDA listeners at startup
- BaseInteraction → SlashCommand / ButtonInteraction / StringSelectInteraction
- InputInteraction: main button handler, routes button IDs (e.g. "move_left", "action_up_right", "inventory_open") → Game.handleInput()
- PlayInteraction, ChangeSettingInteraction, SelectSettingInteraction, CraftSelectInteraction: other button/select handlers
- commands/: PlayCommand, PreferencesCommand, TestCommand, SetHeadCommand (SlashCommand subclasses)
- New player action: add InputInteraction in EventHandler + button in PlayerActionMenu + string case in InputHandler

me.orange.game:
- GamesManager: singleton map guildId→Game, lazy init on first interaction
- Game(guildId, seed?, gameDataDir, time): owns world, players, renderer, preferences
  - players: ConcurrentHashMap<Long, OfflinePlayer> (Player extends OfflinePlayer)
  - FPS=5, PLAYER_TIMEOUT_SECONDS=30
  - run(): coroutine loop, calls update() then delays
  - update(): tick++, per online Player: ensureChunksLoadedAround, player.update(), updateHook, timeoutPlayer; then chunkManager.unloadUnusedChunks(); auto-saves every AUTOSAVE_TICKS
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
  - player.getActions(): returns Discord ActionRows (world button UI)
  - player.getInventoryActions(): returns 3×3 cross button grid for inventory navigation
  - player.saveData(): persists via PlayerDataManager
- PlayerActionQueue: collects actions during button handling, applies on next tick
- InputHandler: string-based action dispatch
- PlayerActionMenu: builds Discord button rows. getActions()=world view, getInventoryActions()=3×3 cross (placeholder/up/close, left/preview/right, placeholder/down/placeholder)
- ViewState: WORLD | INVENTORY | CRAFTING. WORLD+CRAFTING render via updateHook; INVENTORY renders on-demand in InputHandler.
- GameMode: PLACE | BREAK. In PLACE mode, inventory left/right skip non-placeable items.
- PlayerMovement: movement logic
- data/PlayerDataManager: per-player CBOR persistence

me.orange.game.inventory:
- Item(key, emoji, maxCount=16, tileKey?): item definition. item.getTile() → Tile? via tileKey
- Item.Builder DSL: placeable(tileKey), maxCount(n), onUse(block)
- Items: singleton registry via ItemRegistry. Current items: GRASS, DIRT, STONE, IRON_CHUNK, CRAFTING_TABLE, FURNACE, IRON_INGOT, COAL
- ItemStack(item, count): mutable stack in inventory
- Inventory: holds MutableList<ItemStack>, selectedSlot, getSelectedItemStack()
- InventoryRenderer: renders inventory view as inline embed fields. INVENTORY_COLS=3 (Discord renders 3 inline fields per row — up/down nav jumps by this value)

me.orange.game.world:
- World(game, seed): wraps ChunkManager, provides getTile(worldVec)/setTile(worldVec, tile), generateSpawnPoint(), ensureChunksLoadedAround(pos, async)
- Chunk: 16×16 tiles as MutableList<MutableList<Int>> (tile IDs by position)
- ChunkManager: async load (Dispatchers.IO), cache, unload (30s idle TTL), tracks players per chunkPos
- ChunkDataManager: CBOR persistence for chunks → data/games/<guildId>/world/<x>.<y>.dat
- ChunkGenerator: interface for world generation
- OverworldGenerator: implements ChunkGenerator. Perlin noise for terrain height + cave carving. STONE_LAYER_DEPTH=4 tiles below surface → stone. Caves where noise > CAVE_THRESHOLD=0.25. Coal ore patches generated independently.

me.orange.game.world.tile:
- Tile(key, id, breakable, airy, drop?, onBreak): immutable tile definition
- Tile.Builder DSL: breakable(), airy(), drops(item, count), onBreak(block), craftingStation(type)
- TileRegistry: HashMap-based registry. IDs derived from key.hashCode() — order-independent, collision-detected at startup. getTile(id) returns Tile? (null for unknown/removed tile IDs in old save data).
- Tiles: registers via TileRegistry. Current tiles: NULL, AIR, DIRT, GRASS, STONE, IRON_ORE, CRAFTING_TABLE, FURNACE, COAL_ORE
  DIRT/GRASS/STONE/IRON_ORE/COAL_ORE/CRAFTING_TABLE/FURNACE are breakable. AIR is airy. NULL is neither.
- Adding a tile: add entry anywhere in Tiles (order doesn't matter), add emoji in Emojis.loadEmojis(), add Item in Items if it drops something

me.orange.game.craft:
- CraftingStationType: enum NONE|CRAFTING_TABLE|FURNACE, each carries emojiKey (resolved via Emojis.get custom-or-unicode)
- Recipe: id, ingredients (List<ItemStack>), result (ItemStack), requiredStation (CraftingStationType=NONE)
- RecipeRegistry: recipes list + recipeMap (ingredient itemKey → Set<Recipe>). registerRecipe(recipe) populates recipeMap (the only thing that does). DSL: register(id){ station(t); ingredient(key,n); output(key,n) } builds + routes through registerRecipe. getRecipe(id), getRecipesByIngredient(item).
- Recipes: FURNACE_RECIPE (stone×8 → furnace, no station), IRON_INGOT_RECIPE (coal×1 + iron_chunk×2 → iron_ingot, requires FURNACE). Objects init lazily → Recipes.count touched at startup to force registration.
- RecipeManager (per-player): craftPage state. getViewableRecipes()→RecipeCategories(craftable, semi, known) sorted by id. craft(recipeId): validates station, ingredients, output fits; consumes items, adds result, saves.
- CraftingRenderer (per-player, built per-call): render()→Pair<MessageEmbed, components> or null. Clamps craftPage. Craftable recipes shown normally, semi shown with have/need counts, known-only shown struck-through. Components: nav row (craft_prev/craft_close/craft_next) + StringSelectMenu "craft_select" (craftable recipes on page only).
- Station detection: World.getCraftingStationAt(pos) checks pos+(0,-1), pos, pos+(0,1); first non-NONE wins.

me.orange.game.preferences:
- Preference: enum with name, default value, valueType (KClass). MENUS_SIZE(Int,10), SHOW_COORDINATES(Boolean,false), MORE_ACTIONS(Boolean,false), HEAD_EMOJI(String,"😀")
- PreferencesManager (per-game): playerPreferences map<Long, Map<Preference,Any>>. getPreference<T>(id, pref) returns typed value or default. setPreference parses and stores.

me.orange.game.utils:
- Vec: 2D integer vector, toChunkPos() conversion
- MathUtils: utility math

PERSISTENCE: kotlinx.serialization CBOR. Layout:
data/games/<guildId>/game.dat → seed + time
data/games/<guildId>/world/<x>.<y>.dat → chunk tile data (tile IDs are key.hashCode() — deleting old world data required if migrating from positional-ID saves)
Per-player data managed by PlayerDataManager (path under gameDataDir)
saveAll() called on: stdin "stop" command, shutdown hook, auto-save every 5 minutes

CONSTRAINTS:
- Tile IDs are key.hashCode() — safe to reorder/remove entries in Tiles. Unknown IDs in saved chunks load as null (rendered as NULL tile).
- Item keys must match emoji keys in Emojis
- tileKey in Item must match a registered key in Tiles
- ViewState.INVENTORY renders on-demand in InputHandler (not via updateHook). WORLD and CRAFTING render via updateHook.
- recipeMap only populated by RecipeRegistry.registerRecipe — any new DSL/registration path must route through it or getRecipesByIngredient returns empty
- Recipes object must be force-touched at startup (objects init lazily) or no recipes register
- craft_prev/craft_next queue page change + renderCrafting as a single atomic action to avoid concurrency interleave
