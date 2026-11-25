package com.zhstudio.jerrymod.network;

import com.zhstudio.jerrymod.JerryState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: did the player allow voice chat? */
public class VoiceConsentPacket {
    private final boolean allowed;

    public VoiceConsentPacket(boolean allowed) {
        this.allowed = allowed;
    }

    public static void encode(VoiceConsentPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.allowed);
    }

    public static VoiceConsentPacket decode(FriendlyByteBuf buf) {
        return new VoiceConsentPacket(buf.readBoolean());
    }

    public static void handle(VoiceConsentPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            if (packet.allowed) {
                JerryState.VOICE_DENIED.remove(player.getUUID());
            } else {
                JerryState.VOICE_DENIED.add(player.getUUID());
            }
        }
        ctx.get().setPacketHandled(true);
    }
}
