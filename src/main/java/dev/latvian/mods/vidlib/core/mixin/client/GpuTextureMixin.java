package dev.latvian.mods.vidlib.core.mixin.client;

import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.backend.opengl.FrameBufferAttachment;
import dev.latvian.mods.vidlib.core.api.client.VLGpuTexture;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GpuTexture.class)
public interface GpuTextureMixin extends VLGpuTexture {
	default long vl$getHandle() {
		return this instanceof FrameBufferAttachment f ? f.glId() : 0L;
	}
}
