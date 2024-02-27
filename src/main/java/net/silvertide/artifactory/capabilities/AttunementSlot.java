package net.silvertide.artifactory.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AttunementSlot implements IAttunementSlot {
    //TODO: Make the num attunement slots an attribute
    private int numAttunementSlots = 3;
    private Map<UUID, String> attunedArtifacts = new HashMap<>();
    @Override
    public int getNumAttunedArtifacts() {
        return attunedArtifacts.size();
    }
    @Override
    public int getNumAttunementSlots() {
        return this.numAttunementSlots;
    }

    @Override
    public boolean canAttuneNewArtifact() {
        return getNumAttunedArtifacts() < getNumAttunementSlots();
    }

    private void setAttunedArtifacts(Map<UUID, String> attunedArtifacts) {
        this.attunedArtifacts = attunedArtifacts;
    }

    @Override
    public boolean addArtifact(UUID artifactUUID, String artifactDesc) {
        if(this.attunedArtifacts == null){
            this.attunedArtifacts = new HashMap<>();
        }
        if(canAttuneNewArtifact()){
            attunedArtifacts.put(artifactUUID, artifactDesc);
            return true;
        }
        return false;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();

        nbt.putInt("numAttunementSlots", getNumAttunementSlots());

        if(!this.attunedArtifacts.isEmpty()) {
            int artifactIncrement = 0;
            for(Map.Entry<UUID, String> artifact : attunedArtifacts.entrySet()){
                String artifactID = "artifact_" + artifactIncrement;
                nbt.putString(artifactID+"_uuid", artifact.getKey().toString());
                nbt.putString(artifactID+"_description", artifact.getKey().toString());
                artifactIncrement++;
            }
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        int numAttunedArtifacts = nbt.getInt("numAttunementSlots");

        if (numAttunedArtifacts > 0) {
            Map<UUID, String> attunedArtifacts = new HashMap<>();
            for(int i = 0; i < numAttunedArtifacts; i++){
                String artifactID = "artifact_" + i;
                String artifactUUID = nbt.getString(artifactID+"_uuid");
                String artifactDesc = nbt.getString(artifactID+"_description");
                if(StringUtils.isNotEmpty(artifactUUID) && StringUtils.isNotEmpty(artifactDesc)){
                    attunedArtifacts.put(UUID.fromString(artifactUUID), artifactDesc);
                }
            }
            setAttunedArtifacts(attunedArtifacts);
        }
    }
}
