## 2.8.0
- Network protocol changed: 2.7.x clients can't join 2.8.0 servers, and the reverse
- Add the ability to require data components to exist on the item before it is attunable.
- Add `applies_when` to attunement configs: the attunement only exists while the item's data components match (for example a Simply Swords unique once fully awakened). Before that the item is a plain item with no attunement and no use restriction; once it matches, it becomes attunable and, if required, locked until bonded. Bonded items keep their attunement if they later stop matching
