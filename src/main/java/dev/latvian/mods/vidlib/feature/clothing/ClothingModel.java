package dev.latvian.mods.vidlib.feature.clothing;

import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;

public class ClothingModel extends HumanoidModel<PlayerRenderState> {
	public static final CubeDeformation CUBE_DEFORMATION = new CubeDeformation(0.25F + 0.125F);

	public ClothingModel(ModelPart root) {
		super(root);
	}

	public static LayerDefinition createClothingLayer(boolean slim) {
		var mesh = HumanoidArmorModel.createBodyLayer(CUBE_DEFORMATION);
		var rootPart = mesh.getRoot();
		var headPart = rootPart.getChild("head");
		headPart.clearChild("hat");

		if (slim) {
			rootPart.addOrReplaceChild(
				"left_arm",
				CubeListBuilder.create().texOffs(32, 48).addBox(-1F, -2F, -2F, 3F, 12F, 4F, CUBE_DEFORMATION),
				PartPose.offset(5F, 2F, 0F)
			);
			rootPart.addOrReplaceChild(
				"right_arm",
				CubeListBuilder.create().texOffs(40, 16).addBox(-2F, -2F, -2F, 3F, 12F, 4F, CUBE_DEFORMATION),
				PartPose.offset(-5F, 2F, 0F)
			);
		}

		return LayerDefinition.create(mesh, 64, 32);
	}

	public static LayerDefinition createWideClothingLayer() {
		return createClothingLayer(false);
	}

	public static LayerDefinition createSlimClothingLayer() {
		return createClothingLayer(true);
	}
}
