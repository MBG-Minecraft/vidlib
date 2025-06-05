package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.VidLibConfig;
import dev.latvian.mods.vidlib.util.client.VLSectionToNodeMap;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SectionOcclusionGraph.GraphStorage.class)
public class SectionOcclusionGraphGraphStorageMixin {
	@Redirect(method = "<init>", at = @At(value = "NEW", target = "(I)Lnet/minecraft/client/renderer/SectionOcclusionGraph$SectionToNodeMap;"))
	private SectionOcclusionGraph.SectionToNodeMap vl$customNodeMap(int size) {
		if (VidLibConfig.robert) {
			return new VLSectionToNodeMap();
		} else {
			return new SectionOcclusionGraph.SectionToNodeMap(size);
		}
	}
}
