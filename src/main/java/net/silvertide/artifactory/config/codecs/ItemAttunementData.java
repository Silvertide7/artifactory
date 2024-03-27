package net.silvertide.artifactory.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ItemAttunementData(boolean replace, int attunementSlotsUsed, boolean isUnbreakable, boolean canBeSoulbound, boolean useWithoutAttunement) {
    public static final Codec<ItemAttunementData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(ItemAttunementData::replace),
            Codec.INT.fieldOf("attunement_slots_used").forGetter(ItemAttunementData::attunementSlotsUsed),
            Codec.BOOL.optionalFieldOf("unbreakable", true).forGetter(ItemAttunementData::isUnbreakable),
            Codec.BOOL.optionalFieldOf("can_be_soulbound", true).forGetter(ItemAttunementData::canBeSoulbound),
            Codec.BOOL.optionalFieldOf("use_without_attunement", false).forGetter(ItemAttunementData::useWithoutAttunement))
            .apply(instance, ItemAttunementData::new)
    );

    public String toString() {
        return "replace: " + this.replace() + "\n" +
                "attunement_slots_used: " + this.attunementSlotsUsed() + "\n" +
                "unbreakable: " + this.isUnbreakable() + "\n" +
                "can_be_soulbound: " + this.canBeSoulbound() + "\n" +
                "use_without_attunement: " + this.useWithoutAttunement();
    }
}
