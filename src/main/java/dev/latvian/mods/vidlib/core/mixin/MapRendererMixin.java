package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.vidlib.feature.client.VidLibEntityRenderStates;
import dev.latvian.mods.vidlib.util.client.EmptyVertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MapRenderer.class)
public class MapRendererMixin {
	@ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;", ordinal = 0))
	private VertexConsumer vl$originalMapRenderer(VertexConsumer original) {
		return EmptyVertexConsumer.INSTANCE;
	}

	@Inject(method = "render", at = @At("HEAD"))
	private void vl$render(MapRenderState renderState, PoseStack poseStack, MultiBufferSource bufferSource, boolean active, int packedLight, CallbackInfo ci) {
		var m = poseStack.last().pose();
		var buffer = bufferSource.getBuffer(RenderType.text(renderState.texture));
		var uv = renderState.getRenderDataOrDefault(VidLibEntityRenderStates.UV, UV.FULL);
		buffer.addVertex(m, 0F, 128F, -0.01F).setColor(0xFFFFFFFF).setUv(uv.u0(), uv.v1()).setLight(packedLight);
		buffer.addVertex(m, 128F, 128F, -0.01F).setColor(0xFFFFFFFF).setUv(uv.u1(), uv.v1()).setLight(packedLight);
		buffer.addVertex(m, 128F, 0F, -0.01F).setColor(0xFFFFFFFF).setUv(uv.u1(), uv.v0()).setLight(packedLight);
		buffer.addVertex(m, 0F, 0F, -0.01F).setColor(0xFFFFFFFF).setUv(uv.u0(), uv.v0()).setLight(packedLight);
	}

	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void vl$extractRenderState(MapId id, MapItemSavedData savedData, MapRenderState renderState, CallbackInfo ci) {
		var player = Minecraft.getInstance().player;
		var override = player == null ? null : player.vl$sessionData().getMapTextureOverride(id.id());
		renderState.setRenderData(VidLibEntityRenderStates.UV, override == null || override.isSpecial() ? null : Minecraft.getInstance().getSprite(override).uv());
	}
}
