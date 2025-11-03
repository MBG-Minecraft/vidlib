package dev.latvian.mods.vidlib.feature.clothing;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.client.VidLibEntityRenderStates;
import net.minecraft.Util;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class ClothingLayer extends RenderLayer<PlayerRenderState, PlayerModel> {
	public static final ModelLayerLocation WIDE = new ModelLayerLocation(VidLib.id("player"), "clothing");
	public static final ModelLayerLocation SLIM = new ModelLayerLocation(VidLib.id("player_slim"), "clothing");

	private static final EquipmentClientInfo.LayerType[] LAYER_TYPES = {EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS, EquipmentClientInfo.LayerType.HUMANOID};

	record LayerTextureKey(EquipmentClientInfo.LayerType layerType, EquipmentClientInfo.Layer layer) {
	}

	private final EquipmentAssetManager equipmentAssets;
	private final ClothingModel model;
	private final Function<LayerTextureKey, ResourceLocation> layerTextureLookup;

	public ClothingLayer(RenderLayerParent<PlayerRenderState, PlayerModel> renderer, EntityRendererProvider.Context context, boolean slim) {
		super(renderer);
		this.equipmentAssets = context.getEquipmentAssets();
		this.model = new ClothingModel(context.bakeLayer(slim ? SLIM : WIDE));
		this.layerTextureLookup = Util.memoize(key -> key.layer.getTextureLocation(key.layerType));
	}

	@Override
	public void render(PoseStack ms, MultiBufferSource buffers, int light, PlayerRenderState state, float yRot, float xRot) {
		var clothing = VidLibEntityRenderStates.getClothing(state);

		if (clothing == Clothing.NONE) {
			return;
		}

		var parentModel = getParentModel();
		var assets = equipmentAssets.get(clothing.id());

		for (var layerType : LAYER_TYPES) {
			for (var layer : assets.getLayers(layerType)) {
				var texture = layerTextureLookup.apply(new LayerTextureKey(layerType, layer));

				ms.pushPose();
				var buffer = buffers.getBuffer(RenderType.armorCutoutNoCull(texture));
				parentModel.copyPropertiesTo(model);
				model.setupAnim(state);
				model.renderToBuffer(ms, buffer, light, OverlayTexture.NO_OVERLAY);
				ms.popPose();
			}
		}
	}
}
