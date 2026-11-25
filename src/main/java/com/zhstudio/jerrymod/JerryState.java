package com.zhstudio.jerrymod;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Answers "is this player Jerry, and in which form?" on both client and server. */
public final class JerryState {
    // Client copy (sent by the server)
    public static volatile UUID clientJerry;
    public static volatile JerryForm clientForm = JerryForm.NONE;

    // Server copy (mirrors JerryData; also read by the voice chat thread)
    public static volatile UUID serverJerry;
    public static volatile JerryForm serverForm = JerryForm.NONE;

    /** Players who said NO to voice chat. Their microphone is muted (needs Simple Voice Chat). */
    public static final Set<UUID> VOICE_DENIED = ConcurrentHashMap.newKeySet();

    private JerryState() {
    }

    public static JerryForm getForm(Player player) {
        if (player.level() == null) {
            return JerryForm.NONE;
        }
        if (player.level().isClientSide) {
            UUID jerry = clientJerry;
            return jerry != null && jerry.equals(player.getUUID()) ? clientForm : JerryForm.NONE;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return JerryForm.NONE;
        }
        JerryData data = JerryData.get(server);
        UUID jerry = data.getJerry();
        return jerry != null && jerry.equals(player.getUUID()) ? data.getForm() : JerryForm.NONE;
    }

    public static boolean isJerry(Player player) {
        return getForm(player) != JerryForm.NONE;
    }
}
