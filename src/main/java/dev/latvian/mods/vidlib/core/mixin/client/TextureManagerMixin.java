package dev.latvian.mods.vidlib.core.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.auto.TextureReloadParams;
import dev.latvian.mods.vidlib.feature.client.DynamicTextureHolder;
import dev.latvian.mods.vidlib.feature.visual.EphemeralTexture;
import dev.latvian.mods.vidlib.util.client.ReloadableDynamicTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(TextureManager.class)
public abstract class TextureManagerMixin {
	@Shadow
	protected abstract void safeClose(Identifier path, AbstractTexture texture);

	@Shadow
	@Final
	public Map<Identifier, AbstractTexture> byPath;

	@Shadow
	public abstract void register(Identifier location, AbstractTexture texture);

	@Shadow
	@Final
	private ResourceManager resourceManager;

	@Inject(method = "reload", at = @At("HEAD"))
	public void vl$reloadPre(PreparableReloadListener.SharedState sharedState, Executor backgroundExecutor, PreparableReloadListener.PreparationBarrier barrier, Executor gameExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		var list = new ArrayList<Map.Entry<Identifier, AbstractTexture>>();

		for (var entry : byPath.entrySet()) {
			if (entry.getValue() instanceof EphemeralTexture) {
				list.add(entry);
			}
		}

		for (var entry : list) {
			safeClose(entry.getKey(), entry.getValue());
		}

		AutoInit.Type.TEXTURES_RELOADED.invoke(new TextureReloadParams((TextureManager) (Object) this, backgroundExecutor, gameExecutor));
	}

	@ModifyReturnValue(method = "reload", at = @At("RETURN"))
	private CompletableFuture<Void> vl$reloadPost(CompletableFuture<Void> original) {
		return original.thenRun(() -> {
			var reloadable = new ArrayList<ReloadableDynamicTexture>();

			for (var holder : ClientAutoRegister.SCANNED.get()) {
				if (holder.value() instanceof DynamicTextureHolder<?> h) {
					h.texture().forget();
					register(h.asset().texturePath(), h.texture().get());

					if (h.texture().get() instanceof ReloadableDynamicTexture t) {
						reloadable.add(t);
					}
				}
			}

			for (var t : reloadable) {
				t.load(resourceManager, (TextureManager) (Object) this);
			}
		});
	}
}
