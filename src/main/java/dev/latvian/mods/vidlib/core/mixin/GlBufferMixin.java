package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.renderpearl.backend.opengl.GlBuffer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Kept for the VIDL `GpuBuffer` handle accessor, which now lives in {@link GpuBufferMixin}
 * (both target the same OpenGL implementation since 26.3 moved GpuBuffer to renderpearl as an
 * interface). This class stays registered so the mixin config is stable and holds the
 * documentation for the change.
 */
@Mixin(GlBuffer.class)
public class GlBufferMixin {
}
