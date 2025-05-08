package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.texture.EphemeralTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
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
	protected abstract void safeClose(ResourceLocation path, AbstractTexture texture);

	@Shadow
	@Final
	public Map<ResourceLocation, AbstractTexture> byPath;

	@Inject(method = "reload", at = @At("HEAD"))
	public void vl$reload(PreparableReloadListener.PreparationBarrier barrier, ResourceManager manager, Executor executor1, Executor executor2, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		var list = new ArrayList<Map.Entry<ResourceLocation, AbstractTexture>>();

		for (var entry : byPath.entrySet()) {
			if (entry.getValue() instanceof EphemeralTexture) {
				list.add(entry);
			}
		}

		for (var entry : list) {
			safeClose(entry.getKey(), entry.getValue());
		}

		AutoInit.Type.TEXTURES_RELOADED.invoke(this);
	}
}
