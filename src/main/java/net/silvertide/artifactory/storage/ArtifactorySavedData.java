package net.silvertide.artifactory.storage;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.config.codecs.CodecTypes;
import net.silvertide.artifactory.network.client_packets.CB_RemoveAttunedItem;
import net.silvertide.artifactory.network.client_packets.CB_ResetAttunedItems;
import net.silvertide.artifactory.network.client_packets.CB_UpdateAttunedItem;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.util.NetworkUtil;
import net.silvertide.artifactory.util.PlayerMessenger;
import net.silvertide.artifactory.util.StackNBTUtil;

import java.util.*;

public class ArtifactorySavedData extends SavedData {
    private static final Codec<Map<UUID, Map<UUID, AttunedItem>>> ATTUNED_ITEMS_CODEC =
            Codec.unboundedMap(CodecTypes.UUID_CODEC,
                    Codec.unboundedMap(CodecTypes.UUID_CODEC, AttunedItem.CODEC)
                            .xmap(HashMap::new, HashMap::new));

    private static final Codec<Map<UUID, String>> ATTUNED_PLAYERS_CODEC =
            Codec.unboundedMap(CodecTypes.UUID_CODEC, Codec.STRING);
    private static final String NAME = Artifactory.MOD_ID;

    private Map<UUID, Map<UUID, AttunedItem>> attunedItems = new HashMap<>();
    private Map<UUID, String> attunedPlayers = new HashMap<>();

    public Map<UUID, Map<UUID, AttunedItem>> getAttunedItemsMap() {
        return this.attunedItems;
    }

    public Optional<String> getPlayerName(UUID playerUUID) {
        return Optional.ofNullable(attunedPlayers.get(playerUUID));
    }

    public void setPlayerName(UUID playerUUID, String playerName) {
        attunedPlayers.put(playerUUID, playerName);
    }

    public int getNumAttunedItems(UUID playerUUID) {
        return getAttunedItems(playerUUID).size();
    }

    public Optional<AttunedItem> getAttunedItem(UUID playerUUID, UUID attunedItemId) {
        Map<UUID, AttunedItem> playerAttunedItems = attunedItems.getOrDefault(playerUUID, new HashMap<>());
        return Optional.ofNullable(playerAttunedItems.get(attunedItemId));
    }

    // Returns true if the items level was increased successfully, and false if not.
    public boolean increaseLevelOfAttunedItem(UUID playerUUID, UUID attunedItemId) {
        Map<UUID, AttunedItem> playerAttunedItems = attunedItems.getOrDefault(playerUUID, new HashMap<>());
        AttunedItem attunedItem = playerAttunedItems.get(attunedItemId);
        if(attunedItem != null) {
            attunedItem.incremenetAttunementLevel();
            this.setDirty();

            // Sync updated information to the player.
            MinecraftServer minecraftServer = ServerLifecycleHooks.getCurrentServer();
            if(minecraftServer != null && !minecraftServer.getPlayerList().getPlayers().isEmpty()) {
                ServerPlayer player = minecraftServer.getPlayerList().getPlayer(playerUUID);
                if(player != null){
                    PacketDistributor.sendToPlayer(player, new CB_UpdateAttunedItem(attunedItem));
                    NetworkUtil.updateAttunedItemModificationDescription(player, attunedItem);
                }
            }
            return true;
        }
        return false;
    }

    public Map<UUID, AttunedItem> getAttunedItems(UUID playerUUID) {
        return attunedItems.getOrDefault(playerUUID, new HashMap<>());
    }

    public void setAttunedItem(ServerPlayer serverPlayer, AttunedItem attunedItem) {
        attunedItems.computeIfAbsent(serverPlayer.getUUID(), i -> new HashMap<>()).put(attunedItem.getItemUUID(), attunedItem);
        setPlayerName(attunedItem.getItemUUID(), serverPlayer.getDisplayName().toString());
        this.setDirty();
        PacketDistributor.sendToPlayer(serverPlayer, new CB_UpdateAttunedItem(attunedItem));
        NetworkUtil.updateAttunedItemModificationDescription(serverPlayer, attunedItem);
    }

    public void removeAttunedItem(UUID playerUUID, UUID attunedItemUUID) {
        AttunedItem removedItem = attunedItems.getOrDefault(playerUUID, new HashMap<>()).remove(attunedItemUUID);
        if(removedItem != null) {
            this.setDirty();
            ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID);
            if(player != null) {
                PacketDistributor.sendToPlayer(player, new CB_RemoveAttunedItem(removedItem.getItemUUID()));
                PlayerMessenger.displayTranslatabelClientMessage(player, "playermessage.artifactory.bond_broken", removedItem.getDisplayName());
            }
        }
    }

    public void removeAllAttunedItems(UUID playerUUID) {
        if(attunedItems.remove(playerUUID) != null) {
            this.setDirty();
            ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID);
            if (player != null) {
                PacketDistributor.sendToPlayer(player, new CB_ResetAttunedItems());
            }
        }
    }

    public ArtifactorySavedData() {}

    public ArtifactorySavedData(CompoundTag nbt, HolderLookup.Provider provider) {
        attunedItems = new HashMap<>(ATTUNED_ITEMS_CODEC.parse(NbtOps.INSTANCE, nbt.getCompound(ATTUNED_ITEMS_KEY)).result().orElse(new HashMap<>()));
    }

    public static Factory<ArtifactorySavedData> dataFactory() {
        return new SavedData.Factory<>(ArtifactorySavedData::new, ArtifactorySavedData::new, null);
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
        nbt.put(ATTUNED_ITEMS_KEY, ATTUNED_ITEMS_CODEC.encodeStart(NbtOps.INSTANCE, attunedItems).result().orElse(new CompoundTag()));
        nbt.put(ATTUNED_PLAYERS_KEY, ATTUNED_PLAYERS_CODEC.encodeStart(NbtOps.INSTANCE, attunedPlayers).result().orElse(new CompoundTag()));
        return nbt;
    }

    private static final String ATTUNED_ITEMS_KEY = "attuned_items";
    private static final String ATTUNED_PLAYERS_KEY = "attuned_players";

    public static ArtifactorySavedData get() {
        if (ServerLifecycleHooks.getCurrentServer() != null)
            return ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage().computeIfAbsent(dataFactory(), NAME);
        else
            return new ArtifactorySavedData();
    }

    public void updateDisplayName(ItemStack stack) {
        StackNBTUtil.getAttunedToUUID(stack).ifPresent(attunedToUUID -> {
            StackNBTUtil.getItemAttunementUUID(stack).flatMap(itemUUID -> this.getAttunedItem(attunedToUUID, itemUUID)).ifPresent(attunedItem -> {
                String displayName = AttunementUtil.getAttunedItemDisplayName(stack);
                if (!attunedItem.getDisplayName().equals(displayName)) {
                    attunedItem.setDisplayName(displayName);
                    this.setDirty();

                    ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(attunedToUUID);
                    if (player != null) {
                        PacketDistributor.sendToPlayer(player, new CB_UpdateAttunedItem(attunedItem));
                    }
                }
            });
        });
    }

    public void updatePlayerDisplayName(ServerPlayer serverPlayer) {
        if(attunedPlayers.containsKey(serverPlayer.getUUID())) {
            if(attunedPlayers.get(serverPlayer.getUUID()).equals(serverPlayer.getDisplayName().getString()));
        } else {
            setPlayerName(serverPlayer.getUUID(), serverPlayer.getDisplayName().getString());
            this.setDirty();
        }
    }
}
