package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.blaze3d.opengl.GlBuffer;
import dev.latvian.mods.vidlib.core.VLGpuBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlBuffer.class)
public abstract class GlBufferMixin implements VLGpuBuffer {
	@Override
	@Accessor("handle")
	public abstract int vl$getHandle();
}
