package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.ShimmerConfig;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(SectionOcclusionGraph.class)
public class SectionOcclusionGraphMixin {
	@ModifyConstant(method = "addSectionsInFrustum", constant = @Constant(intValue = 32))
	private int shimmer$addSectionsInFrustum(int original) {
		return ShimmerConfig.maxChunkDistance;
	}
}
