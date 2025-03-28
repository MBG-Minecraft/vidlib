package dev.beast.mods.shimmer.core.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SkyRenderer;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
	@Redirect(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V", ordinal = 1))
	private void shimmer$renderSunMoonAndStars(PoseStack ms, Quaternionf quaternion) {
		var player = Minecraft.getInstance().player;
		var override = player != null ? player.shimmer$sessionData().skyRotation : null;

		if (override != null) {
			ms.mulPose(Axis.YP.rotation(override.yawRad()));
			ms.mulPose(Axis.XP.rotation(override.pitchRad()));
		} else {
			ms.mulPose(quaternion);
		}
	}
}
