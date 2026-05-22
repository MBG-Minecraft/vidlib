package dev.latvian.mods.vidlib.feature.client.babymodel;

import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;

public class BabyChickenModel extends ChickenModel {
	public static final ResourceLocation CHICKEN_BABY_TEMPERATE = ResourceLocation.withDefaultNamespace("textures/entity/chicken/chicken_baby_temperate.png");

	public BabyChickenModel(ModelPart root) {
		super(root);
	}

	public static LayerDefinition createBodyLayer() {
		var mesh = new MeshDefinition();
		var root = mesh.getRoot();
		root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
		root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2F, -2.25F, -0.75F, 4F, 4F, 4F).texOffs(10, 8).addBox(-1F, -0.25F, -1.75F, 2F, 1F, 1F), PartPose.offset(0F, 20.25F, -1.25F));
		root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(2, 2).addBox(-0.5F, 0F, 0F, 1F, 2F, 0F).texOffs(0, 1).addBox(-0.5F, 2F, -1F, 1F, 0F, 1F), PartPose.offset(1F, 22F, 0.5F));
		root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 2).addBox(-0.5F, 0F, 0F, 1F, 2F, 0F).texOffs(0, 0).addBox(-0.5F, 2F, -1F, 1F, 0F, 1F), PartPose.offset(-1F, 22F, 0.5F));
		root.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(6, 8).addBox(0F, 0F, -1F, 1F, 0F, 2F), PartPose.offset(2F, 20F, 0F));
		root.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(4, 8).addBox(-1F, 0F, -1F, 1F, 0F, 2F), PartPose.offset(-2F, 20F, 0F));
		return LayerDefinition.create(mesh, 16, 16);
	}
}
