package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.opengl.Uniform;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(GlProgram.class)
public abstract class GlProgramMixin {
	@Unique
	@Nullable
	private Uniform PERSPECTIVE_MATRIX;

	@Unique
	@Nullable
	private Uniform INVERSE_WORLD_MATRIX;

	@Shadow
	@Nullable
	public abstract Uniform getUniform(String name);

	@Inject(method = "setupUniforms", at = @At("RETURN"))
	private void vl$setupUniforms(List<RenderPipeline.UniformDescription> uniforms, List<String> samplers, CallbackInfo ci) {
		PERSPECTIVE_MATRIX = this.getUniform("PerspectiveMat");
		INVERSE_WORLD_MATRIX = this.getUniform("InverseWorldMat");
	}

	@Inject(method = "setDefaultUniforms", at = @At("RETURN"))
	private void vl$setDefaultUniforms(VertexFormat.Mode mode, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, float screenWidth, float screenHeight, CallbackInfo ci) {
		if (PERSPECTIVE_MATRIX != null) {
			PERSPECTIVE_MATRIX.set(MiscClientUtils.PERSPECTIVE_MATRIX);
		}

		if (INVERSE_WORLD_MATRIX != null) {
			var mat = new Matrix4f();
			mat.mul(projectionMatrix);
			mat.mul(modelViewMatrix);
			mat.invert();
			INVERSE_WORLD_MATRIX.set(mat);
		}
	}
}
