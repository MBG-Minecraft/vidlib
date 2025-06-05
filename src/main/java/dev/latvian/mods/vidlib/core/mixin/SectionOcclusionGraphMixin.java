package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.VidLibConfig;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(SectionOcclusionGraph.class)
public class SectionOcclusionGraphMixin {
	@ModifyConstant(method = "addSectionsInFrustum", constant = @Constant(intValue = 32))
	private int vl$addSectionsInFrustum(int original) {
		return VidLibConfig.robert ? VidLibConfig.clientRenderDistance : original;
	}
}
