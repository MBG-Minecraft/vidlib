package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLRenderType;
import dev.latvian.mods.vidlib.feature.bloom.BloomRenderTypes;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(RenderType.class)
public class RenderTypeMixin implements VLRenderType {
	/**
	 * @author Lat
	 * @reason Bloom
	 */
	@Overwrite
	public static RenderType tripwire() {
		return BloomRenderTypes.DEFAULT_BLOCK;
	}
}
