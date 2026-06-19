package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.vidlib.feature.client.VidLibEntityRenderStates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MapRenderer.class)
public class MapRendererMixin {
	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitCustomGeometry(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/SubmitNodeCollector$CustomGeometryRenderer;)V", ordinal = 0))
	private void vl$renderMapQuad(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, RenderType renderType, SubmitNodeCollector.CustomGeometryRenderer customGeometryRenderer, Operation<Void> original, MapRenderState renderState, PoseStack originalPoseStack, SubmitNodeCollector originalSubmitNodeCollector, boolean active, int packedLight) {
		var uv = renderState.getRenderDataOrDefault(VidLibEntityRenderStates.UV, UV.FULL);
		original.call(submitNodeCollector, poseStack, renderType, (SubmitNodeCollector.CustomGeometryRenderer) (pose, buffer) -> {
			buffer.addVertex(pose, 0F, 128F, -0.01F).setColor(0xFFFFFFFF).setUv(uv.u0(), uv.v1()).setLight(packedLight);
			buffer.addVertex(pose, 128F, 128F, -0.01F).setColor(0xFFFFFFFF).setUv(uv.u1(), uv.v1()).setLight(packedLight);
			buffer.addVertex(pose, 128F, 0F, -0.01F).setColor(0xFFFFFFFF).setUv(uv.u1(), uv.v0()).setLight(packedLight);
			buffer.addVertex(pose, 0F, 0F, -0.01F).setColor(0xFFFFFFFF).setUv(uv.u0(), uv.v0()).setLight(packedLight);
		});
	}

	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void vl$extractRenderState(MapId id, MapItemSavedData savedData, MapRenderState renderState, CallbackInfo ci) {
		var player = Minecraft.getInstance().player;
		var override = player == null ? null : player.vl$sessionData().getMapTextureOverride(id.id());
		renderState.setRenderData(VidLibEntityRenderStates.UV, override == null || override.isSpecial() ? null : Minecraft.getInstance().getSprite(override).uv());
	}
}
