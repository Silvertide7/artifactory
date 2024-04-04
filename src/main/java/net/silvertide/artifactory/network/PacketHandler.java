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

        net.messageBuilder(CB_UpdateAttunedItemMessage.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(CB_UpdateAttunedItemMessage::new)
                .encoder(CB_UpdateAttunedItemMessage::encode)
                .consumerMainThread(CB_UpdateAttunedItemMessage::handle)
                .add();

        net.messageBuilder(CB_ResetAttunedItemsMessage.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(CB_ResetAttunedItemsMessage::new)
                .encoder(CB_ResetAttunedItemsMessage::encode)
                .consumerMainThread(CB_ResetAttunedItemsMessage::handle)
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
