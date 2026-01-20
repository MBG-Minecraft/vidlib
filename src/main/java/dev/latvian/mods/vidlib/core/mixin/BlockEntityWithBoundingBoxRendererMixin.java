package dev.latvian.mods.vidlib.core.mixin;

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
		return 8192;
	}
}
