package com.zhstudio.jerrymod.network;

import com.zhstudio.jerrymod.JerryMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class JerryNetwork {
    private static final String VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(JerryMod.MODID, "main"),
            () -> VERSION,
            VERSION::equals,
            VERSION::equals);

    private JerryNetwork() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(FormKeyPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(FormKeyPacket::encode)
                .decoder(FormKeyPacket::decode)
                .consumerMainThread(FormKeyPacket::handle)
                .add();
        CHANNEL.messageBuilder(VoiceConsentPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(VoiceConsentPacket::encode)
                .decoder(VoiceConsentPacket::decode)
                .consumerMainThread(VoiceConsentPacket::handle)
                .add();
        CHANNEL.messageBuilder(JerrySyncPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(JerrySyncPacket::encode)
                .decoder(JerrySyncPacket::decode)
                .consumerMainThread(JerrySyncPacket::handle)
                .add();
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void sendToAll(Object packet) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendTo(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
