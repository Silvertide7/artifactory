package net.silvertide.artifactory.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.component.AttunementOverride;
import net.silvertide.artifactory.config.codecs.CodecTypes;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.util.DataComponentUtil;
import net.silvertide.artifactory.util.ResourceLocationUtil;

import java.util.Optional;
import java.util.UUID;

public class AttunedItem {
    private AttunementOverride attunementOverride;
    private UUID itemUUID;
    private String resourceLocation;
    private String displayName;
    private int attunementLevel;
    private int order;

    public AttunedItem(AttunementOverride attunementOverride, UUID itemUUID, String resourceLocation, String displayName, int attunementLevel, int order) {
        this.attunementOverride = attunementOverride;
        this.itemUUID = itemUUID;
        this.resourceLocation = resourceLocation;
        this.displayName = displayName;
        this.attunementLevel = attunementLevel;
        this.order = order;
    }

    public AttunementOverride getAttunementOverride() {
        return attunementOverride;
    }

    public Optional<AttunementOverride> getAttunementOverrideOpt() {
        return Optional.ofNullable(this.attunementOverride);
    }

    public void setAttunementOverride(AttunementOverride attunementOverride) {
        this.attunementOverride = attunementOverride;
    }

    public UUID getItemUUID() {
        return itemUUID;
    }

    public void setItemUUID(UUID itemUUID) {
        this.itemUUID = itemUUID;
    }

    public String getResourceLocation() {
        return resourceLocation;
    }

    public void setResourceLocation(String resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getAttunementLevel() {
        return attunementLevel;
    }

    public void setAttunementLevel(int attunementLevel) {
        this.attunementLevel = attunementLevel;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public static final Codec<AttunedItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AttunementOverride.CODEC.optionalFieldOf("attunement_override", null).forGetter(AttunedItem::getAttunementOverride),
            CodecTypes.UUID_CODEC.fieldOf("item_uuid").forGetter(AttunedItem::getItemUUID),
            Codec.STRING.fieldOf("resource_location").forGetter(AttunedItem::getResourceLocation),
            Codec.STRING.fieldOf("display_name").forGetter(AttunedItem::getDisplayName),
            Codec.INT.fieldOf("attunement_level").forGetter(AttunedItem::getAttunementLevel),
            Codec.INT.fieldOf("order").forGetter(AttunedItem::getOrder)
    ).apply(instance, AttunedItem::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AttunedItem> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public AttunedItem decode(RegistryFriendlyByteBuf buf) {
            AttunementOverride attunementOverride = null;
            if(buf.readBoolean()) {
                attunementOverride = AttunementOverride.STREAM_CODEC.decode(buf);
            }
            return new AttunedItem(attunementOverride, buf.readUUID(), buf.readUtf(), buf.readUtf(), buf.readInt(), buf.readInt());
        }
        @Override
        public void encode(RegistryFriendlyByteBuf buf, AttunedItem attunedItem) {
            AttunementOverride attunementOverride = attunedItem.getAttunementOverride();
            buf.writeBoolean(attunementOverride != null);
            if(attunementOverride != null) {
                AttunementOverride.STREAM_CODEC.encode(buf, attunementOverride);
            }

            buf.writeUUID(attunedItem.getItemUUID());
            buf.writeUtf(attunedItem.getResourceLocation());
            buf.writeUtf(attunedItem.getDisplayName());
            buf.writeInt(attunedItem.getAttunementLevel());
            buf.writeInt(attunedItem.getOrder());
        }
    };

    public void incremenetAttunementLevel() {
        setAttunementLevel(getAttunementLevel() + 1);
    }

    public static AttunedItem buildAttunedItem(Player player, ItemStack stack) {
        ResourceLocation resourceLocation = ResourceLocationUtil.getResourceLocation(stack);
        int numAttunedItems = ArtifactorySavedData.get().getNumAttunedItems(player.getUUID());
        String itemDisplayName = AttunementUtil.getAttunedItemDisplayName(stack);

        AttunementOverride attunementOverride = DataComponentUtil.getAttunementOverride(stack).orElse(null);
        return new AttunedItem(attunementOverride, UUID.randomUUID(), resourceLocation.toString(), itemDisplayName, 1, numAttunedItems + 1);

    }
}
