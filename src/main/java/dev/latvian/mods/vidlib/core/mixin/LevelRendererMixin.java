package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.feature.skybox.SkyboxRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.level.border.WorldBorder;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/**
 * 26.3 rebuilt {@link LevelRenderer} around the frame graph, the extract/render-state split and
 * MultiDrawIndirect terrain:
 * <ul>
 *     <li>{@code renderLevel} became
 *         {@code render(GraphicsResourceAllocator, boolean, CameraRenderState, GpuBufferSlice, Vector4f, boolean, boolean)}</li>
 *     <li>{@code addWeatherPass} and the {@code DimensionSpecialEffects.renderSky} call are gone —
 *         the sky pass is now {@link #addSkyPass}, weather/world border moved into the main pass and
 *         into {@code net.minecraft.client.renderer.extract.LevelExtractor}</li>
 *     <li>{@code renderEntities}, {@code setSectionDirty}, {@code SectionBuffers},
 *         {@code renderSectionLayer}, {@code allChanged}, {@code doEntityOutline} and
 *         {@code visibleEntities} were all removed by the section-mesh / render-state architecture</li>
 *     <li>{@code net.minecraft.client.renderer.fog.FogData} no longer exists — fog data is carried
 *         by {@code net.minecraft.client.renderer.fog.FogData} and uploaded to a UBO slice</li>
 * </ul>
 */
@Mixin(value = LevelRenderer.class, priority = 1001) // Load before Sodium
public abstract class LevelRendererMixin {
	@Shadow
	@Final
	private LevelTargetBundle targets;

	/**
	 * The world border rendered for the current frame. 26.3 extracts the world border render state
	 * inside {@code LevelExtractor.extract(...)} (through {@code WorldBorderRenderer.extract(...)}),
	 * which is no longer reachable from {@link LevelRenderer}, so the override is computed here and
	 * stashed instead of being redirected at the old {@code lambda$addWeatherPass$7} call site.
	 */
	@Unique
	private WorldBorder vl$renderedWorldBorder = null;

	/**
	 * Head of the level render. 26.3 uploads {@code cameraState.fogData} into the fog UBO before
	 * entering {@code LevelRenderer.render(...)}, so a VidLib fog override has to be applied both to
	 * the render state (which drives the sky pass and the main-target clear colour) and to the
	 * already-uploaded {@link GpuBufferSlice}.
	 */
	@Inject(method = "render", at = @At("HEAD"))
	private void vl$renderLevel(GraphicsResourceAllocator resourceAllocator, boolean renderOutline, CameraRenderState cameraState, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, boolean consistentDepthRequired, CallbackInfo ci) {
		ClientGameEngine.applyFog(ClientGameEngine.INSTANCE.getShaderFog(cameraState.fogData), cameraState.fogData, terrainFog);

		var mc = Minecraft.getInstance();
		vl$renderedWorldBorder = mc.level == null ? null : ClientGameEngine.INSTANCE.getRenderedWorldBorder(mc, mc.level);
	}

	/**
	 * The world border actually rendered for this level.
	 */
	@Nullable
	public WorldBorder vl$getRenderedWorldBorder() {
		return vl$renderedWorldBorder;
	}

	/**
	 * Replaces the vanilla sky pass with the VidLib skybox when one is active. 1.21.5 wrapped
	 * {@code DimensionSpecialEffects.renderSky(...)}; 26.3 removed that method, so the seam is now
	 * the {@code FramePass#executes} call inside {@code addSkyPass}.
	 */
	@WrapOperation(method = "addSkyPass", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FramePass;executes(Ljava/lang/Runnable;)V"))
	private void vl$renderSkybox(FramePass instance, Runnable task, Operation<Void> operation, @Local(argsOnly = true) GpuBufferSlice skyFog) {
		var mc = Minecraft.getInstance();
		var skybox = mc.player == null ? null : mc.player.vl$sessionData().skybox;

		if (skybox == null) {
			operation.call(instance, task);
			return;
		}

		Runnable skyboxTask = () -> {
			if (skybox.data.celestials()) {
				task.run();
			}

			SkyboxRenderer.render(mc, skybox, () -> RenderSystem.setShaderFog(skyFog));
		};

		operation.call(instance, skyboxTask);
	}

	/**
	 * {@code LevelRenderer.allChanged()} was removed in 26.3; the closest hook is
	 * {@code invalidateCompiledGeometry}, which runs whenever the compiled chunk geometry has to be
	 * rebuilt from scratch.
	 */
	@Inject(method = "invalidateCompiledGeometry", at = @At("RETURN"))
	private void vl$allChanged(ClientLevel level, net.minecraft.client.Options options, net.minecraft.client.Camera camera, net.minecraft.client.color.block.BlockColors blockColors, CallbackInfo ci) {
		var mc = Minecraft.getInstance();

		if (mc.level != null) {
			AutoInit.Type.CHUNKS_RENDERED.invoke(mc.level);
		}
	}

	/**
	 * Replaces NeoForge 1.21.5's {@code RenderHighlightEvent.Block}, which no longer exists in 26.3.
	 * The block outline is submitted through {@code submitBlockOutline}, which is the only place a
	 * hook can still suppress it.
	 */
	@Inject(method = "submitBlockOutline", at = @At("HEAD"), cancellable = true)
	private void vl$submitBlockOutline(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LevelRenderState levelRenderState, CallbackInfo ci) {
		net.minecraft.client.renderer.state.level.BlockOutlineRenderState state = levelRenderState.blockOutlineRenderState;

		if (state != null && dev.latvian.mods.vidlib.VidLibClientEventHandler.renderBlockHighlight(state.pos())) {
			ci.cancel();
		}
	}

	@Inject(method = "addMainPass", at = @At("HEAD"))
	private void vl$copyMainDepth(FrameGraphBuilder frame, FeatureRenderDispatcher.PreparedFrame featureFrame, GpuBufferSlice terrainFog, ChunkSectionsToRender chunkSectionsToRender, boolean consistentDepthRequired, CallbackInfo ci) {
		ClientGameEngine.INSTANCE.copyMainDepth(Minecraft.getInstance());
	}

	@Inject(method = "addMainPass", at = @At("RETURN"))
	private void vl$copyOutlineDepth(FrameGraphBuilder frame, FeatureRenderDispatcher.PreparedFrame featureFrame, GpuBufferSlice terrainFog, ChunkSectionsToRender chunkSectionsToRender, boolean consistentDepthRequired, CallbackInfo ci) {
		ClientGameEngine.INSTANCE.copyOutlineDepth(Minecraft.getInstance());
	}
}
