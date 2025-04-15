package dev.beast.mods.shimmer.core.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.beast.mods.shimmer.feature.misc.MiscShimmerClientUtils;
import net.minecraft.client.renderer.FogParameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {
	@Shadow
	private static FogParameters shaderFog;

	/**
	 * @author Lat
	 * @reason Yeet
	 */
	@Overwrite
	public static boolean isOnRenderThread() {
		return true;
	}

	/**
	 * @author Lat
	 * @reason Yeet
	 */
	@Overwrite
	public static void assertOnRenderThread() {
	}

	/**
	 * @author Lat
	 * @reason Yeet
	 */
	@Overwrite
	public static FogParameters getShaderFog() {
		return MiscShimmerClientUtils.fogOverride != null ? MiscShimmerClientUtils.fogOverride : shaderFog;
	}
}
