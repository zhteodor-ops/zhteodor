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

/** Normal Jerry: a regular-looking dog. Texture 64x32. */
public class JerryDogModel extends EntityModel<Player> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart legFR, legFL, legBR, legBL, tail;

    public JerryDogModel() {
        root = create().bakeRoot();
        head = root.getChild("head");
        legFR = root.getChild("leg_fr");
        legFL = root.getChild("leg_fl");
        legBR = root.getChild("leg_br");
        legBL = root.getChild("leg_bl");
        tail = root.getChild("tail");
    }

    public static LayerDefinition create() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition p = mesh.getRoot();
        p.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3f, -3f, -5f, 6, 6, 10),
                PartPose.offset(0f, 14f, 1f));
        PartDefinition head = p.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 0).addBox(-3f, -3f, -4f, 6, 6, 5),
                PartPose.offset(0f, 12.5f, -4f));
        head.addOrReplaceChild("snout", CubeListBuilder.create().texOffs(0, 16).addBox(-1.5f, 0f, -7f, 3, 3, 3),
                PartPose.ZERO);
        head.addOrReplaceChild("ear_r", CubeListBuilder.create().texOffs(12, 16).addBox(-3f, -6f, -2f, 2, 3, 1),
                PartPose.ZERO);
        head.addOrReplaceChild("ear_l", CubeListBuilder.create().texOffs(18, 16).addBox(1f, -6f, -2f, 2, 3, 1),
                PartPose.ZERO);
        CubeListBuilder leg = CubeListBuilder.create().texOffs(24, 16).addBox(-1f, 0f, -1f, 2, 7, 2);
        p.addOrReplaceChild("leg_fr", leg, PartPose.offset(-2f, 17f, -2.5f));
        p.addOrReplaceChild("leg_fl", leg, PartPose.offset(2f, 17f, -2.5f));
        p.addOrReplaceChild("leg_br", leg, PartPose.offset(-2f, 17f, 4.5f));
        p.addOrReplaceChild("leg_bl", leg, PartPose.offset(2f, 17f, 4.5f));
        p.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(32, 16).addBox(-1f, 0f, -1f, 2, 7, 2),
                PartPose.offsetAndRotation(0f, 12f, 6f, 1.0f, 0f, 0f));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(Player player, float limbSwing, float limbAmount, float age, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        float swing = Mth.cos(limbSwing * 0.6662f) * 1.4f * limbAmount;
        legFR.xRot = swing;
        legBL.xRot = swing;
        legFL.xRot = -swing;
        legBR.xRot = -swing;
        tail.xRot = 1.0f + limbAmount * 0.3f;
        tail.yRot = Mth.cos(age * 0.6f) * 0.35f; // happy wag
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, float r, float g, float b, float a) {
        root.render(pose, buffer, light, overlay, r, g, b, a);
    }
}
