package net.silvertide.artifactory.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.config.codecs.CodecTypes;
import net.silvertide.artifactory.util.StackNBTUtil;
import net.silvertide.artifactory.util.ResourceLocationUtil;

import java.util.Optional;
import java.util.UUID;

public class AttunedItem {
    private UUID itemUUID;
    private String resourceLocation;
    private String displayName;
    private int attunementLevel;
    private int order;

    public AttunedItem(UUID itemUUID, String resourceLocation, String displayName, int attunementLevel, int order) {
        this.itemUUID = itemUUID;
        this.resourceLocation = resourceLocation;
        this.displayName = displayName;
        this.attunementLevel = attunementLevel;
        this.order = order;
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
            CodecTypes.UUID_CODEC.fieldOf("item_uuid").forGetter(AttunedItem::getItemUUID),
            Codec.STRING.fieldOf("resource_location").forGetter(AttunedItem::getResourceLocation),
            Codec.STRING.fieldOf("display_name").forGetter(AttunedItem::getDisplayName),
            Codec.INT.fieldOf("attunement_level").forGetter(AttunedItem::getAttunementLevel),
            Codec.INT.fieldOf("order").forGetter(AttunedItem::getOrder)
    ).apply(instance, AttunedItem::new));


    public void incremenetAttunementLevel() {
        setAttunementLevel(getAttunementLevel() + 1);
    }

    public static Optional<AttunedItem> buildAttunedItem(Player player, ItemStack stack) {
        return StackNBTUtil.getItemAttunementUUID(stack).flatMap(itemUUID -> {
            ResourceLocation resourceLocation = ResourceLocationUtil.getResourceLocation(stack);
            int numAttunedItems = ArtifactorySavedData.get().getNumAttunedItems(player.getUUID());
            String itemDisplayName = getAttunedItemName(stack);
            return Optional.of(new AttunedItem(itemUUID, resourceLocation.toString(), itemDisplayName, 1, numAttunedItems + 1));
        });
    }

    private static String getAttunedItemName(ItemStack stack) {
        return StackNBTUtil.getDisplayNameFromNBT(stack).orElse(stack.getItem().toString());
    }

    public static void encode(FriendlyByteBuf buf, AttunedItem attunedItem) {
        buf.writeUUID(attunedItem.getItemUUID());
        buf.writeUtf(attunedItem.getResourceLocation());
        buf.writeUtf(attunedItem.getDisplayName());
        buf.writeInt(attunedItem.getAttunementLevel());
        buf.writeInt(attunedItem.getOrder());
    }

    public static AttunedItem decode(FriendlyByteBuf buf) {
        UUID itemUUID = buf.readUUID();
        String resourceLocation = buf.readUtf();
        String displayName = buf.readUtf();
        int attunementLevel = buf.readInt();
        int order = buf.readInt();
        return new AttunedItem(itemUUID, resourceLocation, displayName, attunementLevel, order);
    }
}
