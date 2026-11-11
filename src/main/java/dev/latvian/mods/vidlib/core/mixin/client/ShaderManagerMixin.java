package dev.latvian.mods.vidlib.core.mixin.client;

import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(targets = "net.minecraft.client.renderer.ShaderManager")
public abstract class ShaderManagerMixin {
	/**
	 * 1.21.5 hooked {@code apply(Configs, ResourceManager, ProfilerFiller)} directly, at the
	 * point where shaders had just been compiled and applied.
	 * <p>
	 * 26.3's reload rework removed that signature: compilation moved into
	 * {@code compilePipelines(...)} and the apply step became
	 * {@code apply(GpuDevice, PendingResults)} - whose {@code PendingResults} is
	 * package-private, so a mixin callback cannot name its parameter type (Mixin requires an
	 * exact match and rejects supertypes such as {@link Record} or {@link Object}). The
	 * equivalent observable point is the completion of the reload future itself, which
	 * {@code apply} runs before. The {@link ResourceManager} also moved into
	 * {@link PreparableReloadListener.SharedState}.
	 */
	@Inject(
		method = "reload(Lnet/minecraft/server/packs/resources/PreparableReloadListener$SharedState;Ljava/util/concurrent/Executor;Lnet/minecraft/server/packs/resources/PreparableReloadListener$PreparationBarrier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;",
		at = @At("RETURN")
	)
	private void vl$reload(PreparableReloadListener.SharedState sharedState, java.util.concurrent.Executor backgroundExecutor, PreparableReloadListener.PreparationBarrier barrier, java.util.concurrent.Executor gameExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		// 26.3 returns the future before `apply` runs, so chain the trigger onto its completion
		// to preserve the "shaders have just been applied" semantics of the 1.21.5 hook.
		cir.getReturnValue().thenRun(() -> AutoInit.Type.SHADERS_LOADED.invoke(sharedState.resourceManager()));
	}
}
