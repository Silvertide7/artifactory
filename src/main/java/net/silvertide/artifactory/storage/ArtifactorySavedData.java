package net.silvertide.artifactory.storage;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.config.codecs.CodecTypes;
import net.silvertide.artifactory.network.CB_UpdateAttunedItemMessage;
import net.silvertide.artifactory.network.PacketHandler;
import net.silvertide.artifactory.util.NetworkUtil;

import java.util.*;

public class ArtifactorySavedData extends SavedData {
    private static final Codec<Map<UUID, Map<UUID, AttunedItem>>> ATTUNED_ITEMS_CODEC =
            Codec.unboundedMap(CodecTypes.UUID_CODEC,
                    Codec.unboundedMap(CodecTypes.UUID_CODEC, AttunedItem.CODEC)
                            .xmap(HashMap::new, HashMap::new));
    private static final String NAME = Artifactory.MOD_ID;

    private Map<UUID, Map<UUID, AttunedItem>> attunedItems = new HashMap<>();

    public int getNumAttunedItems(UUID playerUUID) {
        return getAttunedItems(playerUUID).size();
    }

    public void setAttunedItem(UUID playerUUID, AttunedItem attunedItem) {
        attunedItems.computeIfAbsent(playerUUID, i -> new HashMap<>()).put(attunedItem.itemUUID(), attunedItem);
        this.setDirty();
        ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID);
        if(player != null) PacketHandler.sendToClient(player, new CB_UpdateAttunedItemMessage(attunedItem));
    }

    public Optional<AttunedItem> getAttunedItem(UUID playerUUID, UUID attunedItemId) {
        Map<UUID, AttunedItem> playerAttunedItems = attunedItems.getOrDefault(playerUUID, new HashMap<>());
        return Optional.ofNullable(playerAttunedItems.get(attunedItemId));
    }

    public Optional<List<AttunedItem>> getAttunedItemsAsList(UUID playerUUID) {
        Map<UUID, AttunedItem> playerAttunedItems = attunedItems.getOrDefault(playerUUID, new HashMap<>());
        if(!playerAttunedItems.isEmpty()) {
            List<AttunedItem> items = new ArrayList<>(playerAttunedItems.values());
            return Optional.of(items);
        } else {
            return Optional.empty();
        }
    }

    public Map<UUID, AttunedItem> getAttunedItems(UUID playerUUID) {
        return attunedItems.getOrDefault(playerUUID, new HashMap<>());
    }

    public void setAttunedItems(UUID playerUUID, Map<UUID, AttunedItem> attunedItems) {
        this.attunedItems.put(playerUUID, attunedItems);
        this.setDirty();
        ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID);
        if (player != null) NetworkUtil.updateAllAttunedItems(player, attunedItems);
    }

    public void breakAttunement(UUID playerUUID, UUID attunedItemUUID) {
        if(attunedItems.getOrDefault(playerUUID, new HashMap<>()).remove(attunedItemUUID) != null) {
            this.setDirty();
            // TODO: Add client packet here
        }
    }

    public void breakAllAttunements(UUID playerUUID) {
        if(attunedItems.remove(playerUUID) != null){
            this.setDirty();
            //TODO: Add client packet here
        }
    }

    public ArtifactorySavedData(CompoundTag nbt) {
        attunedItems = new HashMap<>(ATTUNED_ITEMS_CODEC.parse(NbtOps.INSTANCE, nbt.getCompound(ATTUNED_ITEMS_KEY)).result().orElse(new HashMap<>()));
    }

    public ArtifactorySavedData() {}

    private static final String ATTUNED_ITEMS_KEY = "attuned_items";

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.put(ATTUNED_ITEMS_KEY, ATTUNED_ITEMS_CODEC.encodeStart(NbtOps.INSTANCE, attunedItems).result().orElse(new CompoundTag()));
        return nbt;
    }

    public static ArtifactorySavedData get() {
        if (ServerLifecycleHooks.getCurrentServer() != null)
            return ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage().computeIfAbsent(ArtifactorySavedData::new, ArtifactorySavedData::new, NAME);
        else
            return new ArtifactorySavedData();
    }
}
