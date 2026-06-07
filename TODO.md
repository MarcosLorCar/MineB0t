# MineB0t TODO

## Completed in recent checkup

- **Interaction Handler Consolidation** — Refactored the fragmented `EventHandler` system into a centralized `InteractionListener` with a registry-based lookup. This significantly improves efficiency by reducing the number of listeners per event and avoids the "Failed to acknowledge" error by providing a fallback for unhandled/stale interactions.
- **Thread-safe GamesManager** — Synchronized game initialization using `ConcurrentHashMap.computeIfAbsent` to prevent race conditions when multiple players join a new guild simultaneously.
- **Improved Documentation** — Expanded `README.md` and updated `GEMINI.md` to reflect architectural changes.

## Crafting UX gaps


- **Silent craft failure** — selecting a recipe you can't craft (wrong station, not enough items) re-renders unchanged with no feedback. Consider a short ephemeral error message or disabled select option.
- **`canFit` false-negative on full inventory** — rejects a craft that would free a slot via ingredient consumption. Rare, but solvable by simulating removal before the capacity check.

## World generation

- **Decoration unimplemented** — `decorate()` is `TODO()`; both decoration passes are comments. `OverworldGenerator.kt:51-62`

---

## Suggestions

### Tool tiers + mining speed
Right now all tools are equal and crafting has no real purpose. Giving pickaxes/axes tiers would create the core progression loop:
- Bare hands: very slow, can only break dirt/grass
- Stone pickaxe: normal speed, breaks stone
- Iron pickaxe: fast, breaks everything

Players would have a clear path: punch dirt → craft stone pickaxe → mine stone+iron → craft iron pickaxe → go deeper. This is likely the single highest-leverage change for making the game feel like it has a point.

### Depth/leaderboard visibility
Show "Deepest player in this server: PlayerX at Y=-142" somewhere in the world view. Pure social competition with near-zero implementation cost; drives exploration without needing any new systems.

### Hunger / food system
Instead of a mobile-style energy bar that limits actions, a hunger mechanic fits the game thematically: players need to eat to mine at full speed, food is cooked at the furnace, crops can be farmed. Achieves the "come back later" hook without feeling punitive. Works well on top of the furnace crafting station already in the game.

### Turn-based combat
The top-left button in the world view (currently a disabled placeholder) could become an Attack button, enabled only when an enemy is on an adjacent tile. Pressing it opens a `ViewState.COMBAT` screen.

Combat view replaces the entire button grid with combat actions (Attack, Heavy, Block, Flee, Use Item). An embed shows enemy name/HP bar, player HP bar, and a log of the last exchange. Turn resolution: player presses a button → their action and the enemy AI both resolve → embed updates. No tick dependency, fully reactive.

Mobs exist as entities in the world (separate from tiles), managed by a `MobManager` per `World`. They spawn based on depth/biome (e.g. slimes near surface, cave bats underground). Encounter triggers when the player walks into the same tile as a mob (auto-trigger, Terraria-style). Player death: respawn at spawn, drop items or lose half inventory.

Suggested first slice: `Mob` data class + `MobManager`, `ViewState.COMBAT` + `CombatSession` per player, one basic enemy (slime), combat button layout + embed renderer. Leave HP persistence, death penalties, and mob wandering AI for after the core loop is playable.
