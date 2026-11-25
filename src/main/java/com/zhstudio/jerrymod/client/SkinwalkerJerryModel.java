package com.zhstudio.jerrymod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/** Skinwalker Jerry: tall, hunched, long arms, dog skull head. Texture 128x64. */
public class SkinwalkerJerryModel extends EntityModel<Player> {
    private static final float LEAN = 0.55f;

    private final ModelPart root;
    private final ModelPart torso, head, armR, armL, legR, legL;

    public SkinwalkerJerryModel() {
        root = create().bakeRoot();
        torso = root.getChild("torso");
        head = torso.getChild("head");
        armR = torso.getChild("arm_r");
        armL = torso.getChild("arm_l");
        legR = root.getChild("leg_r");
        legL = root.getChild("leg_l");
    }

    public static LayerDefinition create() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition p = mesh.getRoot();
        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 40).addBox(-1.5f, 0f, -1.5f, 3, 20, 3);
        p.addOrReplaceChild("leg_r", leg, PartPose.offset(-2.5f, 4f, 0f));
        p.addOrReplaceChild("leg_l", leg, PartPose.offset(2.5f, 4f, 0f));

        PartDefinition torso = p.addOrReplaceChild("torso",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4f, -16f, -2.5f, 8, 16, 5),
                PartPose.offsetAndRotation(0f, 4f, 0f, LEAN, 0f, 0f));

        PartDefinition head = torso.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(26, 0).addBox(-3f, -6f, -4f, 6, 6, 6),
                PartPose.offset(0f, -16f, -1f));
        head.addOrReplaceChild("snout", CubeListBuilder.create().texOffs(50, 0).addBox(-2f, -3f, -8f, 4, 3, 4),
                PartPose.ZERO);
        head.addOrReplaceChild("ear_r", CubeListBuilder.create().texOffs(66, 0).addBox(-3f, -9f, -1f, 1, 3, 1),
                PartPose.ZERO);
        head.addOrReplaceChild("ear_l", CubeListBuilder.create().texOffs(70, 0).addBox(2f, -9f, -1f, 1, 3, 1),
                PartPose.ZERO);

        CubeListBuilder arm = CubeListBuilder.create()
                .texOffs(24, 40).addBox(-1f, 0f, -1f, 2, 22, 2)
                .texOffs(40, 40).addBox(-1.5f, 22f, -1.5f, 3, 3, 3);
        torso.addOrReplaceChild("arm_r", arm, PartPose.offset(-5f, -15f, 0f));
        torso.addOrReplaceChild("arm_l", arm, PartPose.offset(5f, -15f, 0f));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(Player player, float limbSwing, float limbAmount, float age, float netHeadYaw, float headPitch) {
        torso.xRot = LEAN + Mth.sin(age * 0.05f) * 0.03f; // creepy breathing
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = -LEAN + headPitch * Mth.DEG_TO_RAD;
        head.zRot = Mth.sin(age * 0.07f) * 0.12f; // head tilt twitch
        float swing = Mth.cos(limbSwing * 0.5f) * 1.1f * limbAmount;
        legR.xRot = swing;
        legL.xRot = -swing;
        // long arms hang forward and drag
        armR.xRot = -LEAN - swing * 0.8f;
        armL.xRot = -LEAN + swing * 0.8f;
        armR.zRot = 0.12f;
        armL.zRot = -0.12f;
        if (attackTime > 0f) {
            float slash = Mth.sin(attackTime * Mth.PI);
            armR.xRot -= slash * 1.6f;
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, float r, float g, float b, float a) {
        root.render(pose, buffer, light, overlay, r, g, b, a);
    }
}
