package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.klib.gl.GLDebugLog;
import dev.latvian.mods.vidlib.VidLibConfig;
import dev.latvian.mods.vidlib.core.VLOutlineBufferSource;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import dev.latvian.mods.vidlib.feature.canvas.CanvasImpl;
import dev.latvian.mods.vidlib.feature.skybox.SkyboxRenderer;
import dev.latvian.mods.vidlib.util.client.VLViewArea;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(value = LevelRenderer.class, priority = 1001) // Load before Sodium
public abstract class LevelRendererMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	@Final
	private LevelTargetBundle targets;

	@Shadow
	public abstract boolean shouldShowEntityOutlines();

	@Shadow
	@Nullable
	private ClientLevel level;

	@Inject(method = "allChanged", at = @At("RETURN"))
	private void vl$allChanged(CallbackInfo ci) {
		if (level != null) {
			AutoInit.Type.CHUNKS_RENDERED.invoke(level);
		}
	}

	@Redirect(method = "allChanged", at = @At(value = "NEW", target = "(Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher;Lnet/minecraft/world/level/Level;ILnet/minecraft/client/renderer/LevelRenderer;)Lnet/minecraft/client/renderer/ViewArea;"))
	private ViewArea vl$customViewArea(SectionRenderDispatcher dispatcher, Level level, int viewDistance, LevelRenderer levelRenderer) {
		if (VidLibConfig.robert) {
			return new VLViewArea(dispatcher, level, viewDistance, levelRenderer);
		} else {
			return new ViewArea(dispatcher, level, viewDistance, levelRenderer);
		}
	}

	@Redirect(method = "lambda$addSkyPass$13", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;renderSky(Lnet/minecraft/client/multiplayer/ClientLevel;IFLorg/joml/Matrix4f;Lnet/minecraft/client/Camera;Lorg/joml/Matrix4f;Ljava/lang/Runnable;)Z"))
	private boolean vl$renderSkybox(DimensionSpecialEffects instance, ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, Runnable setupFog) {
		var skybox = minecraft.player != null ? minecraft.player.vl$sessionData().skybox : null;

		if (skybox != null) {
			return SkyboxRenderer.render(minecraft, skybox, setupFog);
		} else {
			return instance.renderSky(level, ticks, partialTick, modelViewMatrix, camera, projectionMatrix, setupFog);
		}
	}

	/**
	 * @author Lat
	 * @reason Yeet
	 */
	@Overwrite
	private boolean shouldRenderDarkDisc(float partialTick) {
		return false;
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

	@Inject(method = "addWeatherPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/world/phys/Vec3;FLnet/minecraft/client/renderer/FogParameters;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/Camera;)V", at = @At("RETURN"))
	private void vl$addMainPass(FrameGraphBuilder frameGraphBuilder, Vec3 cameraPosition, float partialTick, FogParameters fog, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, Camera camera, CallbackInfo ci) {
		CanvasImpl.addAllToFrame(minecraft, frameGraphBuilder, targets);
	}

	@Redirect(method = "addMainPass", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FramePass;readsAndWrites(Lcom/mojang/blaze3d/resource/ResourceHandle;)Lcom/mojang/blaze3d/resource/ResourceHandle;", ordinal = 4))
	private ResourceHandle<RenderTarget> vl$cancelOutline(FramePass instance, ResourceHandle<RenderTarget> original) {
		return original;
	}

	@Inject(method = "addMainPass", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LevelTargetBundle;main:Lcom/mojang/blaze3d/resource/ResourceHandle;", ordinal = 2))
	private void vl$addMainPassReadsAndWrites(FrameGraphBuilder frameGraphBuilder, Frustum frustum, Camera camera, Matrix4f frustumMatrix, Matrix4f projectionMatrix, FogParameters fogParameters, boolean renderBlockOutline, boolean renderEntityOutline, DeltaTracker deltaTracker, ProfilerFiller profiler, CallbackInfo ci, @Local FramePass framePass) {
		CanvasImpl.allReadsAndWrites(framePass);
	}

	@Inject(method = "lambda$addMainPass$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;shouldShowEntityOutlines()Z"))
	private void vl$copyMainDepth(FogParameters fogParameters, DeltaTracker deltaTracker, Camera camera, ProfilerFiller profiler, Matrix4f frustumMatrix, Matrix4f projectionMatrix, ResourceHandle<RenderTarget> itemEntity, ResourceHandle<RenderTarget> entityOutline, Frustum frustum, boolean renderBlockOutline, ResourceHandle<RenderTarget> translucent, ResourceHandle<RenderTarget> main, CallbackInfo ci) {
		// Canvas.MAIN.clone(main.get(), true, true);
	}

	@Inject(method = "lambda$addMainPass$2", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;<init>()V"))
	private void vl$copyOutlineDepth(FogParameters fogParameters, DeltaTracker deltaTracker, Camera camera, ProfilerFiller profiler, Matrix4f frustumMatrix, Matrix4f projectionMatrix, ResourceHandle<RenderTarget> itemEntity, ResourceHandle<RenderTarget> entityOutline, Frustum frustum, boolean renderBlockOutline, ResourceHandle<RenderTarget> translucent, ResourceHandle<RenderTarget> main, CallbackInfo ci) {
		if (VidLibConfig.entityOutlineDepth && shouldShowEntityOutlines()) {
			CanvasImpl.copyEntityOutlineDepth(minecraft, minecraft.getMainRenderTarget());
		}
	}

	@Inject(method = "renderEntities", at = @At("HEAD"))
	private void vl$renderEntitiesHead(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Camera camera, DeltaTracker deltaTracker, List<Entity> entities, CallbackInfo ci) {
		GLDebugLog.pushGroup("Render Entities");
	}

	@Inject(method = "renderEntities", at = @At("RETURN"))
	private void vl$renderEntitiesReturn(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Camera camera, DeltaTracker deltaTracker, List<Entity> entities, CallbackInfo ci) {
		GLDebugLog.popGroup();
	}

	@Inject(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"))
	private void vl$renderEntitiesRender(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Camera camera, DeltaTracker deltaTracker, List<Entity> entities, CallbackInfo ci, @Local Entity entity) {
		GLDebugLog.message(entity.getScoreboardName() + " [" + entity.getType().getDescription().getString() + "]");
	}

	@ModifyExpressionValue(method = {
		"setupRender",
		"applyFrustum",
		"renderLevel",
		"scheduleTranslucentSectionResort",
		"renderSectionLayer",
		"compileSections",
	}, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/Profiler;get()Lnet/minecraft/util/profiling/ProfilerFiller;"))
	private ProfilerFiller vl$getProfiler(ProfilerFiller profiler) {
		return GLDebugLog.PROFILER;
	}

	/**
	 * @author Lat
	 * @reason Yeet
	 */
	@Overwrite
	@Nullable
	public RenderTarget entityOutlineTarget() {
		return Canvas.ENTITY_OUTLINE.getTargetOrNull();
	}

	@Inject(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;setColor(IIII)V"))
	private void vl$renderEntitiesSetColor(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Camera camera, DeltaTracker deltaTracker, List<Entity> entities, CallbackInfo ci, @Local OutlineBufferSource outlineBuffer, @Local Entity entity) {
		((VLOutlineBufferSource) outlineBuffer).vl$setPlayer(entity instanceof Player);
	}
}
