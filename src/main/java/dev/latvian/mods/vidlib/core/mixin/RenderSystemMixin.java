package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.latvian.mods.vidlib.feature.skybox.ClientFogOverride;
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
		return ClientFogOverride.get(shaderFog);
	}
}
