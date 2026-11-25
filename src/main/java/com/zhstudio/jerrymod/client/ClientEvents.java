package com.zhstudio.jerrymod.client;

import com.zhstudio.jerrymod.JerryForm;
import com.zhstudio.jerrymod.JerryMod;
import com.zhstudio.jerrymod.JerryState;
import com.zhstudio.jerrymod.network.FormKeyPacket;
import com.zhstudio.jerrymod.network.JerryNetwork;
import com.zhstudio.jerrymod.network.VoiceConsentPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = JerryMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientEvents {
    private static boolean justJoined;
    private static int joinDelay;

    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        justJoined = true;
        joinDelay = 40; // wait 2 seconds after joining
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        JerryState.clientJerry = null;
        JerryState.clientForm = JerryForm.NONE;
        justJoined = false;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            return;
        }

        // 1) Before anything else: ask for voice chat permission
        if (justJoined) {
            if (joinDelay > 0) {
                joinDelay--;
            } else if (mc.screen == null) {
                justJoined = false;
                Boolean answer = VoicePermission.get();
                if (answer == null) {
                    mc.setScreen(new VoicePermissionScreen());
                } else {
                    JerryNetwork.sendToServer(new VoiceConsentPacket(answer));
                }
            }
        }

    }

    private static boolean demonWasDown;
    private static boolean skinWasDown;

    /** START phase runs before Minecraft handles its own keys, so we can stop F from swapping hands for Jerry. */
    @SubscribeEvent
    public static void onClientTickStart(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            return;
        }
        boolean jerry = JerryState.isJerry(mc.player);
        // Read the keys directly (F is also "swap hands" in Minecraft, so normal key events can clash)
        boolean demonDown = mc.screen == null && isDown(mc, JerryKeys.DEMON);
        boolean skinDown = mc.screen == null && isDown(mc, JerryKeys.SKINWALKER);
        if (jerry && demonDown && !demonWasDown) {
            JerryNetwork.sendToServer(new FormKeyPacket(JerryForm.DEMON));
        }
        if (jerry && skinDown && !skinWasDown) {
            JerryNetwork.sendToServer(new FormKeyPacket(JerryForm.SKINWALKER));
        }
        demonWasDown = demonDown;
        skinWasDown = skinDown;

        while (JerryKeys.DEMON.consumeClick()) {
            // handled above
        }
        while (JerryKeys.SKINWALKER.consumeClick()) {
            // handled above
        }
        if (jerry && (sameKey(mc.options.keySwapOffhand, JerryKeys.DEMON) || sameKey(mc.options.keySwapOffhand, JerryKeys.SKINWALKER))) {
            while (mc.options.keySwapOffhand.consumeClick()) {
                // Jerry doesn't swap hands with F
            }
        }
    }

    private static boolean isDown(Minecraft mc, KeyMapping key) {
        InputConstants.Key k = key.getKey();
        if (k.getType() != InputConstants.Type.KEYSYM || k.getValue() == InputConstants.UNKNOWN.getValue()) {
            return key.isDown();
        }
        return InputConstants.isKeyDown(mc.getWindow().getWindow(), k.getValue());
    }

    private static boolean sameKey(KeyMapping a, KeyMapping b) {
        return a.getKey().equals(b.getKey());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        JerryForm form = JerryState.getForm(event.getEntity());
        if (form == JerryForm.NONE) {
            return;
        }
        event.setCanceled(true);
        JerryRenderer.render(event.getEntity(), form, event.getPartialTick(), event.getPoseStack(),
                event.getMultiBufferSource(), event.getPackedLight());
    }

    /** Jerry has paws, not human hands. */
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && JerryState.isJerry(mc.player)) {
            event.setCanceled(true);
        }
    }
}
