# Artifactory — Code Audit

**Date:** 2026-06-12
**Method:** Two independent full-codebase audits (primary + independent reviewer agent), reconciled and cross-verified against decompiled NeoForge 1.21.1 sources where API behavior was in question.
**Scope:** all `src/main/java` (~81 files), datapack JSONs, `build.gradle`, `gradle.properties`, `neoforge.mods.toml` template.

## Legend
- `- [x]` ✅ **FIXED** — resolved this session, verified by compile + trace.
- `- [ ]` — open, not yet addressed.
- Confidence reflects how certain the finding is, not its severity.
- Line numbers in files edited this session (`AttunementMenu`, `AttunementRequirements`, `ArtifactorySavedData`, `MergeableCodecDataManager`, `SystemEvents`, `ClientAttunementUtil`) may have shifted slightly; methods are named for reference.

## Status summary
| Severity | Total | Fixed | Open |
|---|---|---|---|
| Critical | 2 | 2 | 0 |
| High | 8 | 3 | 5 |
| Medium | 12 | 0 | 12 |
| Low | ~16 | 0 | ~16 |

---

## CRITICAL

- [x] ✅ **FIXED — #1 [bug] `AttunementMenu` — ascension gate validates against a stale cached flag (free-attunement exploit + broken attune flow).**
  `canAscensionStart` was only recomputed on input-slot changes, never on requirement-slot changes, and `tickAttunement` trusted the cached data slot at completion. Exploit: arm the gate with requirement items in the slots, pull them back out (slot lock only applies *during* the ritual), then attune — `consumeRequiredItems()` no-ops on empty slots and `increaseLevelOfAttunement` never re-checks cost, so you keep the items. Inverse: button stayed greyed when items were added in the "correct" order.
  **Fix (3 parts):** (a) requirement slots' `setChanged()`/`onTake()` now set `recalculateAttunementState = true`; (b) `tickAttunement` now re-validates authoritatively via extracted `computeCanAscensionStart(serverPlayer)` instead of reading the cached slot; (c) reordered `updateAttunementState` so `updateItemSlotRequirements()` (sets each slot's `stackRequired`) runs **before** `updateAscensionCanStart()` — previously `resetDerivedState()` cleared `stackRequired` and the gate evaluated `hasRequiredItems()` vacuously (returns true when no requirement set), so the item check never actually ran. Confidence: high.

- [x] ✅ **FIXED — #2 [bug] `AttunementRequirements` codec vs shipped datapack JSON — per-level XP costs silently ignored.**
  Codec read `xp_levels_consumed` / `xp_level_threshold`; all 31 shipped JSONs (and the documented format) used camelCase `xpLevelsConsumed` / `xpLevelThreshold`. Unknown keys are dropped by `RecordCodecBuilder`, so every per-level XP value fell back to the flat server-config defaults.
  **Fix:** migrated all 127 keys across the 31 builtin JSONs to canonical snake_case; codec unchanged (snake_case only). Per maintainer decision: **no** camelCase back-compat alias — misspelled keys fall back to defaults. Verified the snake_case codec round-trips against the exact DFU build MC 1.21.1 ships (8.0.16). **Player-visible balance change:** per-level XP costs now actually apply for the first time since the 1.21 port. Confidence: high.

---

## HIGH

- [x] ✅ **FIXED — #3 [bug] `AttributeModification` (`fromAttunementDataString`, `equipmentSlotGroupFromString`) — malformed modification string crashes the server.**
  `equipmentSlotGroupFromString` threw `IllegalArgumentException` on an unknown slot name; `ResourceLocation.parse(attribute)` threw on a bad id; `ModificationFactory` only caught in the `BasicModificationType` branch. A datapack typo (e.g. `.../main_hand` instead of `mainhand`, or an uppercase attribute id) crash-looped the dedicated server when the item recalculated in the nexus.
  **Fix:** `fromAttunementDataString` now honors its `@Nullable` contract for all malformed input — `equipmentSlotGroupFromString` returns `null` (was: throw) and is null-checked with a warn; the attribute id is parsed with `ResourceLocation.tryParse` + null-check + warn instead of throwing `parse`. No path in `createAttunementModification` can throw now; `buildAttributeModifier`'s remaining `parse` is safe because the attribute is validated first. Confidence: high.

- [x] ✅ **FIXED — #4 [bug] `AttunableItems:13` (`processItemAttunements`) — `raws.getFirst()` throws on an all-failed-parse ID → reload/start crash.**
  `prepare()` dropped codec failures via `resultOrPartial(...).ifPresent(raws::add)`, but `map.put(id, merger.apply(raws))` ran unconditionally. If every JSON for an ID failed the codec, `raws` was empty and `getFirst()` threw inside `prepare`, failing the whole resource load.
  **Fix:** guarded the merge call site in `prepare()` with `if (!raws.isEmpty())` — an ID whose every JSON failed is now skipped (already logged per-file) instead of crashing. The guard protects any merger, so `processItemAttunements`'s `getFirst()` is never reached with an empty list. Confidence: high.

- [x] ✅ **FIXED — #5 [bug] `MergeableCodecDataManager.postProcess` — throwing `ResourceLocation.parse` on `apply_to_items` item entries crashes reload.**
  Item branch used throwing `ResourceLocation.parse(itemPath)`; the tag branch right above used `tryParse` + null check. A malformed item id (e.g. `"Minecraft:Foo"`) in any pack's `apply_to_items` threw inside `postProcess` (runs on `TagsUpdatedEvent`) → world load / reload crash.
  **Fix:** item branch now uses `ResourceLocation.tryParse(itemPath)` with a null check (`resourceLocation != null && isValidItem(...)`), mirroring the tag branch — a malformed id is skipped instead of crashing the reload. Confidence: high.

- [ ] **#6 [bug] `ItemRequirements.parseItemStack:~40` — trailing `#` throws `ArrayIndexOutOfBoundsException` during reload.**
  `"a#".split("#")` yields length-1 array (trailing empties stripped); `itemParts[1]` is guarded only by `catch (NumberFormatException)`, so a trailing `#` or bare `"#"` throws AIOOBE. Reached server-side from `sanitizeItemRequirements` during datapack load — a one-char typo fails the whole reload instead of log-and-remove. (Also: inconsistent contract — returns `null` in some failure paths and `ItemStack.EMPTY` in others.) Confidence: high.

- [ ] **#7 [bug/perf] `EffectUtil:~37` — bad effect id in server config crashes the player tick; config string re-parsed every application.**
  `BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(effectName))` — `parse` throws on malformed ids and nothing catches it; chain is `onPlayerTick → applyEffectsToPlayer → applyMobEffectInstancesToPlayer` (server tick). An admin typo in `wearRestrictedEffects` / `holdingOtherPlayersItemEffects` hard-crashes the server when a player holds a restricted item. Also re-splits/re-resolves the constant config string on every application (per item, per 18-tick window, per player) — parse once and cache. Confidence: high.

- [ ] **#8 [bug] `trident.json` / `turtle_helmet.json` — `forge:swim_speed` no longer exists on NeoForge 1.21.1.**
  Verified against NeoForge sources: the attribute is `neoforge:swim_speed` (`NeoForgeMod.SWIM_SPEED`, description id `neoforge.swim_speed`). `forge:swim_speed` resolves to empty → `fromAttunementDataString` returns null → modification silently dropped with no log. The trident's signature benefit (5 levels) and the turtle helmet's do nothing; tooltips drop it too. Confidence: high.

- [ ] **#9 [bug] `ArtifactEvents.onPlayerTick:~95` — per-player throttle uses one static counter; some players never re-validated.**
  `private static short ticksIgnoredSinceLastProcess` is incremented once per player per tick and reset on reaching 18. With an even player count (18 ≡ 0 mod 2) the reset always lands on the same player; the other is processed every 9 ticks and the rest can be starved entirely — their restricted-gear penalty effects lapse and held/worn stacks stop self-healing. Must be per-player. Confidence: high.

- [ ] **#10 [bug] `CodecTypes.UUID_CODEC` + `ArtifactorySavedData` load/save — UUID codec throws instead of `DataResult.error`; SavedData silently discards on failure.**
  `UUID.fromString(ops.getStringValue(input).getOrThrow())` throws out of the codec. Load uses `.result().orElse(new HashMap<>())`, save uses `.result().orElse(new CompoundTag())`. A corrupted UUID in `artifactory.dat` crashes world load (exception bypasses `orElse`); conversely any *reported* parse/encode error silently wipes all attunements (load) or writes empty (save) with no log. Confidence: high.

---

## MEDIUM

- [ ] **#11 [bug] `AttunementMenu.getAttunementInputSlot().mayPlace` — side effects in a predicate; runs independently on client & server.**
  `mayPlace` calls `checkAndUpdateAttunementComponents` (component writes) and `discoverAttunementItem` (rolls `Math.random()` and locks a flag). Runs on both logical sides during click handling / `moveItemStackTo`, so client and server roll different discovery outcomes → flicker/desync until resync. Also uses `Math.random()` instead of the level's `RandomSource`, and mutates on mere placement *attempts*. Confidence: high.

- [ ] **#12 [bug] `ClientAttunedItems` / client state — caches never cleared on disconnect.**
  `clearAllAttunedItems()` is only called by `CB_ResetAttunedItems` (admin command). No `ClientPlayerNetworkEvent.LoggingOut` handler exists; login sync sends per-item updates with no leading reset. `ClientAttunementDataSource` / `ClientSyncedConfig` likewise never reset. Attunements/config from a previous world/server linger after joining another — ghost slot counts and stale manage-screen entries. Confidence: high.

- [ ] **#13 [bug] `AttributeModification` — `combineWith` ignores the operation; modifier IDs are random per parse.**
  `applyModification` combines on attribute+slot only (not operation), and `combineWith` keeps the existing modifier's operation while summing amounts: `add_value/1` + `add_multiplied_base/0.25` collapses into one `add_value/1.25`. Also `fromAttunementDataString` embeds `UUID.randomUUID()` in the modifier id, so every recalc (every nexus input-slot change) rewrites the component with new IDs → component churn, needless resyncs, prediction mismatch. Confidence: high.

- [ ] **#14 [bug] Side handling is physical (`FMLEnvironment.dist`), not logical; client/server attunement checks diverge.**
  `ArtifactEvents.onApplyAttributeModifier` switches on `FMLEnvironment.dist`, so on an integrated server the *logical server* reads client singletons (`ClientAttunementDataSource`) from the server thread (cross-thread, wrong source). `ClientAttunementUtil.isUseRestricted` skips the attunement-flag check the server makes and ignores `useWithoutAttunement` when player data is present → client cancels attacks/use the server allows. `CuriosCompat` attribute modifiers and the `is_attuned_item` validator are `ServerPlayer`-only → client stat display / equip prediction disagree. CLAUDE.md explicitly requires server+client variants stay in lockstep. Confidence: high (logic divergence); medium (in-game reachability).

- [ ] **#15 [bug] `DeleteConfirmationScreen.onClose` — closes the entire GUI stack instead of the dialog.**
  Opened via `pushGuiLayer`, but `onClose()` calls `super.onClose()` → `Minecraft.setScreen(null)`, which (verified in NeoForge sources via `ClientHooks.clearGuiLayers`) tears down the whole stack. Cancel/Confirm/ESC kicks the player out of the Manage screen (and the nexus screen beneath it) instead of returning. Should `popGuiLayer()`. Confidence: high (pattern verified against sources).

- [ ] **#16 [bug] `ManageAttunementsScreen.mouseScrolled:~253` — wheel scrolls the whole list per notch.**
  `updateSliderProgress(this.sliderProgress - (float) scrollY)` subtracts ±1 from a 0..1 clamped value, so one notch jumps top↔bottom. Unusable for >6 attunements except via slider drag; should divide by scrollable distance / card count. Confidence: high.

- [ ] **#17 [bug] `MergeableCodecDataManager.postProcess` + `AttunableItems` merger — surprising datapack precedence.**
  `postProcess` does `data.putAll(...)` after direct entries are loaded, so a broad `apply_to_items` tag entry clobbers a specific `namespace:item.json` entry regardless of `replace`; overlapping tag sources resolve in `HashMap` iteration order; the merger takes `raws.getFirst()` unless a later pack sets `replace:true`, so a plain higher-priority override file doesn't win. Confidence: high (putAll clobber, HashMap nondeterminism); medium (resource-stack ordering).

- [ ] **#18 [bug] `ArtifactEvents` — use-restriction enforcement has gaps; offhand over-blocks.**
  Only `AttackEntityEvent`, `LeftClickBlock`, `RightClickItem` are cancelled. Item-on-block uses (hoe till, shovel path, axe strip, flint & steel) go through `RightClickBlock`/`useOn`; `EntityInteract` (shears, saddles) is unhandled — restricted tools still work for these. Conversely, a restricted item in the *offhand* blocks all melee (`onLivingAttack` checks both hands), stricter than intended and inconsistent with `LeftClickBlock`. Confidence: high.

- [ ] **#19 [perf/bug] `CB_SyncDatapackData` + `SystemEvents.onDatapackReload` — whole datapack as one packet of JSON strings; duplicated per tag item; live map captured.**
  `encode` writes `CODEC.encodeStart(JsonOps...).toString()` per entry (a binary `STREAM_CODEC` already exists); every item matched by an `apply_to_items` tag serializes the identical JSON body again; `writeUtf` caps one entry at 32767 bytes and the whole map must fit one ~1 MiB clientbound payload (big modpacks can kick clients on login). The packet captures the **live** `getData()` map, encoded later on the netty thread → races a concurrent `/reload` `data.clear()`. Decode uses `.getOrThrow()` → any error disconnects the client. Pass `Map.copyOf(...)`. Confidence: high (duplication/limits/live ref); medium (reload race in practice).

- [ ] **#20 [bug] `SoulboundEvents.onPlayerDeath` — items stashed at `EventPriority.HIGH`; later death-cancel strands them.**
  Soulbound items are removed from inventory and written to persistent NBT before NORMAL/LOW handlers run. If a revival mod (totem-style) cancels the death afterward, the player stays alive with the gear missing until their next real respawn restores it. Confidence: medium (depends on third-party priorities; code order verified).

- [ ] **#21 [bug] `ArtifactEvents` — destroyed attuned item entities leak their attunement slots.**
  Attuned items get `setUnlimitedLifetime` (and may be invulnerable), so `ItemExpireEvent` never fires for them; a non-invulnerable attuned item destroyed by lava/explosion fires no handled event. The SavedData entry and its reserved slots persist until the player manually breaks the bond. Confidence: high.

- [ ] **#22 [bug] `ArtifactorySavedData.get()` — returns a throwaway instance on clients, masking side confusion.**
  Returns `new ArtifactorySavedData()` when `getCurrentServer()` is null (remote client) and the real data on an integrated server. This silently masks side-confusion bugs (e.g. the unguarded `PlayerDestroyItemEvent` handler, the client menu ctor reading server data cross-thread on a LAN host). Confidence: medium.

---

## LOW

- [ ] **[bug] `GUIUtil.playSound:~130` — sounds played at volume 20** (≈320-block audible range), broadcast every 3 ticks during the ritual. Almost certainly meant ~1.0–2.0. Confidence: high.
- [ ] **[bug] `GUIUtil.convertToPercentage` — `0.07` renders `7.000000000000001%` (IEEE artifact) and everything gets a trailing `.0`** (e.g. `50.0%`). Cosmetic, on every unidentified-item tooltip. Confidence: high.
- [ ] **[smell] `AttunementScreen.render` / `ManageAttunementsScreen.render` / `DeleteConfirmationScreen.render` — `catch (Exception ignore) { onClose(); }`** around the whole render pass swallows every GUI bug unlogged ("screen randomly closes"). Confidence: high.
- [ ] **[bug] `AttunedItem.buildAttunedItem` — `order = numAttunedItems + 1` produces duplicates after a removal** → unstable manage-screen sort. Confidence: high.
- [ ] **[bug] `ManageAttunementsScreen.getItemBorderOffset` — range gap `[0.65, 0.66)`** falls through to the lowest border tier (e.g. 13/20 = 0.65). Cosmetic. Confidence: high.
- [ ] **[bug] `ManageAttunementsScreen.mouseDragged` — slider-drag origin shifts with its own value** (`SLIDER_HEIGHT*sliderProgress` in the base coord), minor non-linearity. Confidence: medium.
- [ ] **[bug] `ItemRequirementSlot` — item requirements above a slot's max stack are unfulfillable** (e.g. `ender_pearl#32` — single slot can't hold the count). Confidence: high.
- [ ] **[bug] `DataComponentUtil.makeUnbreakable` / unbreakable lifecycle — free full repair on bond break.** `makeUnbreakable` zeroes damage; breaking the attunement removes UNBREAKABLE but the item keeps the free repair. Confidence: medium.
- [ ] **[smell] Shared mutable codec defaults** — `optionalFieldOf("...", new ArrayList<>())` in `AttunementLevel` / `AttunementRequirements` aliases one list instance across all parsed objects. Use `List.of()`. Confidence: high.
- [ ] **[smell] Hand-written `equals`/`hashCode` on records** (`PlayerAttunementData`, `AttunementOverride`, `AttunementLevel`, `AttunementRequirements`) duplicate the generated ones; can silently drift when a field is added. Confidence: high.
- [ ] **[smell] `MergeableCodecDataManager.getData()` documented immutable but returns the live `HashMap`** (also handed to the sync packet) — leaky encapsulation. Confidence: high.
- [ ] **[design] Client-only classes referenced from common/server code** — `client.state.ItemRequirements` is a server-side datapack-sanitizer dependency; `GUIUtil` (imports client `Font`/`GuiGraphics`) hosts server-used `prettifyName`; `LocalPlayer` imported in `AttunementMenu`/`ArtifactEvents` (loaded on dedicated servers). Works only because the branches never execute; inverts the project's client/server-split convention. Confidence: high.
- [ ] **[smell] `AttunementNexusBlock` — missing `@Override` on `getMenuProvider`, `getStateForPlacement`, `getPistonPushReaction`.** Verified against 1.21.1 sources that all three *do* override correctly today (hygiene, not breakage) — but a future mapping change would silently turn them into dead non-overrides. Related: `NEEDS_IRON_TOOL` tag is inert without `requiresCorrectToolForDrops()`. Confidence: high.
- [ ] **[naming] Hardcoded English bypassing the lang file** — `"Soulbound"`/`"Invulnerable"` in `TooltipHandler`, `"Manage Attunements"`/`"No enhancements"` in `ManageAttunementsScreen`, `"Requires N ..."` in `AttunementNexusSlotInformation`, `§`-coded command output in `CmdNodeGeneral`. Also `add_multiplied_total` vs `add_multiplied_base` are indistinguishable in tooltips (only " Base" gets a suffix). Confidence: high.
- [ ] **[design] `AttunementUtil.isUseRestricted` sends chat messages as a side effect of a boolean query** — every attack/click spams the action bar, and the messaging is welded to the predicate (forced `applyEffectsToPlayer` to re-implement the checks). Confidence: high.
- [ ] **[design] Near-verbatim duplicated logic across server/client** — `createAttunementNexusSlotInformation`, `getAttunementSchema`/`getClientAttunementSchema`, `isUseRestricted`, `getAttunementLevel`, `getNumAttunementLevels` exist as 2–4 hand-synced copies (the `isUseRestricted` pair has already drifted, see #14). Confidence: high.
- [ ] **[perf] Hot-path allocations** — `ArtifactorySavedData.get()` builds a new `SavedData.Factory` per call (called from `ItemAttributeModifierEvent`, player tick, every query); `getAttunedItems` eagerly allocates a default `HashMap` per miss; `AttunementScreen` rebuilds the requirements `ArrayList` and `int[]` offset arrays every frame. Confidence: high.
- [ ] **[smell] Misc** — config comment drift (`"Default: 15"`/`35` over actual `20`/`30`), `spawnParticals` typo, spaced config key vs camelCase siblings, magic `18`/`20` coupling between `ArtifactEvents` and `EffectUtil`, dead lang keys, unused `dispatcher` params in `CmdNode*`, boxed `Integer` fields in `CB_SyncServerConfigs`, no optional `curios` dependency/ordering in `neoforge.mods.toml`, non-volatile `CompatFlags.CURIOS_LOADED` written during parallel mod setup, `kills`/`feats` parsed but never enforced. Confidence: high.

---

## Additional fixes this session (outside the formal audit)

- [x] ✅ **FIXED — `MergeableCodecDataManager.apply()` did not clear maps on reload.** `putAll` without a preceding `clear()` meant entries removed from a datapack lingered until a full restart (`/reload` wouldn't drop them). Fixed by clearing `rawData` and `data` at the top of `apply()`; `postProcess()` rebuilds `apply_to_items`/tag entries from `rawData` on the subsequent `TagsUpdatedEvent`, so tag-derived entries are not lost.
- [x] ✅ **FIXED — obsolete `attunedPlayers` map removed from `ArtifactorySavedData`.** A write-only remnant of the removed "unique attunement" feature (written on attune + login, never read; the `getPlayerName` reader was deleted in an earlier commit). Removed the map, its codec, `setPlayerName`, `updatePlayerDisplayName`, and the login call. Owner-name display still works via `PlayerAttunementData.attunedToName` on the stack. No save migration needed (the `attuned_players` NBT key was never read on load).

---

## Notes
- Two items remain dependent on third-party behavior and were not fully verifiable here: `PlayerDestroyItemEvent` client-side firing (#22 context) and revival-mod interaction (#20).
- Suggested fix order for the remaining work: high-severity crash family (#3–#7, all "bad data shouldn't kill the server", same shape of fix), then #8/#9/#12/#15/#16, then the rest.
