package com.zhstudio.jerrymod.network;

import com.zhstudio.jerrymod.JerryForm;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;

/** Server -> client: who is Jerry and which form. */
public class JerrySyncPacket {
    @Nullable
    public final UUID jerry;
    public final JerryForm form;

    public JerrySyncPacket(@Nullable UUID jerry, JerryForm form) {
        this.jerry = jerry;
        this.form = form;
    }

    public static void encode(JerrySyncPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.jerry != null);
        if (packet.jerry != null) {
            buf.writeUUID(packet.jerry);
        }
        buf.writeVarInt(packet.form.ordinal());
    }

    public static JerrySyncPacket decode(FriendlyByteBuf buf) {
        UUID jerry = buf.readBoolean() ? buf.readUUID() : null;
        return new JerrySyncPacket(jerry, JerryForm.byId(buf.readVarInt()));
    }

    public static void handle(JerrySyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.zhstudio.jerrymod.client.ClientPacketHandler.handleSync(packet));
        ctx.get().setPacketHandled(true);
    }
}
