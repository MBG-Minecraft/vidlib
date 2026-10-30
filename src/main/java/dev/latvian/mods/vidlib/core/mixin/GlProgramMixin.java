package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.renderpearl.backend.opengl.GlProgram;
import org.spongepowered.asm.mixin.Mixin;

/**
 * 26.3 replaced the named-uniform program API (setupUniforms / Uniform.set(Matrix4f)) with
 * bind-group layouts and uniform buffers (GlProgram.setupBindGroupLayouts). The old
 * PerspectiveMat / InverseWorldMat per-draw injection point no longer exists, so this mixin
 * is kept registered but no longer injects. Matrices are provided through the standard
 * Projection / DynamicTransforms uniform buffers.
 */
@Mixin(GlProgram.class)
public class GlProgramMixin {
}
