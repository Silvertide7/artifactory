# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview
Artifactory is a Minecraft NeoForge mod (MC 1.21.1, NeoForge 21.1.x, mod ID `artifactory`) adding an item attunement system: players bond to non-stackable items at an **Attunement Nexus** block to unlock per-level benefits (attribute bonuses, soulbound, unbreakable, invulnerable). Which items are attunable and what each level costs/grants is data-driven via datapack JSON. Curios API integration is optional at runtime.

## Build & Run
- **Build**: `./gradlew build` (jar lands in `build/libs/`)
- **Run client**: `./gradlew runClient` (username Dev); `./gradlew runClient2` (username Ved, for multiplayer testing)
- **Run server**: `./gradlew runServer`
- **Data generators**: `./gradlew runData` — regenerates `src/generated/resources/` (committed to the repo; it is a resource source dir)
- **Publish**: `./gradlew publishMods` — publishes to CurseForge; needs `CF_TOKEN` env var and reads `CHANGELOG.md` for the changelog
- Java 21 required. Gradle configuration caching is enabled.
- Version lives in `gradle.properties` (`mod_version`); update `CHANGELOG.md` alongside version bumps.
- **There is no test suite.** `src/test` is empty (build.gradle has a JUnit block, but no tests exist).

## Where things live
- Mod metadata template: `src/main/templates/META-INF/neoforge.mods.toml` (expanded from `gradle.properties` values by the `generateModMetadata` task — edit the template, never build output)
- Built-in datapack: `src/main/resources/builtin_data_packs/artifactory_default_data_pack/` (~31 vanilla item configs). Registered in `Artifactory.addPackFinders` as default-enabled but removable at world creation.
- Curios slot definitions: `src/main/resources/data/artifactory/curios/` — adds an `attuned_item` curios slot to players, gated by the `artifactory:is_attuned_item` validator predicate registered in `CuriosCompat`.
- `run/` (dev game dir) is gitignored; server config template at `run/config/artifactory-server.toml`.

## Architecture

### State model — three places, one source of truth
1. **`ArtifactorySavedData`** (world SavedData on the overworld) is the authoritative server-side record: `player UUID -> attunement UUID -> AttunedItem` (level, slot order, display name, item resource location, optional `AttunementOverride` snapshot). All mutations `setDirty()` and push `CB_*` packets to keep the owning client's `ClientAttunedItems` in sync.
2. **Item data components** (`DataComponentRegistry`):
   - `ATTUNEMENT_FLAG` — discovery state (`isAttunable`, `discovered`, `chance`); set lazily on pickup/tick/menu placement.
   - `PLAYER_ATTUNEMENT_DATA` ("attunement_data") — live attunement: attunement UUID, owner UUID + name, soulbound/invulnerable/unbreakable flags, accumulated `AttributeModification`s.
   - `ATTUNEMENT_OVERRIDE` — optional per-stack schema that takes precedence over datapack config.
3. **Datapack definitions** loaded into `AttunableItems.DATA_LOADER` (server) and mirrored to `ClientAttunementDataSource` (client).

The stack's `attunementUUID` points into SavedData; SavedData wins. `AttunementService.checkAndUpdateAttunementComponents` clears stale component data when no matching SavedData entry exists ("broken attunement" cleanup). It runs on item pickup, player tick, login, nexus slot placement, and curios equip — there is no global scan; stacks self-heal when touched.

### Schema resolution
`AttunementSchema` (interface: `slots_used`, `attunement_levels`, `chance`, `use_without_attunement`) is implemented by both `AttunementOverride` (component / AttunedItem snapshot) and `AttunementDataSource` (datapack). `AttunementSchemaUtil` resolves override first, then datapack. A schema is valid iff `attunementSlotsUsed() >= 0` (the null-override sentinel is -1). Client mirror: `ClientAttunementUtil.getClientAttunementSchema`.

### Lifecycle: discovery → attunement → ascension
- Items with `chance < 1.0` are "unidentified" until placed in the nexus input slot, where `AttunementService.discoverAttunementItem` rolls once and locks the flag.
- `AttunementMenu` (opened from `AttunementNexusBlock` — **no BlockEntity**; `SimpleMenuProvider` + `ContainerLevelAccess`) drives a 120-tick ritual in `broadcastChanges()`/`tickAttunement`. Completion fires `AttuneEvent.Pre` (cancellable) / `.Post` on the game bus, then `AttunementService.increaseLevelOfAttunement` (the single entry point for both first attunement and level-ups) and consumes XP levels + up to 3 required items (creative bypasses costs).
- `ModificationService.applyAttunementModifications` is idempotent: it clears all modification state then replays levels 1..N from the schema, so datapack edits retroactively apply the next time a stack is recalculated.
- Players have an `artifactory:attunement_slots` attribute (base 20, syncable) capping total `slots_used` across attuned items; configs can rebase it on login (`updateAttunementLevelAttribute`).
- Commands: `/artifactory slots` (anyone), `/artifactory admin <players> attunementLevels breakAll` (perm 2).

### Datapack loading pipeline (two-phase)
`MergeableCodecDataManager` (a `SimplePreparableReloadListener`, registered in `SystemEvents.onAddReloadListeners`) scans `data/<namespace>/artifactory/*.json`; the resource location `namespace:filename` names the target item. Entries using `apply_to_items` (item IDs or `#tags`) can't resolve at reload time — they're applied in `postProcess()` on `TagsUpdatedEvent`. Validation drops items that don't exist, stack > 1, or are BlockItems; `chance` is forced to 1.0 when `use_without_attunement` is false; malformed item-requirement strings are removed. Any entry with `replace: true` overrides others for the same ID. The full map is synced to clients on `OnDatapackSyncEvent` via `CB_SyncDatapackData` (each entry travels as a JSON string).

### Client/server split
Server logic: `AttunementUtil` / `AttunementSchemaUtil` / `ArtifactorySavedData` / `ServerConfigs`. Client mirrors: `ClientAttunementUtil` + `client/state` singletons (`ClientAttunedItems`, `ClientAttunementDataSource`, `ClientSyncedConfig`) fed by `CB_*` packets. Shared codepaths branch on `ServerPlayer` vs `LocalPlayer` (e.g. `ArtifactEvents.sidedIsUseRestricted`, `AttunementMenu.updateAttunementState`, `ItemAttributeModifierEvent` handler switching on `FMLEnvironment.dist`) — **when changing attunement checks, update both the server and client variants.**

### Enforcement & soulbound (events/)
- `ArtifactEvents`: cancels attack/left-click/right-click with use-restricted items (both sides); blocks pickup of others' attuned items (config); attuned item entities never despawn (and may be invulnerable); destroy/expire breaks the attunement in SavedData; `ItemAttributeModifierEvent` applies stored attribute modifications; a throttled player tick (every 18 ticks) re-validates held/worn gear and applies config-driven penalty effects (`minecraft:effect/level;...` strings parsed by `EffectUtil`).
- `SoulboundEvents`: on death (without keepInventory) stashes soulbound items into persistent player NBT (Twilight Forest charm-of-keeping approach); restores on respawn.

### Curios integration
Curios is **compile-only**; all runtime use is gated behind `CompatFlags.CURIOS_LOADED`, set by `CuriosSetup` only if the mod is loaded. Keep curios imports confined to `compat/` (callers reference `CuriosCompat` fully-qualified behind the flag). `CuriosCompat` blocks equipping restricted/foreign items, keeps soulbound curios on death, applies attribute modifications (except in the `attuned_item` slot), and ejects invalid curios on login.

## Datapack JSON format
```json
{
  "replace": false,
  "slots_used": 2,
  "chance": 1.0,
  "use_without_attunement": true,
  "apply_to_items": ["minecraft:diamond_sword", "#minecraft:swords"],
  "attunement_levels": [
    {
      "requirements": { "xp_levels_consumed": 20, "xp_level_threshold": 30, "items": ["minecraft:coal#64"] },
      "modifications": ["invulnerable", "attribute/minecraft:generic.attack_speed/add_multiplied_base/0.25/mainhand"]
    }
  ]
}
```
- Modification grammar: `soulbound` | `unbreakable` | `invulnerable` | `attribute/<attribute id>/<add_value|add_multiplied_base|add_multiplied_total>/<amount>/<slot group>` (parsed by `ModificationFactory`).
- Item requirement grammar: `modid:item#quantity`; only the first 3 are used (3 GUI slots).
- Omitted/negative xp values fall back to server config defaults (`xpLevelThreshold`, `xpLevelsConsumed` in `artifactory-server.toml`).
- All keys are snake_case exactly as shown — there are no legacy aliases; misspelled keys are silently ignored and fall back to defaults.
- `requirements.kills` and `requirements.feats` are parsed by the codec but **not enforced anywhere** — only XP and items gate attunement today.

## Conventions
- **No backwards-compatibility shims by default.** Fix problems permanently and cleanly — do not add legacy aliases, fallback parsers, or migration paths on your own. When a correct fix breaks backwards compatibility (datapack formats, save data, configs, network), state exactly what breaks and ask before choosing the approach; the maintainer decides.
- Package `net.silvertide.artifactory`; registries use the NeoForge `DeferredRegister` pattern (`registry/`).
- Network packets: NeoForge `PayloadRegistrar` + `StreamCodec`, registered in `network/Networking`; naming `CB_*` (client-bound) / `SB_*` (server-bound).
- Server config: NeoForge `ModConfigSpec` TOML (`config/ServerConfigs`), registered as `artifactory-server.toml`; most values are `worldRestart`.
- Most domain types hand-roll both a `Codec` (persistence) and a `StreamCodec` (network) side by side; follow that pattern for new components.
- User-facing text goes through translation keys in `assets/artifactory/lang/en_us.json` (`screen.*`, `playermessage.*`, `hovertext.*`).

---

# Reusable Engineering Standards

The sections below are project-agnostic. Copy this block (everything below the `---` separator) into any other project's `CLAUDE.md` unchanged to apply the same standards there.

## Code Style

**Never write comments.** No inline `//` comments, no `/* */` blocks, no javadoc, no leading explanatory headers on methods or fields. Code must be self-documenting through naming alone.

- Variable names describe what the value *is* (e.g. `armorCoveragePercent`, not `acp` with a comment).
- Method names describe what they *do* and under what conditions (e.g. `applyMultiplierIfAttackerIsPlayer`, not `applyBonus` with a comment explaining the player check).
- Extract a well-named helper method instead of writing a comment to explain a block.
- Constants get descriptive names that encode their meaning and unit (e.g. `KNIGHTMETAL_BONUS_DAMAGE_AT_FULL_ARMOR`, not `MAX` with a `// 2.0 vs fully-armored target` comment).
- If a name would need a comment to explain it, rename it until it doesn't.

Existing files may still contain comments and javadoc — leave them in place when editing unrelated code, but do not add new ones and prefer to delete obsolete ones when touching the surrounding code.

**Never leave dead code.** No unused methods, fields, classes, parameters, or imports. No "escape hatch" or "just in case" code. No commented-out blocks. If it's not called, delete it — the git history is the archive.

## Code Review

When asked to review code, do a "pass", check for issues, or otherwise audit a recent change, do **two** passes in order:

1. **Self-audit first.** Read the diff yourself. Fix the obvious — dead code, comments, naming, anything that violates the Code Style rules above. Report findings.
2. **Then spawn an independent reviewer** via the `/code-review` skill or a fresh agent. Give it only the diff and the goal, no context about why you made the choices you did. That catches the bugs you would otherwise rationalize away.
3. **Project Problems check** by running a whole project search in the problems / project tab. Have the user download and put this file into the claude_reference/problems to check. Ask before deploys if we should do this.
   Don't skip step 2 because step 1 looked clean — the value of the independent reviewer is exactly that it doesn't share your blind spots.

## Version Control

**The user handles commits in git.** Never run `git add`, `git commit`, or `git push` — and don't suggest doing so — unless the user explicitly asks. Wrap up work by reporting what changed; staging and pushing are the user's job.
