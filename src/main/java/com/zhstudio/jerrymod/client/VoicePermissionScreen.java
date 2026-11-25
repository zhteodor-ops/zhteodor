package com.zhstudio.jerrymod.client;

import com.zhstudio.jerrymod.network.JerryNetwork;
import com.zhstudio.jerrymod.network.VoiceConsentPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;

/** Shown when you join a world for the first time: asks for voice chat permission. */
public class VoicePermissionScreen extends Screen {

    public VoicePermissionScreen() {
        super(Component.literal("Jerry Mod - Voice Chat"));
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int y = height / 2 + 30;
        addRenderableWidget(Button.builder(Component.literal("Allow voice chat").withStyle(ChatFormatting.GREEN), b -> answer(true))
                .bounds(cx - 155, y, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Don't allow").withStyle(ChatFormatting.RED), b -> answer(false))
                .bounds(cx + 5, y, 150, 20).build());
    }

    private void answer(boolean allowed) {
        VoicePermission.set(allowed);
        JerryNetwork.sendToServer(new VoiceConsentPacket(allowed));
        onClose();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int cx = width / 2;
        int y = height / 2 - 60;
        g.drawCenteredString(font, Component.literal("Something is wrong with the dogs...").withStyle(ChatFormatting.DARK_RED), cx, y, 0xFFFFFF);
        g.drawCenteredString(font, title, cx, y + 16, 0xFFFFFF);
        g.drawCenteredString(font, Component.literal("This mod wants to use your microphone for proximity voice chat."), cx, y + 36, 0xCCCCCC);
        g.drawCenteredString(font, Component.literal("When you are demonic or skinwalker Jerry, your voice gets twisted."), cx, y + 48, 0xCCCCCC);
        boolean svc = ModList.get().isLoaded("voicechat");
        g.drawCenteredString(font, Component.literal(svc
                        ? "Simple Voice Chat is installed."
                        : "Voice needs the Simple Voice Chat mod (not installed yet)."),
                cx, y + 66, svc ? 0x55FF55 : 0xFFAA00);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
