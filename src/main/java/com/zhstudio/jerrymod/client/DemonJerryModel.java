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

/** Demonic Jerry: big horned beast with spikes and a snapping jaw. Texture 128x64. */
public class DemonJerryModel extends EntityModel<Player> {
    private final ModelPart root;
    private final ModelPart head, jaw;
    private final ModelPart legFR, legFL, legBR, legBL, tail;

    public DemonJerryModel() {
        root = create().bakeRoot();
        head = root.getChild("head");
        jaw = head.getChild("jaw");
        legFR = root.getChild("leg_fr");
        legFL = root.getChild("leg_fl");
        legBR = root.getChild("leg_br");
        legBL = root.getChild("leg_bl");
        tail = root.getChild("tail");
    }

    public static LayerDefinition create() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition p = mesh.getRoot();
        PartDefinition body = p.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-5f, -5f, -8f, 10, 10, 16),
                PartPose.offset(0f, 10f, 2f));
        CubeListBuilder spike = CubeListBuilder.create().texOffs(62, 26).addBox(-1f, -9f, -1f, 2, 4, 2);
        body.addOrReplaceChild("spike1", spike, PartPose.offset(0f, 0f, -5f));
        body.addOrReplaceChild("spike2", spike, PartPose.offset(0f, 0f, 0f));
        body.addOrReplaceChild("spike3", spike, PartPose.offset(0f, 0f, 5f));

        PartDefinition head = p.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(52, 0).addBox(-4f, -4f, -7f, 8, 8, 7),
                PartPose.offset(0f, 8f, -6f));
        head.addOrReplaceChild("snout", CubeListBuilder.create().texOffs(0, 26).addBox(-3f, -1f, -12f, 6, 4, 5),
                PartPose.ZERO);
        head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(22, 26).addBox(-3f, 0f, -6f, 6, 2, 6),
                PartPose.offset(0f, 3f, -6f));
        head.addOrReplaceChild("horn_r", CubeListBuilder.create().texOffs(46, 26).addBox(-4f, -9f, -3f, 2, 5, 2),
                PartPose.ZERO);
        head.addOrReplaceChild("horn_l", CubeListBuilder.create().texOffs(54, 26).addBox(2f, -9f, -3f, 2, 5, 2),
                PartPose.ZERO);

        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 40).addBox(-2f, 0f, -2f, 4, 9, 4);
        p.addOrReplaceChild("leg_fr", leg, PartPose.offset(-3.5f, 15f, -3f));
        p.addOrReplaceChild("leg_fl", leg, PartPose.offset(3.5f, 15f, -3f));
        p.addOrReplaceChild("leg_br", leg, PartPose.offset(-3.5f, 15f, 7f));
        p.addOrReplaceChild("leg_bl", leg, PartPose.offset(3.5f, 15f, 7f));
        p.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(16, 40).addBox(-1.5f, 0f, -1.5f, 3, 10, 3),
                PartPose.offsetAndRotation(0f, 7f, 10f, 1.1f, 0f, 0f));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(Player player, float limbSwing, float limbAmount, float age, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        jaw.xRot = 0.25f + Math.abs(Mth.sin(age * 0.15f)) * 0.35f; // snapping jaw
        float swing = Mth.cos(limbSwing * 0.5f) * 1.2f * limbAmount;
        legFR.xRot = swing;
        legBL.xRot = swing;
        legFL.xRot = -swing;
        legBR.xRot = -swing;
        tail.xRot = 1.1f;
        tail.yRot = Mth.sin(age * 0.1f) * 0.25f;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, float r, float g, float b, float a) {
        root.render(pose, buffer, light, overlay, r, g, b, a);
    }
}
