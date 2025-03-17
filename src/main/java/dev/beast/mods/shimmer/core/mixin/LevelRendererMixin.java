package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.skybox.SkyboxRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "allChanged", at = @At("RETURN"))
	private void shimmer$allChanged(CallbackInfo ci) {
		AutoInit.Type.CHUNKS_RELOADED.invoke();
	}

	@Redirect(method = "lambda$addSkyPass$12", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;renderSky(Lnet/minecraft/client/multiplayer/ClientLevel;IFLorg/joml/Matrix4f;Lnet/minecraft/client/Camera;Lorg/joml/Matrix4f;Ljava/lang/Runnable;)Z"))
	private boolean shimmer$renderSkybox(DimensionSpecialEffects instance, ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, Runnable setupFog) {
		var skybox = minecraft.player != null ? minecraft.player.shimmer$sessionData().skybox : null;

		if (skybox != null) {
			return SkyboxRenderer.render(minecraft, skybox, setupFog);
		} else {
			return instance.renderSky(level, ticks, partialTick, modelViewMatrix, camera, projectionMatrix, setupFog);
		}
	}
}
