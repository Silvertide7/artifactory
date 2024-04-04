package net.silvertide.artifactory.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.config.codecs.CodecTypes;
import net.silvertide.artifactory.util.StackNBTUtil;
import net.silvertide.artifactory.util.ResourceLocationUtil;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record AttunedItem(UUID itemUUID, String resourceLocation, int attunementLevel, int order) {
    public static final Codec<AttunedItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CodecTypes.UUID_CODEC.fieldOf("item_uuid").forGetter(AttunedItem::itemUUID),
            Codec.STRING.fieldOf("resource_location").forGetter(AttunedItem::resourceLocation),
            Codec.INT.fieldOf("attunement_level").forGetter(AttunedItem::attunementLevel),
            Codec.INT.fieldOf("order").forGetter(AttunedItem::attunementLevel)
    ).apply(instance, AttunedItem::new));

    public static Optional<AttunedItem> buildAttunedItem(Player player, ItemStack stack) {
        return StackNBTUtil.getItemAttunementUUID(stack).flatMap(itemUUID -> {
            ResourceLocation resourceLocation = ResourceLocationUtil.getResourceLocation(stack);
            int numAttunedItems = ArtifactorySavedData.get().getAttunedItems(player.getUUID()).size();
            return Optional.of(new AttunedItem(itemUUID, resourceLocation.toString(), 1, numAttunedItems + 1));
        });
    }

    public static void encode(FriendlyByteBuf buf, AttunedItem attunedItem) {
        buf.writeUUID(attunedItem.itemUUID());
        buf.writeUtf(attunedItem.resourceLocation());
        buf.writeInt(attunedItem.attunementLevel());
        buf.writeInt(attunedItem.order());
    }

    public static AttunedItem decode(FriendlyByteBuf buf) {
        UUID itemUUID = buf.readUUID();
        String resourceLocation = buf.readUtf();
        int attunementLevel = buf.readInt();
        int order = buf.readInt();
        return new AttunedItem(itemUUID, resourceLocation, attunementLevel, order);
    }
}
