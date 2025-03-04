package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.ShimmerConfig;
import net.minecraft.client.renderer.blockentity.StructureBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(StructureBlockRenderer.class)
public class StructureBlockRendererMixin {
	/**
	 * @author Lat
	 * @reason Yeet
	 */
	@Overwrite
	public int getViewDistance() {
		return ShimmerConfig.structureBlockRange * 2;
	}
}
