## 2.6.4
- Actually make the default datapack removable.

## 2.6.3
- The default Artifactory datapack can now be removed from a world during world creation.

## 2.6.2
- The datapack is now enabled by default. This follows standard practice. It can still be disabled / overwritten.
- The default recipe for the anvil changed a little bit to be easier to craft. It still requires a beacon, but everything else should be easy to find.
- Fixes and cleanup

## 2.6.1
- Remove unused dependencies.

## 2.6.0
- Fix bug when loading into the game with an attunable curios item in a slot that is not attuned.
- Small performance improvements and fixes.

## 2.5.1
- Fix lang file text

## 2.5.0
- Changed the attunement screen attunement slots UI
- Set the base attribute to 20
- Small UI fixes.

## 2.4.0
- Added configs to allow players to change the base attribute value. You have to turn on the updateAttributes config so it starts checking. If the base value in game differs from the config then it will set the base value.
- Updated the text for the show attunement percentage config.

## 2.3.0
- Added tag support to apply_to_items json node. You can now include tags in the list and it will apply the attunement configuration to all items with that tag.