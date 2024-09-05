package net.silvertide.artifactory.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.silvertide.artifactory.Artifactory;

public class PacketHandler {

    private static SimpleChannel INSTANCE;
    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }
    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(Artifactory.MOD_ID, "main"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        // CLIENT BOUND
        net.messageBuilder(CB_UpdateAttunedItem.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(CB_UpdateAttunedItem::new)
                .encoder(CB_UpdateAttunedItem::encode)
                .consumerMainThread(CB_UpdateAttunedItem::handle)
                .add();

        net.messageBuilder(CB_UpdateAttunedItemModifications.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(CB_UpdateAttunedItemModifications::new)
                .encoder(CB_UpdateAttunedItemModifications::encode)
                .consumerMainThread(CB_UpdateAttunedItemModifications::handle)
                .add();

        net.messageBuilder(CB_ResetAttunedItems.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(CB_ResetAttunedItems::new)
                .encoder(CB_ResetAttunedItems::encode)
                .consumerMainThread(CB_ResetAttunedItems::handle)
                .add();

        net.messageBuilder(CB_RemoveAttunedItem.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(CB_RemoveAttunedItem::new)
                .encoder(CB_RemoveAttunedItem::encode)
                .consumerMainThread(CB_RemoveAttunedItem::handle)
                .add();

        // SERVER BOUND
        net.messageBuilder(SB_RemoveAttunedItem.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SB_RemoveAttunedItem::new)
                .encoder(SB_RemoveAttunedItem::encode)
                .consumerMainThread(SB_RemoveAttunedItem::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToClient(ServerPlayer player, MSG message) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToAllPlayers(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }

}
