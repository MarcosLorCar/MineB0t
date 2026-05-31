# MineB0t TODO

## 🔴 Broken core mechanics

- [x] **Place a block** — `world.setTile(...)` is commented out in `Game.placeTile()`. `Game.kt:174`
- [x] **Break drops items** — drop logic commented out; never uses `Tile.drop` / `Tile.onBreak`. `Game.kt:161-167`
- [x] **Bridge `ItemType` → `Tile`** — `ItemType.getTile()` now resolves a placeable `Tile` via `tileKey`.
- [x] **PLACE button enable-check** — now uses `getSelectedItemStack()?.itemType?.getTile() != null`. `PlayerActionMenu.kt`

## 🔴 Crafting (non-functional)

- [ ] **Register recipes** — `RecipeRegistry.registerRecipe()` has zero callers → craft button always disabled. `RecipeRegistry.kt:9`
- [ ] **Route craft button correctly** — `InputHandler.handle` sends `"craft"` → `handleInventory` instead of `handleCraft`. `InputHandler.kt:28`
- [ ] **Implement `handleCraft`** — dead code; navigation logic commented out. `InputHandler.kt:66-99`
- [ ] **Execute craft action** — nothing consumes ingredients / produces results; craft-nav buttons not in `EventHandler`.
- [ ] **`World.hasCraftingStation()`** — throws `TODO()` if ever called. `World.kt:36`

## 🟠 Rendering other players

- [x] **Blocked move removes player from chunk map but never re-adds** — occupant lists now only change on a real chunk crossing, so blocked/step-up moves keep the player registered. `PlayerMovement.kt`
- [x] **Timeout/leave doesn't clean `chunkManager.players`** — offline `sleepy_head` rendering is intentional, so offline players stay in the map; `addPlayer` now calls `chunkManager.removePlayer(id)` before re-registering to kill stale/duplicate entries on reconnect. `Game.kt`, `ChunkManager.kt`

## 🟡 World generation

- [ ] **Decoration unimplemented** — `decorate()` is `TODO()`; both decoration passes are comments. `OverworldGenerator.kt:51-62` — _SKIPPED: incomplete feature system (like crafting); never called, so harmless._
- [x] **Ore placement not seed-reproducible** — now uses seeded `oreNoise` Perlin with `ORE_THRESHOLD`. `OverworldGenerator.kt`
- [x] **`setTile` silently no-ops on unloaded chunks** — `setTile` now returns `Boolean` and logs when the target chunk isn't loaded; callers bail out instead of mutating inventory on a dropped edit. `World.kt`

## ⚪ Cleanup

- [x] **Delete `TileType`** — removed; `ItemType` maps to `Tile` directly via `getTile()`, unused `Inventory.hasBlocks()` dropped.
- [x] **`Vec.toEnvPos` return type** — tightened to `Vec`. `Vec.kt`
- [x] **Missing-chunk renders `":x:"` literal** — now uses `Emojis.getEmojiCode("null")`. `GameRenderer.kt`
- [x] **`playerEnvUiCache` only keys on world string** — cache key now also folds in game mode, selected slot, item type/count and inventory size. `Game.kt`
- [x] **CLAUDE.md doc drift** — doc now reads `<chunkX>.<chunkY>.dat` to match `ChunkDataManager`.
