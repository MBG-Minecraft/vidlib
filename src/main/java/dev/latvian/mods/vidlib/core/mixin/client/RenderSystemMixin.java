package dev.latvian.mods.vidlib.core.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {
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
}
