package net.silvertide.artifactory.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface AttunedItems extends INBTSerializable<CompoundTag> {
    int getNumAttunedItems();
    void attuneItem(AttunedItem itemToAttune);
}
