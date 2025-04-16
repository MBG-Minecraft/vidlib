package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.VidLibConfig;
import net.minecraft.client.renderer.blockentity.BlockEntityWithBoundingBoxRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockEntityWithBoundingBoxRenderer.class)
public class BlockEntityWithBoundingBoxRendererMixin {
	/**
	 * @author Lat
	 * @reason Yeet
	 */
	@Overwrite
	public int getViewDistance() {
		return VidLibConfig.structureBlockRange * 2;
	}
}
