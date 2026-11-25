package com.zhstudio.jerrymod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zhstudio.jerrymod.JerryForm;
import com.zhstudio.jerrymod.JerryMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/** Draws Jerry instead of the normal player model. */
public final class JerryRenderer {
    private static final ResourceLocation DOG_TEX = tex("jerry_dog");
    private static final ResourceLocation DEMON_TEX = tex("jerry_demon");
    private static final ResourceLocation DEMON_EYES = tex("jerry_demon_eyes");
    private static final ResourceLocation SKIN_TEX = tex("jerry_skinwalker");
    private static final ResourceLocation SKIN_EYES = tex("jerry_skinwalker_eyes");

    private static JerryDogModel dogModel;
    private static DemonJerryModel demonModel;
    private static SkinwalkerJerryModel skinModel;

    private JerryRenderer() {
    }

    private static ResourceLocation tex(String name) {
        return new ResourceLocation(JerryMod.MODID, "textures/entity/" + name + ".png");
    }

    public static void render(Player player, JerryForm form, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        if (player.isInvisible()) {
            return;
        }
        EntityModel<Player> model;
        ResourceLocation texture;
        ResourceLocation eyes = null;
        switch (form) {
            case DEMON -> {
                if (demonModel == null) demonModel = new DemonJerryModel();
                model = demonModel;
                texture = DEMON_TEX;
                eyes = DEMON_EYES;
            }
            case SKINWALKER -> {
                if (skinModel == null) skinModel = new SkinwalkerJerryModel();
                model = skinModel;
                texture = SKIN_TEX;
                eyes = SKIN_EYES;
            }
            default -> {
                if (dogModel == null) dogModel = new JerryDogModel();
                model = dogModel;
                texture = DOG_TEX;
            }
        }

        float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
        float headYaw = Mth.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot);
        float netHeadYaw = Mth.wrapDegrees(headYaw - bodyYaw);
        float headPitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());
        float limbSwing = player.walkAnimation.position(partialTick);
        float limbAmount = Math.min(player.walkAnimation.speed(partialTick), 1.0f);
        float age = player.tickCount + partialTick;

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180.0f - bodyYaw));
        if (player.deathTime > 0) {
            float f = Mth.sqrt((player.deathTime + partialTick - 1.0f) / 20.0f * 1.6f);
            pose.mulPose(Axis.ZP.rotationDegrees(Math.min(f, 1.0f) * 90.0f));
        }
        pose.scale(-1.0f, -1.0f, 1.0f);
        pose.translate(0.0f, -1.501f, 0.0f);

        model.attackTime = player.getAttackAnim(partialTick);
        model.setupAnim(player, limbSwing, limbAmount, age, netHeadYaw, headPitch);
        int overlay = LivingEntityRenderer.getOverlayCoords(player, 0.0f);
        model.renderToBuffer(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(texture)), light, overlay, 1f, 1f, 1f, 1f);
        if (eyes != null) {
            model.renderToBuffer(pose, buffers.getBuffer(RenderType.eyes(eyes)), LightTexture.FULL_BRIGHT, overlay, 1f, 1f, 1f, 1f);
        }
        pose.popPose();
    }
}
