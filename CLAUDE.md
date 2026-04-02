# Artifactory - Minecraft NeoForge Mod

## Overview
Artifactory is a Minecraft mod for NeoForge (1.21.1) that adds an item attunement system. Players can bond to items using an Attunement Nexus block to gain powerful benefits. Items go through discovery, attunement, and leveling stages. The mod supports Curios API integration for additional equipment slots.

## Build & Run
- **Build**: `./gradlew build`
- **Run client**: `./gradlew runClient`
- **Run server**: `./gradlew runServer`
- **Run data generators**: `./gradlew runData`
- **Run tests**: `./gradlew test` (JUnit 5)
- **Publish**: `./gradlew publishMods` (requires `CF_TOKEN` env var for CurseForge)

Java 21 is required. Gradle configuration caching is enabled.

## Project Structure
```
src/main/java/net/silvertide/artifactory/
  Artifactory.java          # Main mod class, mod ID = "artifactory"
  blocks/                   # Block implementations (AttunementNexusBlock)
  client/                   # Client-side code (events, keybindings, state, utils)
  commands/                 # Chat commands (admin, general)
  compat/                   # Mod compatibility (Curios API)
  component/                # Data components (AttunementFlag, AttunementLevel, PlayerAttunementData, etc.)
  config/                   # Server configs (TOML) and codec definitions
    codecs/                 # Data-driven attunement definitions (AttunableItems, AttunementDataSource)
  datagen/                  # Data generators (recipes, loot tables, block tags)
  events/                   # Server event handlers (ArtifactEvents, SoulboundEvents, SystemEvents)
  gui/                      # Menu and screen classes (AttunementMenu, AttunementScreen, ManageAttunementsScreen)
  modifications/            # Attunement modification system (attributes, effects)
  network/                  # Client/server packet definitions (CB_ = client-bound, SB_ = server-bound)
  registry/                 # NeoForge deferred registries (blocks, items, attributes, menus, data components)
  services/                 # Core business logic (AttunementService, ModificationService, PlayerMessenger)
  setup/                    # Mod setup (ClientSetup, CuriosSetup)
  storage/                  # Persistent data (ArtifactorySavedData, AttunedItem)
  util/                     # Utility classes
```

## Key Architecture
- **Data-driven attunement**: Item attunement configs are defined via JSON data packs under `data/<namespace>/artifactory/`. A built-in default data pack ships with standard Minecraft items.
- **Data components**: Custom NeoForge data components on ItemStacks track attunement state (`PLAYER_ATTUNEMENT_DATA`, `ATTUNEMENT_FLAG`, `ATTUNEMENT_OVERRIDE`).
- **SavedData**: `ArtifactorySavedData` persists player-to-item attunement mappings at the world level (keyed by player UUID -> item UUID -> AttunedItem).
- **Client/server sync**: Custom network packets sync attunement data, datapack info, and configs to the client. Packet classes follow naming: `CB_*` (client-bound) and `SB_*` (server-bound).
- **Curios compatibility**: Optional integration behind `CompatFlags.CURIOS_LOADED` runtime check. Curios is a compile-only dependency.

## Conventions
- Package: `net.silvertide.artifactory`
- Registries use NeoForge `DeferredRegister` pattern
- Server configs use NeoForge `ModConfigSpec` (TOML)
- Network packets use NeoForge `PayloadRegistrar` with `StreamCodec`
- Generated resources go in `src/generated/resources/` (committed to repo)
- Mod metadata template is in `src/main/templates/`
- Version is managed in `gradle.properties` (`mod_version`)
