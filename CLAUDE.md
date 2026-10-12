# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview
Artifactory is a Minecraft NeoForge mod (MC 1.21.1, NeoForge 21.1.x, mod ID `artifactory`) adding an item attunement system: players bond to non-stackable items at an **Attunement Nexus** block to unlock per-level benefits (attribute bonuses, soulbound, unbreakable, resilient). Which items are attunable and what each level costs/grants is data-driven via datapack JSON. Attuned items are protected by default: they never despawn, ignore lava/fire/explosions, and are returned to their owner if they fall into the void.

## Dependencies
| Mod | Mod ID (mods.toml) | CurseForge slug | Required? | Range | Used for |
|---|---|---|---|---|---|
| SilverLib | `silver_lib` | `silverlib` | required | `[1.2.2,)` | breakage (`resilient`, broken-item rules), soulbound (death stash/restore) |
| QualityAPI | `quality_api` | `qualityapi` | optional | `[1.3.2,)` | `min_quality`, keeping an attuned item's tier on break |
| Curios | `curios` | `curios` | optional | `9.5.1` | `attuned_item` slot, curio restrictions, soulbound curios |
| NeoForge | `neoforge` | | required | `[21.1.228,)` | dev runs on 21.1.249 (SilverLib/QualityAPI need ≥ 21.1.228) |

- The mod ID goes in `neoforge.mods.toml`; the CurseForge slug goes in `build.gradle`'s `publishMods { curseforge { requires(...) / optional(...) } }`. They differ for SilverLib (`silver_lib` vs `silverlib`). Look slugs up from the project id (e.g. `https://api.cfwidget.com/<project id>`) instead of guessing.
- Dev jars come from CurseMaven; the file ids are `silverlib_version` / `qualityapi_version` in `gradle.properties`. When a dependency is released, update the file id **and** the mods.toml range, and confirm the version by reading `META-INF/neoforge.mods.toml` inside the downloaded jar (`~/.gradle/caches/modules-2/.../*-<file id>.jar`).
- SilverLib and QualityAPI live next to this repo (`../silver_lib`, `../quality_api`) and are maintained alongside it. Release order when a change spans them: SilverLib → QualityAPI → Artifactory, each pointing at the previous one's new file id.


## Build & Run
- **Build**: `./gradlew build` (jar lands in `build/libs/`)
- **Run client**: `./gradlew runClient` (username Dev); `./gradlew runClient2` (username Ved, for multiplayer testing)
- **Run server**: `./gradlew runServer`
- **Data generators**: `./gradlew runData` — regenerates `src/generated/resources/` (committed to the repo; it is a resource source dir)
- **Publish**: the GitHub `publish.yml` workflow runs `./gradlew publishMods` on a published GitHub release (needs `CF_TOKEN` and the Discord webhook secrets). Only the newest `## ` section of `CHANGELOG.md` is sent to CurseForge/Discord.
- Java 21 required. Gradle configuration caching is enabled.
- Version lives in `gradle.properties` (`mod_version`); update `CHANGELOG.md` alongside version bumps.
- **There is no test suite.** `src/test` is empty (build.gradle has a JUnit block, but no tests exist). See **Testing** below for how changes get verified.

## Where things live
- Mod metadata template: `src/main/templates/META-INF/neoforge.mods.toml` (expanded from `gradle.properties` values by the `generateModMetadata` task — edit the template, never build output)
- Built-in datapack: `src/main/resources/builtin_data_packs/artifactory_default_data_pack/` (~31 vanilla item configs). Registered in `Artifactory.addPackFinders` as default-enabled but removable at world creation.
- Curios slot definitions: `src/main/resources/data/artifactory/curios/` — adds an `attuned_item` curios slot to players, gated by the `artifactory:is_attuned_item` validator predicate registered in `CuriosCompat`.
- `run/` (dev game dir) is gitignored; server config template at `run/config/artifactory-server.toml`.
- Mixins: `src/main/resources/artifactory.mixins.json`, package `net.silvertide.artifactory.mixin` (mixins only; logic lives in services): `EntityMixin` (void return) and `ItemStackMixin` (`durable`). Registered by the `[[mixins]]` block in the mods.toml template.
- `claude_reference/` (gitignored): `audit.md` (audit checklists), `problems/` (IntelliJ Problems exports), `test_datapack/` (dev-only test pack).

## Architecture

### State model — three places, one source of truth
1. **`ArtifactorySavedData`** (world SavedData on the overworld) is the authoritative server-side record: `player UUID -> attunement UUID -> AttunedItem` (level, slot order, display name, item resource location, optional `AttunementOverride` snapshot). All mutations `setDirty()` and push `CB_*` packets to keep the owning client's `ClientAttunedItems` in sync. It also holds a server-only `player UUID -> stacks` queue of items returned from the void (`items_returned_from_void`, stacks saved with registry ops; a stack that no longer loads, e.g. from a removed mod, is kept as raw NBT and written back), which pushes no packets.
2. **Item data components** (`DataComponentRegistry`):
   - `ATTUNEMENT_FLAG` — discovery state (`isAttunable`, `discovered`, `chance`); set lazily on pickup/tick/menu placement.
   - `PLAYER_ATTUNEMENT_DATA` ("attunement_data") — live attunement: attunement UUID, owner UUID + name, soulbound/invulnerable/unbreakable/resilient flags, accumulated `AttributeModification`s.
   - `ATTUNEMENT_OVERRIDE` — optional per-stack schema that takes precedence over datapack config.
3. **Datapack definitions** loaded into `AttunableItems.DATA_LOADER` (server) and mirrored to `ClientAttunementDataSource` (client).

The stack's `attunementUUID` points into SavedData; SavedData wins. `AttunementService.checkAndUpdateAttunementComponents` clears stale component data when no matching SavedData entry exists ("broken attunement" cleanup). It runs on item pickup, player tick, login, nexus slot placement, curios equip, and on attuned stacks inside SilverLib's `ItemBreakingEvent` and QualityAPI's `QualityBreakDowngradeEvent` — there is no global scan; stacks self-heal when touched.

### Schema resolution
`AttunementSchema` (interface: `slots_used`, `attunement_levels`, `chance`, `use_without_attunement`) is implemented by both `AttunementOverride` (component / AttunedItem snapshot) and `AttunementDataSource` (datapack). `AttunementSchemaUtil` resolves override first, then datapack. A schema is valid iff `attunementSlotsUsed() >= 0` (the null-override sentinel is -1). Client mirror: `ClientAttunementUtil.getClientAttunementSchema`.

### Lifecycle: discovery → attunement → ascension
- Items with `chance < 1.0` are "unidentified" until placed in the nexus input slot, where `AttunementService.discoverAttunementItem` rolls once and locks the flag.
- `AttunementMenu` (opened from `AttunementNexusBlock` — **no BlockEntity**; `SimpleMenuProvider` + `ContainerLevelAccess`) drives a 120-tick ritual in `broadcastChanges()`/`tickAttunement`. Completion fires `AttuneEvent.Pre` (cancellable) / `.Post` on the game bus, then `AttunementService.increaseLevelOfAttunement` (the single entry point for both first attunement and level-ups) and consumes XP levels + up to 3 required items (creative bypasses costs).
- `ModificationService.applyAttunementModifications` is idempotent: it clears all modification state then replays levels 1..N from the schema, so datapack edits retroactively apply the next time a stack is recalculated.
- Players have an `artifactory:attunement_slots` attribute (base 20, syncable) capping total `slots_used` across attuned items; configs can rebase it on login (`updateAttunementLevelAttribute`).
- Commands: `/artifactory slots` (anyone), `/artifactory admin <players> attunementLevels breakAll` (perm 2).

### Datapack loading pipeline (two-phase)
`MergeableCodecDataManager` (a `SimplePreparableReloadListener`, registered in `SystemEvents.onAddReloadListeners`) scans `data/<namespace>/artifactory/*.json`; the resource location `namespace:filename` names the target item. Entries using `apply_to_items` (item IDs or `#tags`) can't resolve at reload time — they're applied in `postProcess()` on `TagsUpdatedEvent`. Validation drops items that don't exist, stack > 1, or are BlockItems; `chance` is forced to 1.0 when `use_without_attunement` is false; malformed item-requirement strings are removed. Any entry with `replace: true` overrides others for the same source ID. A specific per-item config always wins over an `apply_to_items`/tag entry targeting the same item (the tag entry is skipped with a logged warning); among competing tag entries, the alphabetically-last source config key wins (deterministic). The full map is synced to clients on `OnDatapackSyncEvent` via `CB_SyncDatapackData` (each entry encoded with `AttunementDataSource.STREAM_CODEC`).

### Client/server split
Server logic: `AttunementUtil` / `AttunementSchemaUtil` / `ArtifactorySavedData` / `ServerConfigs`. Client mirrors: `ClientAttunementUtil` + `client/state` singletons (`ClientAttunedItems`, `ClientAttunementDataSource`, `ClientSyncedConfig`) fed by `CB_*` packets. Shared codepaths branch on `ServerPlayer` vs `LocalPlayer` (e.g. `ArtifactEvents.sidedIsUseRestricted`, `AttunementMenu.updateAttunementState`, `ItemAttributeModifierEvent` handler switching on `FMLEnvironment.dist`) — **when changing attunement checks, update both the server and client variants.**

### Enforcement (events/)
- `ArtifactEvents`: cancels attack/left-click/right-click with use-restricted items (both sides); blocks pickup of others' attuned items (config); attuned item entities never despawn and are invulnerable to environmental damage (lava/fire/explosion — set unconditionally on join); destroy/expire breaks the attunement in SavedData (except a SilverLib-broken stack: `PlayerDestroyItemEvent` never removes its attunement); `ItemAttributeModifierEvent` applies stored attribute modifications; a throttled player tick (every 18 ticks) re-validates held/worn gear, applies config-driven penalty effects (`minecraft:effect/level;...` strings parsed by `EffectUtil`) and delivers items waiting from the void; SilverLib's `ItemBreakingEvent` / `ItemKeptBrokenEvent` drive `resilient` and the break message (see the datapack section).

### Void return (`mixin/EntityMixin` → `services/VoidReturnService`)
- `EntityMixin` injects at HEAD of `Entity.onBelowWorld` (vanilla's void kill; `ItemEntity` doesn't override it, `LivingEntity` overrides without super so players/mobs never reach it). It only runs when an entity actually falls out of the world, so there is no per-tick cost. An earlier version used `EntityTickEvent.Pre`; it was replaced to avoid touching every entity every tick.
- `VoidReturnService.returnAttunedItemToOwner(Entity)` handles a loose `ItemEntity`, or an `AbstractArrow` (thrown trident) whose `pickup` is `ALLOWED` (a creative-thrown copy is not returned, so it can't duplicate). It skips entities something else already removed, heals stale components, gives a copy to the owner (a free inventory slot via `InventoryUtil.giveOrDropAtFeet`, else `player.drop`, which throws it forward from eye height), discards the entity and cancels the rest of `onBelowWorld`, so a rescue mod injecting after Artifactory can't also save it. The message is a chat message (several returns don't overwrite each other).
- Owner offline or dead: the stack is queued in `ArtifactorySavedData` (`items_returned_from_void`) and delivered by the throttled player tick once they are online and alive (covers login, respawn and revive mods, and runs after their handlers, so it never races SilverLib's soulbound restore).
- Not covered (documented, not a bug): attuned items inside containers (shulkers, bundles), on armor stands or in item frames, and other modded projectiles still die in the void, leaving their attunement in SavedData. A thrown item that lands back in the void when the owner's inventory is full just loops until they free a slot or move (maintainer decision).

### Soulbound (`events/SoulboundEvents`)
- Answers SilverLib's `SoulboundCheckEvent` with `keep()` when `AttunementUtil.isSoulboundActive(player, stack)` (never for `FakePlayer`s). SilverLib does the stash on death (after every `LivingDeathEvent` listener, before Curse of Vanishing) and the slot-exact restore on respawn.
- One-release migration: returns any pre-2.7.0 stash (`PlayerPersisted.artifactoryInventory`, vanilla `Inventory.save` slot bytes: 0-35, armor 100-103 → 36-39, offhand 150 → 40) on login (if alive) or respawn, then removes the tag; unreadable entries are logged. Delete once no pre-2.7.0 stashes remain.

### QualityAPI integration
QualityAPI is **compile-only**, gated behind `CompatFlags.QUALITY_LOADED` set by `CompatSetup`; imports stay in `compat/QualityCompat`. It answers `min_quality` checks and cancels `QualityBreakDowngradeEvent` for actively attuned items (healing stale attunement components first, so a stack whose bond was broken elsewhere can still lose a tier), so an attuned item never loses a tier when it breaks.

### Curios integration
Curios is **compile-only**; all runtime use is gated behind `CompatFlags.CURIOS_LOADED`, set by `CompatSetup` only if the mod is loaded. Keep curios imports confined to `compat/` (callers reference `CuriosCompat` fully-qualified behind the flag). `CuriosCompat` blocks equipping restricted/foreign items, keeps soulbound curios on death (one `DropRulesEvent` override, which also covers cosmetic slots), applies attribute modifications (except in the `attuned_item` slot), and ejects invalid curios on login.

## Datapack JSON format
```json
{
  "replace": false,
  "slots_used": 2,
  "chance": 1.0,
  "use_without_attunement": true,
  "min_quality": 3,
  "attunement_conditions": [
    { "type": "artifactory:component_int_range", "component": "simplyswords:awakening", "min": 8, "when_missing": "pass",
      "description": { "translate": "artifactory.condition.reverie.fully_awakened" } }
  ],
  "apply_to_items": ["minecraft:diamond_sword", "#minecraft:swords"],
  "attunement_levels": [
    {
      "requirements": { "xp_levels_consumed": 20, "xp_level_threshold": 30, "items": ["minecraft:coal#64"] },
      "modifications": ["unbreakable", "attribute/minecraft:generic.attack_speed/add_multiplied_base/0.25/mainhand"]
    }
  ]
}
```
- Modification grammar: `soulbound` | `unbreakable` | `resilient` | `durable/<multiplier>` | `attribute/<attribute id>/<add_value|add_multiplied_base|add_multiplied_total>/<amount>/<slot group>` (parsed by `ModificationFactory`). `invulnerable` was removed — all attuned items are now invulnerable when dropped — and is accepted as a no-op so existing JSONs don't break.
- `resilient` keys on the item having durability (`DataComponents.MAX_DAMAGE`), not on it currently being damageable, so it applies alongside `unbreakable` in any order. It stores `is_resilient` on `PlayerAttunementData` (optional in the codec, default false, so older saves load). `ArtifactEvents` calls SilverLib's `ItemBreakingEvent.keepBroken()` for it, so the item sits broken at zero durability instead of vanishing until repaired, and messages the owner on `ItemKeptBrokenEvent` for any attuned item SilverLib keeps broken (resilient or kept by another mod such as QualityAPI). The `Resilient` tooltip line only shows while the item can actually take damage. `PlayerDestroyItemEvent` never removes an attunement from a SilverLib-broken stack. `shouldApplyAttunementModifications` is false for any SilverLib-broken stack, so broken items grant no attunement attributes. Like soulbound, removing `resilient` from a datapack only clears it from existing stacks when they are next recalculated (nexus placement or level-up). Resilient keeps the item no matter who holds it, but the break message only goes to the owner when the owner is the one holding it. A resilient item that breaks keeps SilverLib's broken marker after its attunement ends, so it stays broken until repaired. Gaining `unbreakable` on an item that is currently broken makes it usable again at zero durability (SilverLib only treats damageable items as broken).
- `min_quality` (optional QualityAPI tier level, compared by number not name): when set, only stacks whose quality level is at least this can be attuned; a stack with no quality, or any stack while QualityAPI is absent, cannot. It gates new attunement only: checked in the datapack branch of `AttunementSchemaUtil.getAttunementSchema(stack)` (which the client also uses) and skipped for stacks that are already actively attuned, so a later datapack change, tier change or QualityAPI removal never breaks an existing attunement. Below-`min_quality` unattuned stacks get no schema at all, so they are also free of `use_without_attunement: false` restrictions (accepted: the combination is unusual). Smithing an attuned item keeps its attunement on the result even if the result's own `min_quality` is higher (consistent with "attuned items never lose attunement").
- `durable/<multiplier>` (a plain decimal ≥ 1, e.g. `2` or `1.5`; anything else is rejected with a logged warning) makes the item wear `multiplier` times slower: `mixin/ItemStackMixin` modifies the damage argument at the HEAD of `ItemStack.hurtAndBreak` (before NeoForge's `damageItem` and vanilla Unbreaking), and `DurabilityService` lets each point through with chance `1/multiplier` for actively attuned, damageable stacks. It samples the gap to the next kept point geometrically instead of rolling every point, and stops once the item would break (returning the original amount), so the work is bounded by the item's remaining durability whatever the damage amount or multiplier (mods do call `hurtAndBreak(Integer.MAX_VALUE, …)`). Any holder benefits, not only the owner, and a stale bond keeps the bonus until the stack self-heals (same as `resilient`). Non-finite or ≤ 1 stored multipliers are ignored; stacked products are clamped to `Float.MAX_VALUE`. Unbreaking then rolls on what's left, so the two multiply (Unbreaking III on a tool ×4, with `durable/2` ×8); QualityAPI's durability multiplier also multiplies. Several `durable` entries across levels multiply. Stored as `durability_multiplier` on `PlayerAttunementData` (optional, default 1, so older saves load), reset to 1 by `ModificationService` before replay, and shown as a "Durable ×N" trait line (rounded to 2 decimals) while the item is actively attuned and can take damage; the nexus uses the same "Durable ×N" text. Covers anything that wears through `hurtAndBreak`: tools, weapons, armour, shields, elytra, bows.
- Modification names are parsed with `toUpperCase(Locale.ROOT)` (a default-locale upper-case breaks `resilient` on Turkish-locale servers).
- `attunement_conditions` (optional list, default empty): every condition must pass to **start** an attunement and to **ascend**; checked in `AttunementUtil.canIncreaseAttunementLevel` (the single gate behind the nexus's `computeCanAscensionStart` and `AttunementService`), so a failing item never starts the progress animation. Existing bonds are never broken or changed by a condition that later fails. `AttuneEvent.Pre` is unchanged. Conditions live in `component/condition/`: a `"type"` dispatch codec (`AttunementConditions`, a plain type → codec map), synced to clients inside `AttunementDataSource.STREAM_CODEC` as JSON text (lossless: NBT would turn a `component_equals` `true` into `1`; up to 262144 characters; a client that can't read the synced list falls back to the always-failing `artifactory:unreadable` and logs an error, which only affects display since the server is the gate). Component ids are resolved when checked via `ConditionComponentTypes`: if the component exists it's checked; if its namespace's mod is **not installed** the condition passes (logged once at debug), so packs work without optional mods; if the mod **is** installed (or the namespace is `minecraft`) but the component doesn't exist (typo, unsupported version) the condition **fails** and warns once. An unreadable value warns once per condition type and component, and fails. Failing conditions' descriptions are listed in the nexus info-icon tooltip (`AttunementNexusSlotInformation.unmetAttunementConditions`, built by both the server and client slot-information builders) and in red under the item's attunement tooltip line, only while the viewer could still attune or ascend it (not attuned to someone else, not at max level).
  - `artifactory:component_int_range`: `component` (id), `min` and/or `max` (ints, inclusive, at least one required), `when_missing` (`pass` | `fail`, default `fail`), optional `description` (a text component; default text from `artifactory.condition.component_int_range.{at_least,at_most,between}`). The component id is resolved when checked, so an unregistered component (optional mod absent) passes, logged once at debug. A value counts if it is an `Integer` or encodes with its codec to a bare int (e.g. a single-int record via `xmap`), or to a byte/short tag, or a long tag within int range; anything else logs one warning and fails.
  - `artifactory:component_present`: `component`, `present` (default `true`; `false` means the item must *not* have it), optional `description`. Ignores the value.
  - `artifactory:component_equals`: `component`, `value` (any JSON, read with the component's own codec via `JsonOps` and compared with `equals`; cached per condition), optional `description`. A missing component fails. Values that need registry lookups (holder references) aren't readable this way and fail with a warning.
  - A condition list that doesn't parse (unknown type, missing fields) logs the loader's usual error naming the file and is replaced by the internal `artifactory:unreadable` condition, which always fails, so a broken file can't silently make an item attunable (fail closed).
  - `"attunement_conditions": null` and misspelled keys are treated as absent (no conditions, no log), like every other optional key.
  - Per-stack `ATTUNEMENT_OVERRIDE` schemas have no conditions and are resolved before the datapack, so an overridden stack isn't gated.
  - Several packs configuring the same item follow the normal merge rule (the first file wins unless a later one sets `"replace": true`), so a pack adding conditions to an item another pack already configures must set `"replace": true`. The loader warns when the winning file has no conditions but a losing one did.
  - A schema with conditions and `use_without_attunement: false` logs a warning at load (once per source config, not per tag-expanded item, and not for the unreadable placeholder): nobody can use or attune the item until its conditions pass.
- Item requirement grammar: `modid:item#quantity`; only the first 3 are used (3 GUI slots).
- Omitted/negative xp values fall back to server config defaults (`xpLevelThreshold`, `xpLevelsConsumed` in `artifactory-server.toml`).
- All keys are snake_case exactly as shown — there are no legacy aliases; misspelled keys are silently ignored and fall back to defaults.
- `requirements.kills` and `requirements.feats` are parsed by the codec but **not enforced anywhere** — only XP and items gate attunement today.

## Testing
- **Gametests:** Artifactory ships none. To verify a change, write a temporary `@GameTestHolder(Artifactory.MOD_ID)` class plus `data/artifactory/structure/empty.nbt` (copy from `../quality_api/src/main/resources/data/quality_api/structure/`), run `./gradlew runGameTestServer`, then delete both. Use `FakePlayerFactory.get(...)` rather than `helper.makeMockServerPlayerInLevel()`: the mock player fires login, Artifactory's datapack sync packet then fails on its fake connection, and the mock player is creative (no item damage). A real attunement in a test: `checkAndUpdateAttunementComponents(stack)` then `AttunementService.increaseLevelOfAttunement(fakePlayer, stack)` on an item the default pack configures (e.g. diamond sword, trident).
- **Dev server boot check:** `./gradlew runServer`, wait for `Done (`, then stop it (find the process with `lsof -iTCP:25565 -sTCP:LISTEN`). Kill leftover servers first or the port is taken. Mixin application shows in the log as `Mixing EntityMixin from artifactory.mixins.json`.
- **In-game test pack:** `claude_reference/test_datapack/artifactory_test` (free levels for iron sword with `min_quality: 3`, shield, bow, iron pickaxe, iron chestplate). Copy it into `run/saves/<world>/datapacks/` (and `run/world/datapacks/` for the dev server); a newly created world needs it copied again, or `/reload` + `/datapack enable "file/artifactory_test"`. Handy commands: `/quality set quality_api:exquisite` (main hand), `/give @s minecraft:iron_sword[damage=249]`. QualityAPI rescales damage proportionally when a tier changes, so a nearly broken item stays nearly broken.
- The IDE auto-stages newly created files. Since the user handles git, unstage anything you create (`git restore --staged <path>`), including temporary test files before deleting them.

## Decisions and ideas
- **Recall** (returning an attuned item on demand) was explored and **shelved**: items in chests/unloaded chunks can't be reached without searching or force-loading chunks, and a version-stamp design (give a fresh copy, delete stale ones when seen) reaches everything but lets stale copies be converted by automation and turns them into plain items when a bond breaks unless dead-attunement records are kept forever.
- Possible future modifications discussed: durability via a SilverLib durability-loss event (`durable/<chance>`, live, no enchantments) or a baked `MAX_DAMAGE` bonus; `glint`; `effect/<effect>/<amp>` while held/worn; `on_hit/...`; `lifesteal/...`; enforcing `requirements.kills` (SilverLib's combat ledger can attribute kills); `quality/+1` on ascension.
- Post-2.7.0 cleanup list (deprecated `@EventBusSubscriber(bus = ...)`, unused parameters, comment typos) is in `claude_reference/audit.md`. Check `bus` routing on NeoForge 21.1.228 before removing it.

## Conventions
- **No backwards-compatibility shims by default.** Fix problems permanently and cleanly — do not add legacy aliases, fallback parsers, or migration paths on your own. When a correct fix breaks backwards compatibility (datapack formats, save data, configs, network), state exactly what breaks and ask before choosing the approach; the maintainer decides.
- Package `net.silvertide.artifactory`; registries use the NeoForge `DeferredRegister` pattern (`registry/`).
- Network packets: NeoForge `PayloadRegistrar` (created with `event.registrar(PROTOCOL_VERSION)`) + `StreamCodec`, registered in `network/Networking`; naming `CB_*` (client-bound) / `SB_*` (server-bound). Bump `Networking.PROTOCOL_VERSION` whenever any stream codec changes (packets or synced data components), so mismatched clients get a version screen instead of decode errors.
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
3. **Write to audit.md** Write the findings from the audit to audit.md in the claude_reference folder so we can checkmark them as we complete them.
4. **Project Problems check** by running a whole project search in the problems / project tab. Have the user download and put this file into the claude_reference/problems to check. Ask before deploys if we should do this.
Don't skip step 2 because step 1 looked clean — the value of the independent reviewer is exactly that it doesn't share your blind spots.

## Version Control

**The user handles commits in git.** Never run `git add`, `git commit`, or `git push` — and don't suggest doing so — unless the user explicitly asks. Wrap up work by reporting what changed; staging and pushing are the user's job.
