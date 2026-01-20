package dev.latvian.mods.vidlib.feature.clothing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.client.VidLibEntityRenderStates;
import dev.latvian.mods.vidlib.util.LevelOfDetailValue;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.Map;
import java.util.function.Function;

public class ClothingLayer extends RenderLayer<PlayerRenderState, PlayerModel> {
	public static final ModelLayerLocation WIDE = new ModelLayerLocation(VidLib.id("player"), "clothing");
	public static final ModelLayerLocation SLIM = new ModelLayerLocation(VidLib.id("player_slim"), "clothing");

	record LayerTextureKey(EquipmentClientInfo.LayerType layerType, EquipmentClientInfo.Layer layer) {
	}

	private static final Map<ResourceLocation, ResourceKey<EquipmentAsset>> KEYS = new Object2ObjectOpenHashMap<>();

	public static ResourceKey<EquipmentAsset> getKey(ResourceLocation id) {
		return KEYS.computeIfAbsent(id, i -> ResourceKey.create(EquipmentAssets.ROOT_ID, i));
	}

	private final EquipmentAssetManager equipmentAssets;
	private final ClothingModel model;
	private final Function<EquipmentClientInfo.Layer, RenderType> humanoidRenderTypelookup;
	private final Function<EquipmentClientInfo.Layer, RenderType> humanoidLeggingRenderTypelookup;

	public ClothingLayer(RenderLayerParent<PlayerRenderState, PlayerModel> renderer, EntityRendererProvider.Context context, boolean slim) {
		super(renderer);
		this.equipmentAssets = context.getEquipmentAssets();
		this.model = new ClothingModel(context.bakeLayer(slim ? SLIM : WIDE));
		this.humanoidRenderTypelookup = Util.memoize(key -> RenderType.armorCutoutNoCull(key.getTextureLocation(EquipmentClientInfo.LayerType.HUMANOID)));
		this.humanoidLeggingRenderTypelookup = Util.memoize(key -> RenderType.armorCutoutNoCull(key.getTextureLocation(EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS)));
	}

	@Override
	public void render(PoseStack ms, MultiBufferSource buffers, int light, PlayerRenderState state, float yRot, float xRot) {
		var clothing = VidLibEntityRenderStates.getClothing(state);

		if (clothing == Clothing.NONE || !LevelOfDetailValue.CLOTHING.isVisible(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition(), state.x, state.y, state.z)) {
			return;
		}

		var assets = equipmentAssets.get(getKey(clothing.id()));

		if (assets == EquipmentAssetManager.MISSING) {
			return;
		}

		var parentModel = getParentModel();

		for (var slot : Clothing.ORDERED_SLOTS) {
			if (!clothing.parts().visible(slot)) {
				continue;
			}

			var layerType = slot == EquipmentSlot.LEGS ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS : EquipmentClientInfo.LayerType.HUMANOID;
			var layers = assets.getLayers(layerType);

			for (var layer : layers) {
				ms.pushPose();

				var renderType = (slot == EquipmentSlot.LEGS ? humanoidLeggingRenderTypelookup : humanoidRenderTypelookup).apply(layer);
				VertexConsumer buffer;

				if (clothing.parts().enchanted()) {
					buffer = VertexMultiConsumer.create(buffers.getBuffer(RenderType.armorEntityGlint()), buffers.getBuffer(renderType));
				} else {
					buffer = buffers.getBuffer(renderType);
				}

				parentModel.copyPropertiesTo(model);
				model.setAllVisible(false);

				switch (slot) {
					case HEAD:
						model.head.visible = true;
						break;
					case CHEST:
						model.body.visible = true;
						model.rightArm.visible = true;
						model.leftArm.visible = true;
						break;
					case LEGS:
						model.body.visible = true;
						model.rightLeg.visible = true;
						model.leftLeg.visible = true;
						break;
					case FEET:
						model.rightLeg.visible = true;
						model.leftLeg.visible = true;
				}

				model.setupAnim(state);
				model.renderToBuffer(ms, buffer, light, OverlayTexture.NO_OVERLAY);
				ms.popPose();
			}
		}
	}
}
