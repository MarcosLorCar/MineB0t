# MineB0t TODO

## 🔴 Broken core mechanics

- [x] **Place a block** — `world.setTile(...)` is commented out in `Game.placeTile()`. `Game.kt:174`
- [x] **Break drops items** — drop logic commented out; never uses `Tile.drop` / `Tile.onBreak`. `Game.kt:161-167`
- [x] **Bridge `ItemType` → `Tile`** — `ItemType.getTile()` now resolves a placeable `Tile` via `tileKey`.
- [x] **PLACE button enable-check** — now uses `getSelectedItemStack()?.itemType?.getTile() != null`. `PlayerActionMenu.kt`

## 🔴 Crafting (non-functional)

- [x] **Register recipes** — `RecipeRegistry.registerRecipe()` had zero callers; now populated via DSL in `Recipes.kt`, force-initialized at startup.
- [x] **Route craft button correctly** — `InputHandler.handle` sent `"craft"` → `handleInventory`; fixed to `handleCraft`.
- [x] **Implement `handleCraft`** — pagination, open/close/prev/next, cache invalidation all wired.
- [x] **Execute craft action** — `RecipeManager.craft()` validates station + ingredients + output space, consumes across stacks, calls `saveData()`. `CraftSelectInteraction` triggers it.
- [x] **`World.hasCraftingStation()`** — was `TODO()`; now backed by `getCraftingStationAt(pos)` checking ±1 Y.

## 🟠 Crafting UX gaps

- [ ] **Silent craft failure** — selecting a recipe you can't craft (wrong station, not enough items) re-renders unchanged with no feedback. Consider a short ephemeral error message or disabled select option.
- [ ] **`canFit` false-negative on full inventory** — rejects a craft that would free a slot via ingredient consumption. Rare, but solvable by simulating removal before the capacity check.

## 🟠 Rendering other players

- [x] **Blocked move removes player from chunk map but never re-adds** — occupant lists now only change on a real chunk crossing. `PlayerMovement.kt`
- [x] **Timeout/leave doesn't clean `chunkManager.players`** — `addPlayer` now calls `chunkManager.removePlayer(id)` before re-registering. `Game.kt`, `ChunkManager.kt`

## 🟡 World generation

- [ ] **Decoration unimplemented** — `decorate()` is `TODO()`; both decoration passes are comments. `OverworldGenerator.kt:51-62` — _SKIPPED: incomplete feature system; never called._
- [x] **Ore placement not seed-reproducible** — now uses seeded `oreNoise` Perlin with `ORE_THRESHOLD`. `OverworldGenerator.kt`
- [x] **`setTile` silently no-ops on unloaded chunks** — `setTile` now returns `Boolean` and logs; callers bail instead of mutating inventory on a dropped edit. `World.kt`

## ⚪ Cleanup

- [x] **Delete `TileType`** — removed; `ItemType` maps to `Tile` directly via `getTile()`.
- [x] **`Vec.toEnvPos` return type** — tightened to `Vec`. `Vec.kt`
- [x] **Missing-chunk renders `":x:"` literal** — now uses `Emojis.getEmojiCode("null")`. `GameRenderer.kt`
- [x] **`playerEnvUiCache` only keys on world string** — cache key now folds in game mode, selected slot, item type/count and inventory size. `Game.kt`
- [x] **CLAUDE.md doc drift** — doc updated to reflect crafting system, constraints, and new types.
