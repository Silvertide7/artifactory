package net.silvertide.artifactory.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.UUID;

public interface IAttunementSlot extends INBTSerializable<CompoundTag> {
    int getNumAttunedArtifacts();
    int getNumAttunementSlots();
    boolean canAttuneNewArtifact();
    boolean addArtifact(UUID artifactUUID, String artifactDesc);
}
