package com.zhstudio.jerrymod.client;

import com.zhstudio.jerrymod.JerryForm;
import com.zhstudio.jerrymod.JerryState;
import com.zhstudio.jerrymod.network.JerrySyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public final class ClientPacketHandler {
    private ClientPacketHandler() {
    }

    public static void handleSync(JerrySyncPacket packet) {
        JerryState.clientJerry = packet.jerry;
        JerryState.clientForm = packet.jerry == null ? JerryForm.NONE : packet.form;
        refreshAll();
    }

    public static void refreshAll() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        for (Player player : mc.level.players()) {
            player.refreshDimensions();
        }
    }
}
