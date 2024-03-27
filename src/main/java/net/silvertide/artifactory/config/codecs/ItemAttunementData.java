package net.silvertide.artifactory.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ItemAttunementData(boolean replace, int slotsUsed, boolean hasDurability, boolean canBeSoulbound) {
    public static final Codec<ItemAttunementData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(ItemAttunementData::replace),
            Codec.INT.fieldOf("slots_used").forGetter(ItemAttunementData::slotsUsed),
            Codec.BOOL.optionalFieldOf("has_durability", false).forGetter(ItemAttunementData::hasDurability),
            Codec.BOOL.optionalFieldOf("can_be_soulbound", true).forGetter(ItemAttunementData::canBeSoulbound))
            .apply(instance, ItemAttunementData::new)
    );

    public String toString() {
        return "replace: " + this.replace() + "\n" +
                "slots_used: " + this.slotsUsed() + "\n" +
                "has_durability: " + this.hasDurability() + "\n" +
                "can_be_soulbound: " + this.canBeSoulbound() + "\n" +
                "\n";
    }
}
