package com.zhstudio.jerrymod.network;

import com.zhstudio.jerrymod.JerryForm;
import com.zhstudio.jerrymod.JerryLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: Jerry pressed F (demon) or J (skinwalker). */
public class FormKeyPacket {
    private final JerryForm wanted;

    public FormKeyPacket(JerryForm wanted) {
        this.wanted = wanted;
    }

    public static void encode(FormKeyPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.wanted.ordinal());
    }

    public static FormKeyPacket decode(FriendlyByteBuf buf) {
        return new FormKeyPacket(JerryForm.byId(buf.readVarInt()));
    }

    public static void handle(FormKeyPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            JerryLogic.toggleForm(player, packet.wanted);
        }
        ctx.get().setPacketHandled(true);
    }
}
