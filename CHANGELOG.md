## 2.7.0
- Requires SilverLib 1.2.2; QualityAPI integration requires QualityAPI 1.3.2
- Network protocol bumped: 2.6.x clients can't join 2.7.0 servers (and the reverse)
- Add `resilient` modification: an attuned item at zero durability stays broken until repaired instead of vanishing
- Add optional `min_quality` to attunement configs (QualityAPI tier level); lower-quality or quality-less items can't be attuned, but items already attuned keep their attunement
- Attuned items no longer lose a QualityAPI tier when they break
- Broken items no longer grant attunement attribute bonuses
- Soulbound now runs on SilverLib: items survive revive mods cancelling the death, return to their original slot, and drop at your feet instead of vanishing when the inventory is full; soulbound curios in cosmetic slots are kept too
- Items kept by the old soulbound system are returned on login or respawn

## 2.6.5
- Tons of clean up, refactor, and bug fixes.