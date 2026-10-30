package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.renderpearl.backend.opengl.GlBuffer;
import dev.latvian.mods.vidlib.core.VLGpuBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Injects VidLib's {@link VLGpuBuffer} handle accessor onto the OpenGL buffer implementation.
 * <p>
 * 26.3 moved {@code com.mojang.blaze3d.buffers.GpuBuffer} to
 * {@code com.mojang.renderpearl.api.buffers.GpuBuffer}, where it is an *interface*, so the old
 * {@code @Mixin(GpuBuffer.class)} no longer resolves. The handle lives on the concrete OpenGL
 * backend class, which is also what {@code interfaces.json} injects the interface into, so
 * targeting {@link GlBuffer} here keeps both paths consistent.
 */
@Mixin(GlBuffer.class)
public abstract class GpuBufferMixin implements VLGpuBuffer {
	@Override
	@Accessor("handle")
	public abstract int vl$getHandle();
}
