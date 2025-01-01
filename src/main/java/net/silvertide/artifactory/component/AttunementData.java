package net.silvertide.artifactory.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.silvertide.artifactory.config.codecs.CodecTypes;
import net.silvertide.artifactory.storage.AttunedItem;

public record AttunementData() {
    public static final Codec<AttunedItem> CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                CodecTypes.UUID_CODEC.fieldOf("item_uuid").forGetter(AttunementData::getItemUUID),
                Codec.STRING.fieldOf("resource_location").forGetter(AttunementData::getResourceLocation),
                Codec.STRING.fieldOf("display_name").forGetter(AttunementData::getDisplayName),
                Codec.INT.fieldOf("attunement_level").forGetter(AttunementData::getAttunementLevel),
                Codec.INT.fieldOf("order").forGetter(AttunementData::getOrder)
        ).apply(instance, AttunedItem::new));
    }
}
