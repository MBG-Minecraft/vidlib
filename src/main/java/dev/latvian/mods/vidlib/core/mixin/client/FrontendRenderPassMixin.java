package dev.latvian.mods.vidlib.core.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.frontend.FrontendRenderPass;
import com.mojang.renderpearl.frontend.FrontendRenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 26.3's frontend validates every draw when running in an IDE ({@code STRICT_VALIDATION}):
 * vertex formats per buffer slot, uniforms supplied through the render pass, depth texture
 * availability, etc. Iris drives many of its passes (shadow pass, center depth sampler, ...)
 * through legacy GL program/sampler state instead of the renderpearl uniform map, so those
 * draws legitimately fail vanilla's checks in a dev environment. Skip validation for
 * pipelines owned by Iris; everything else (vanilla + VidLib) is still fully validated.
 */
@Mixin(FrontendRenderPass.class)
public class FrontendRenderPassMixin {
	@Shadow
	private FrontendRenderPipeline boundPipeline;

	private List<VertexFormat> vl$paddedSource;
	private List<VertexFormat> vl$paddedFormats;

	@Inject(method = "validateDraw", at = @At("HEAD"), cancellable = true)
	private void vl$skipIrisValidation(Collection<String> dynamicUniforms, boolean dynamicVertexBuffer, boolean indexed, CallbackInfo ci) {
		if (this.boundPipeline != null && this.boundPipeline.name().startsWith("iris:")) {
			ci.cancel();
		}
	}

	@ModifyExpressionValue(method = "validateDraw", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/frontend/FrontendRenderPipeline;vertexFormats()Ljava/util/List;"))
	private List<VertexFormat> vl$padVertexFormats(List<VertexFormat> original) {
		if (original.size() >= 16) {
			return original;
		}

		if (this.vl$paddedSource != original) {
			var padded = new ArrayList<>(original);

			while (padded.size() < 16) {
				padded.add(null);
			}

			this.vl$paddedSource = original;
			this.vl$paddedFormats = padded;
		}

		return this.vl$paddedFormats;
	}
}
