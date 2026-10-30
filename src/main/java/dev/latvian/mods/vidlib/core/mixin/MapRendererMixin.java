package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.vidlib.feature.client.VidLibEntityRenderStates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapRenderer.class)
public class MapRendererMixin {
	@Inject(method = "render", at = @At("HEAD"))
	private void vl$render(MapRenderState renderState, com.mojang.blaze3d.vertex.PoseStack poseStack, SubmitNodeCollector submitNodeCollector, boolean active, int packedLight, CallbackInfo ci) {
		var m = poseStack.last();
		var uv = renderState.getRenderDataOrDefault(VidLibEntityRenderStates.UV, UV.FULL);
		submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.text(renderState.texture), (pose, buffer) -> {
			buffer.addVertex(m, 0F, 128F, -0.01F).setColor(0xFFFFFFFF).setUv(uv.u0(), uv.v1()).setLight(packedLight);
			buffer.addVertex(m, 128F, 128F, -0.01F).setColor(0xFFFFFFFF).setUv(uv.u1(), uv.v1()).setLight(packedLight);
			buffer.addVertex(m, 128F, 0F, -0.01F).setColor(0xFFFFFFFF).setUv(uv.u1(), uv.v0()).setLight(packedLight);
			buffer.addVertex(m, 0F, 0F, -0.01F).setColor(0xFFFFFFFF).setUv(uv.u0(), uv.v0()).setLight(packedLight);
		});
	}

	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void vl$extractRenderState(MapId id, MapItemSavedData savedData, MapRenderState renderState, CallbackInfo ci) {
		var player = Minecraft.getInstance().player;
		var override = player == null ? null : player.vl$sessionData().getMapTextureOverride(id.id());
		renderState.setRenderData(VidLibEntityRenderStates.UV, override == null || override.isSpecial() ? null : Minecraft.getInstance().getSprite(override).uv());
	}
}
