package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.blaze3d.buffers.GpuBuffer;
import dev.latvian.mods.vidlib.core.VLGpuBuffer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GpuBuffer.class)
public class GpuBufferMixin implements VLGpuBuffer {
}
