package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.renderpearl.backend.opengl.GlTexture;
import dev.latvian.mods.vidlib.core.VLGpuTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Injects VidLib's {@link VLGpuTexture} handle accessor onto the OpenGL texture implementation.
 * <p>
 * 26.3 moved {@code com.mojang.blaze3d.textures.GpuTexture} to
 * {@code com.mojang.renderpearl.api.textures.GpuTexture}, where it is an *interface*, so the old
 * {@code @Mixin(GpuTexture.class)} no longer resolves. The GL id lives on the concrete OpenGL
 * backend class, which {@code interfaces.json} also injects the interface into.
 */
@Mixin(GlTexture.class)
public abstract class GpuTextureMixin implements VLGpuTexture {
	@Override
	@Invoker("glId")
	public abstract int vl$getHandle();
}
