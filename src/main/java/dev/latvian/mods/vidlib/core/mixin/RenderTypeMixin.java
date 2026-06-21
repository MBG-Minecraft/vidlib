package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLRenderType;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderType.class)
public abstract class RenderTypeMixin implements VLRenderType {
	@Override
	@Accessor("state")
	public abstract RenderSetup vl$getState();

	/*
	 * @author Lat
	 * @reason Bloom
	 @Overwrite public static RenderType tripwire() {
	 return BloomRenderTypes.DEFAULT_BLOCK;
	 }
	 */
}
