package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {
	@Shadow
	private static GpuBufferSlice shaderFog;

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
	public static GpuBufferSlice getShaderFog() {
		return ClientGameEngine.INSTANCE.getShaderFog(shaderFog);
	}

	@Inject(at = @At("HEAD"), method = "flipFrame")
	private static void runTickTail(CallbackInfo ci) {
		// VidLibImGui.enable(Minecraft.getInstance()).frame();
	}
}
