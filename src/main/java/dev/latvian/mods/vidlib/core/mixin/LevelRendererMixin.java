package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.WorldBorderRenderState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 1001) // Load before Sodium
public abstract class LevelRendererMixin {
	@ModifyExpressionValue(method = {"prepareTranslucents", "executeOit", "executeClassicTransparency"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/LevelRenderState;worldBorderRenderState:Lnet/minecraft/client/renderer/state/level/WorldBorderRenderState;", opcode = Opcodes.GETFIELD))
	private WorldBorderRenderState overrideWorldBorderRenderState(WorldBorderRenderState original) {
		var mc = Minecraft.getInstance();
		var override = ClientGameEngine.INSTANCE.overrideRenderedWorldBorder(mc);
		return override == null ? original : override;
	}

	@Inject(method = "invalidateCompiledGeometry", at = @At("RETURN"))
	private void vl$invalidateCompiledGeometry(ClientLevel level, Options options, Camera camera, BlockColors blockColors, CallbackInfo ci) {
		var mc = Minecraft.getInstance();

		if (mc.level != null) {
			AutoInit.Type.CHUNKS_RELOADED.invoke(mc.level);
		}
	}

	@Inject(method = "submitBlockOutline", at = @At("HEAD"), cancellable = true)
	private void vl$submitBlockOutline(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LevelRenderState levelRenderState, CallbackInfo ci) {
		if (ClientGameEngine.INSTANCE.cancelBlockHighlight(levelRenderState)) {
			ci.cancel();
		}
	}
}
