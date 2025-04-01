package dev.beast.mods.shimmer.core.mixin;

import net.minecraft.client.renderer.SectionOcclusionGraph;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SectionOcclusionGraph.class)
public class SectionOcclusionGraphMixin {
	//@ModifyConstant(method = "addSectionsInFrustum", constant = @Constant(intValue = 32))
	//private int shimmer$addSectionsInFrustum(int original) {
	//	return ShimmerConfig.maxChunkDistance;
	//}
}
