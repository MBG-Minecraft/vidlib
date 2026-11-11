package dev.latvian.mods.vidlib.core.mixin.client;

import dev.latvian.mods.vidlib.core.api.client.VLRenderType;
import net.minecraft.client.renderer.rendertype.RenderType;
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
