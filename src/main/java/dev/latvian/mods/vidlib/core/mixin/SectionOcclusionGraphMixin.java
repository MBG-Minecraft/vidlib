package dev.latvian.mods.vidlib.core.mixin;

import net.minecraft.client.renderer.SectionOcclusionGraph;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SectionOcclusionGraph.class)
public class SectionOcclusionGraphMixin {
	//@ModifyConstant(method = "addSectionsInFrustum", constant = @Constant(intValue = 32))
	//private int vl$addSectionsInFrustum(int original) {
	//	return VidLibConfig.maxChunkDistance;
	//}
}
