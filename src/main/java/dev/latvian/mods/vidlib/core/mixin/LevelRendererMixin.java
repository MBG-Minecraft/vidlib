package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.skybox.SkyboxRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
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
	private void vl$allChanged(CallbackInfo ci) {
		AutoInit.Type.CHUNKS_RELOADED.invoke();
	}

	//@Redirect(method = "allChanged", at = @At(value = "NEW", target = "(Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher;Lnet/minecraft/world/level/Level;ILnet/minecraft/client/renderer/LevelRenderer;)Lnet/minecraft/client/renderer/ViewArea;"))
	//private ViewArea vl$allChangedView(SectionRenderDispatcher sectionRenderDispatcher, Level level, int viewDistance, LevelRenderer levelRenderer) {
	//	return new VidLibViewArea(sectionRenderDispatcher, level, viewDistance, levelRenderer);
	//}

	@Redirect(method = "lambda$addSkyPass$13", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;renderSky(Lnet/minecraft/client/multiplayer/ClientLevel;IFLorg/joml/Matrix4f;Lnet/minecraft/client/Camera;Lorg/joml/Matrix4f;Ljava/lang/Runnable;)Z"))
	private boolean vl$renderSkybox(DimensionSpecialEffects instance, ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, Runnable setupFog) {
		var skybox = minecraft.player != null ? minecraft.player.vl$sessionData().skybox : null;

		if (skybox != null) {
			return SkyboxRenderer.render(minecraft, skybox, setupFog);
		} else {
			return instance.renderSky(level, ticks, partialTick, modelViewMatrix, camera, projectionMatrix, setupFog);
		}
	}

	/**
	 * @author Lat
	 * @reason Yeet
	 */
	@Overwrite
	private boolean shouldRenderDarkDisc(float partialTick) {
		return false;
	}
}
