package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.util.LevelOfDetailValue;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockEntityRenderer.class)
public interface BlockEntityRendererMixin {

	/**
	 * @author LimeGlass
	 * @reason yeet
	 */
	@Overwrite
	default int getViewDistance() {
		return (int) LevelOfDetailValue.BLOCK_ENTITIES.getDistance();
	}
}
