package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLRenderType;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RenderType.class)
public class RenderTypeMixin implements VLRenderType {
	/*
	 * @author Lat
	 * @reason Bloom
	 @Overwrite public static RenderType tripwire() {
	 return BloomRenderTypes.DEFAULT_BLOCK;
	 }
	 */
}
