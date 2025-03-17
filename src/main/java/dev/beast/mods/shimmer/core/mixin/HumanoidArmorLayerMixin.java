package dev.beast.mods.shimmer.core.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.beast.mods.shimmer.feature.clothing.Clothing;
import dev.beast.mods.shimmer.feature.misc.MiscShimmerClientUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>> extends RenderLayer<S, M> {
	@Shadow
	protected abstract A getArmorModel(S renderState, EquipmentSlot slot);

	@Shadow
	protected abstract void setPartVisibility(A model, EquipmentSlot slot);

	@Shadow
	protected abstract boolean usesInnerModel(EquipmentSlot slot);

	@Shadow
	@Final
	private EquipmentLayerRenderer equipmentRenderer;

	public HumanoidArmorLayerMixin(RenderLayerParent<S, M> renderer) {
		super(renderer);
	}

	@Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At("RETURN"))
	private void shimmer$render(PoseStack ms, MultiBufferSource buffers, int light, S state, float yRot, float xRot, CallbackInfo ci) {
		var clothing = state.getRenderData(MiscShimmerClientUtils.CLOTHING);

		if (clothing != null) {
			shimmer$renderArmorPiece(ms, buffers, light, state, clothing, EquipmentSlot.CHEST);
			shimmer$renderArmorPiece(ms, buffers, light, state, clothing, EquipmentSlot.LEGS);
			shimmer$renderArmorPiece(ms, buffers, light, state, clothing, EquipmentSlot.FEET);
			shimmer$renderArmorPiece(ms, buffers, light, state, clothing, EquipmentSlot.HEAD);
		}
	}

	@Unique
	private void shimmer$renderArmorPiece(PoseStack ms, MultiBufferSource buffers, int light, S state, Clothing clothing, EquipmentSlot slot) {
		var model = getArmorModel(state, slot);
		getParentModel().copyPropertiesTo(model);
		setPartVisibility(model, slot);
		var layer = usesInnerModel(slot) ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS : EquipmentClientInfo.LayerType.HUMANOID;
		this.equipmentRenderer.renderLayers(layer, clothing.equipmentAsset, model, clothing.item, ms, buffers, light);
	}
}
