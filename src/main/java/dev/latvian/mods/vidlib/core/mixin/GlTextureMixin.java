package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.renderpearl.backend.opengl.GlTexture;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Kept for the VIDL `GpuTexture` handle accessor, which now lives in {@link GpuTextureMixin}
 * (both target the same OpenGL implementation since 26.3 moved GpuTexture to renderpearl as an
 * interface). This class stays registered so the mixin config is stable.
 */
@Mixin(GlTexture.class)
public class GlTextureMixin {
}
