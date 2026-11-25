package com.zhstudio.jerrymod.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.zhstudio.jerrymod.JerryMod;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = JerryMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class JerryKeys {
    private static final String CATEGORY = "key.categories.jerrymod";

    public static final KeyMapping DEMON = new KeyMapping("key.jerrymod.demon",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F, CATEGORY);
    public static final KeyMapping SKINWALKER = new KeyMapping("key.jerrymod.skinwalker",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, CATEGORY);

    private JerryKeys() {
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(DEMON);
        event.register(SKINWALKER);
    }
}
