package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import dev.latvian.mods.vidlib.feature.canvas.CanvasImpl;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.border.WorldBorder;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(value = LevelRenderer.class, priority = 1001) // Load before Sodium
public abstract class LevelRendererMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	@Final
	private LevelTargetBundle targets;

	@Shadow
	@Nullable
	private ClientLevel level;

	@Inject(method = "allChanged", at = @At("RETURN"))
	private void vl$allChanged(CallbackInfo ci) {
		if (level != null) {
			AutoInit.Type.CHUNKS_RENDERED.invoke(level);
		}
	}

	@Inject(method = "doEntityOutline", at = @At("HEAD"))
	private void vl$doEntityOutlineBefore(CallbackInfo ci) {
		CanvasImpl.drawAllBeforeOutline(minecraft);
	}

	@Inject(method = "doEntityOutline", at = @At("RETURN"))
	private void vl$doEntityOutlineAfter(CallbackInfo ci) {
		CanvasImpl.drawAllAfterOutline(minecraft);
	}

	@Inject(method = "onResourceManagerReload", at = @At("RETURN"))
	private void vl$initOutline(ResourceManager manager, CallbackInfo ci) {
		CanvasImpl.initAll(minecraft, manager);
	}

	@Inject(method = "close", at = @At("RETURN"))
	private void vl$close(CallbackInfo ci) {
		CanvasImpl.closeAll();
	}

	@Inject(method = "resize", at = @At("RETURN"))
	private void vl$resize(int width, int height, CallbackInfo ci) {
		CanvasImpl.resizeAll(width, height);
	}

	@Inject(method = "addWeatherPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Matrix4fc;)V", at = @At("RETURN"))
	private void vl$addMainPass(FrameGraphBuilder frameGraphBuilder, com.mojang.blaze3d.buffers.GpuBufferSlice fog, Matrix4fc modelViewMatrix, CallbackInfo ci) {
		CanvasImpl.addAllToFrame(minecraft, frameGraphBuilder, targets, false);
	}

	@Redirect(method = "addMainPass", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FramePass;readsAndWrites(Lcom/mojang/blaze3d/resource/ResourceHandle;)Lcom/mojang/blaze3d/resource/ResourceHandle;", ordinal = 5))
	private ResourceHandle<RenderTarget> vl$cancelOutline(FramePass instance, ResourceHandle<RenderTarget> original) {
		return original;
	}

	@Inject(method = "addMainPass", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LevelTargetBundle;main:Lcom/mojang/blaze3d/resource/ResourceHandle;", ordinal = 2))
	private void vl$addMainPassReadsAndWrites(FrameGraphBuilder frameGraphBuilder, Frustum frustum, Matrix4fc modelViewMatrix, com.mojang.blaze3d.buffers.GpuBufferSlice fog, boolean renderBlockOutline, LevelRenderState levelRenderState, DeltaTracker deltaTracker, ProfilerFiller profiler, net.minecraft.client.renderer.chunk.ChunkSectionsToRender chunkSectionsToRender, CallbackInfo ci, @Local FramePass framePass) {
		CanvasImpl.allReadsAndWrites(framePass);
	}

	@Inject(method = "lambda$addMainPass$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;shouldShowEntityOutlines()Z"))
	private void vl$copyMainDepth(GpuBufferSlice terrainFog, LevelRenderState levelRenderState, ProfilerFiller profiler, ChunkSectionsToRender chunkSectionsToRender, Matrix4fc modelViewMatrix, ResourceHandle<RenderTarget> mainTarget, ResourceHandle<RenderTarget> translucentTarget, ResourceHandle<RenderTarget> itemEntityTarget, ResourceHandle<RenderTarget> entityOutlineTarget, ResourceHandle<RenderTarget> particleTarget, boolean renderBlockOutline, CallbackInfo ci) {
		ClientGameEngine.INSTANCE.copyMainDepth(minecraft);
	}

	@Inject(method = "lambda$addMainPass$0", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;<init>()V"))
	private void vl$copyOutlineDepth(GpuBufferSlice terrainFog, LevelRenderState levelRenderState, ProfilerFiller profiler, ChunkSectionsToRender chunkSectionsToRender, Matrix4fc modelViewMatrix, ResourceHandle<RenderTarget> mainTarget, ResourceHandle<RenderTarget> translucentTarget, ResourceHandle<RenderTarget> itemEntityTarget, ResourceHandle<RenderTarget> entityOutlineTarget, ResourceHandle<RenderTarget> particleTarget, boolean renderBlockOutline, CallbackInfo ci) {
		ClientGameEngine.INSTANCE.copyOutlineDepth(minecraft);
	}

	/**
	 * @author Lat
	 * @reason Yeet
	 */
	@Overwrite
	@Nullable
	public RenderTarget entityOutlineTarget() {
		return Canvas.WEAK_OUTLINE.getTargetOrNull();
	}

	@ModifyExpressionValue(method = "extractVisibleEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;extractEntity(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;"))
	private EntityRenderState vl$updateEntityOutlineColor(EntityRenderState state, @Local Entity entity) {
		var color = ClientGameEngine.INSTANCE.getTeamColor(minecraft, entity);

		if (color != null && state.outlineColor != EntityRenderState.NO_OUTLINE) {
			state.outlineColor = ARGB.opaque(color.rgb());
		}

		return state;
	}

	@Redirect(method = "extractLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getWorldBorder()Lnet/minecraft/world/level/border/WorldBorder;"))
	private WorldBorder vl$getWorldBorder(ClientLevel level) {
		return ClientGameEngine.INSTANCE.getRenderedWorldBorder(minecraft, level);
	}
}
