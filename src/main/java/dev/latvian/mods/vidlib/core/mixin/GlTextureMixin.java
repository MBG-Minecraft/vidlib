package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.blaze3d.opengl.GlTexture;
import dev.latvian.mods.vidlib.core.VLGpuTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GlTexture.class)
public abstract class GlTextureMixin implements VLGpuTexture {
	@Override
	@Invoker("glId")
	public abstract int vl$getHandle();
}
