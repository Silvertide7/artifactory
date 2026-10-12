## 2.7.0

**Requirements**
- Now requires SilverLib 1.2.2 or newer
- Now requires NeoForge 21.1.228 or newer
- QualityAPI support is optional and needs QualityAPI 1.3.2 or newer
- Network protocol changed: 2.6.x clients can't join 2.7.0 servers, and the reverse

**New**
- `durable/<multiplier>` modification: the item wears out that many times slower (`durable/2` lasts twice as long). It stacks with Unbreaking and with QualityAPI durability, and shows as "Durable ×2" in the tooltip
- `resilient` modification: an attuned item that runs out of durability stays in your inventory, broken and unusable, until you repair it instead of vanishing. It shows as "Resilient" in the tooltip
- Void protection for every attuned item: if one falls into the void (dropped, or a thrown trident) it comes back to its owner instead of being destroyed. If the owner is offline or dead, they get it back once they're online and alive
- QualityAPI support: attunement configs can set `min_quality` (a QualityAPI tier level). Items below it, or with no quality, can't be attuned; items that are already attuned keep their attunement whatever happens to their quality
- Attuned items never lose a QualityAPI tier when they break
- You're told when one of your attuned items breaks and is kept broken

**Changes**
- Broken items no longer grant their attunement attribute bonuses
- Soulbound now runs on SilverLib: soulbound items are kept even if a revive mod cancels your death, go back to the slot they came from, and drop at your feet instead of vanishing when your inventory is full. Soulbound curios in cosmetic slots are kept too
- Items held by the old soulbound system from a death before updating are returned on your next login or respawn

## 2.6.5
- Tons of clean up, refactor, and bug fixes.