package dev.latvian.mods.vidlib.feature.atmosphere;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import dev.latvian.mods.vidlib.core.api.client.VLSkyRenderState;
import dev.latvian.mods.vidlib.feature.client.ChromaKeyData;
import dev.latvian.mods.vidlib.feature.client.VLBuffers;
import dev.latvian.mods.vidlib.feature.client.VLRenderPipelines;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Map;
import java.util.function.Function;

public class SkyboxRenderer {
	public static final Map<SkyboxTextureId, Function<Minecraft, AbstractTexture>> CUSTOM_TEXTURES = new Object2ObjectOpenHashMap<>(1);

	public static boolean render(RenderPass renderPass, SkyRenderState skyState) {
		var vl = ((VLSkyRenderState) skyState).vl$state();

		if (vl.skyboxTexture == null) {
			return false;
		}

		int indexCount = vl.skyboxEquirectangular ? (VLBuffers.EQUIRECTANGULAR_QUAD_COUNT * 6) : 36;

		var autoIndices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
		var indexBuffer = autoIndices.getBuffer(indexCount);
		var modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		modelViewStack.rotate(vl.skyboxRotation);
		var dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(new Matrix4f(modelViewStack), vl.skyboxTint);
		renderPass.pushDebugGroup(() -> "VidLib Skybox");
		renderPass.setPipeline(RenderSystem.getCompiledPipeline(vl.skyboxChromaKey.w > 0F ? VLRenderPipelines.SKYBOX_WITH_CHROMA_KEY : VLRenderPipelines.SKYBOX));
		RenderSystem.bindDefaultUniforms(renderPass);
		renderPass.setUniform("DynamicTransforms", dynamicTransforms);
		renderPass.setUniform("Sampler0", vl.skyboxTexture.getTextureView(), vl.skyboxTexture.getSampler());

		if (vl.skyboxChromaKey.w > 0F) {
			float Y = 0.299F * vl.skyboxChromaKey.x + 0.587F * vl.skyboxChromaKey.y + 0.114F * vl.skyboxChromaKey.z;

			renderPass.setUniform("ChromaKey", VLBuffers.chromaKey.writeData(new ChromaKeyData(
				new Vector4f(vl.skyboxChromaKey),
				new Vector2f((vl.skyboxChromaKey.z - Y) * 0.565F, (vl.skyboxChromaKey.x - Y) * 0.713F),
				new Vector2f(vl.skyboxChromaKeyRange.min(), vl.skyboxChromaKeyRange.max())
			)));
		}

		renderPass.setVertexBuffer(0, (vl.skyboxEquirectangular ? VLBuffers.equirectangular : VLBuffers.cubeMap3x2).slice());
		renderPass.setIndexBuffer(indexBuffer, autoIndices.type());
		renderPass.drawIndexed(indexCount, 1, 0, 0, 0);
		renderPass.popDebugGroup();
		modelViewStack.popMatrix();
		return false;
	}
}
