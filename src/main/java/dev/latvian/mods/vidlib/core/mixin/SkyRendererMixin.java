package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.commands.RenderPass;
import dev.latvian.mods.vidlib.feature.skybox.ClientSkybox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 26.3 rewrote {@link SkyRenderer}: celestial bodies are drawn with GPU buffers and a
 * {@link RenderPass} instead of a {@code MultiBufferSource}, stars/sun/moon take an
 * {@code com.mojang.renderpearl.api.commands.RenderPass} instead of fog parameters, and
 * {@code FogParameters} no longer exists at all.
 */
@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
	@Shadow
	protected abstract void renderStars(RenderPass renderPass, float starBrightness, PoseStack poseStack);

	@Shadow
	protected abstract void renderSun(RenderPass renderPass, float rainBrightness, PoseStack poseStack);

	@Shadow
	protected abstract void renderMoon(RenderPass renderPass, MoonPhase moonPhase, float rainBrightness, PoseStack poseStack);

	/**
	 * Applies the skybox override's celestial rotation. 1.21.5 redirected the second
	 * {@code PoseStack.mulPose(...)} in {@code renderSunMoonAndStars}; 26.3 replaced those with
	 * {@code rotateDegrees}/{@code rotate}, so the equivalent slot is the single
	 * {@code rotateDegrees(Axis.YP, -90F)} that orients the whole celestial group.
	 */
	@Redirect(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;rotateDegrees(Lcom/mojang/math/Axis;F)V"))
	private void vl$celestialRotation(PoseStack ms, Axis axis, float angle) {
		var player = Minecraft.getInstance().player;
		var override = player != null ? player.vl$sessionData().skybox : null;

		if (override != null && override.data.celestialRotation().isPresent()) {
			ms.rotate(Axis.YP, override.data.celestialRotation().get().yawRad());
			ms.rotate(Axis.XP, override.data.celestialRotation().get().pitchRad());
		} else {
			ms.rotateDegrees(axis, angle);
		}
	}

	@Redirect(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSun(Lcom/mojang/renderpearl/api/commands/RenderPass;FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void vl$renderSun(SkyRenderer instance, RenderPass renderPass, float rainBrightness, PoseStack poseStack) {
		var override = vl$skyboxOverride();

		if (override == null || override.data.sun()) {
			renderSun(renderPass, rainBrightness, poseStack);
		}
	}

	@Redirect(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderMoon(Lcom/mojang/renderpearl/api/commands/RenderPass;Lnet/minecraft/world/level/MoonPhase;FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void vl$renderMoon(SkyRenderer instance, RenderPass renderPass, MoonPhase moonPhase, float rainBrightness, PoseStack poseStack) {
		var override = vl$skyboxOverride();

		if (override == null || override.data.moon()) {
			renderMoon(renderPass, moonPhase, rainBrightness, poseStack);
		}
	}

	/**
	 * Hides vanilla stars when a skybox override supplies its own, and additionally renders the
	 * override's stars at the override's brightness. 1.21.5 split this across a redirect and an
	 * inject after {@code MultiBufferSource.BufferSource.endBatch()}; 26.3 removed batching from
	 * {@link SkyRenderer} entirely, so both behaviours are folded into this single redirect.
	 */
	@Redirect(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderStars(Lcom/mojang/renderpearl/api/commands/RenderPass;FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void vl$renderStars(SkyRenderer instance, RenderPass renderPass, float starBrightness, PoseStack poseStack) {
		var override = vl$skyboxOverride();

		if (override == null || override.data.stars().isEmpty()) {
			renderStars(renderPass, starBrightness, poseStack);
		}

		if (override != null && override.data.stars().isPresent() && override.data.stars().get() > 0F) {
			renderStars(renderPass, override.data.stars().get(), poseStack);
		}
	}

	private static ClientSkybox vl$skyboxOverride() {
		var player = Minecraft.getInstance().player;
		return player == null ? null : player.vl$sessionData().skybox;
	}

	/**
	 * {@code LevelRenderer.shouldRenderDarkDisc(float)} moved to {@link SkyRenderer} (it is a
	 * {@code private} method there, so it has to be fully overwritten).
	 *
	 * @author Lat
	 * @reason Yeet
	 */
	@Overwrite
	private boolean shouldRenderDarkDisc(float deltaPartialTick, ClientLevel level) {
		return false;
	}
}
