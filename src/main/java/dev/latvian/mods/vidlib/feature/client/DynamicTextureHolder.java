package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.Executor;

public record DynamicTextureHolder<T extends AbstractTexture>(ResourceLocation id, Lazy<T> texture) {
	@AutoInit(AutoInit.Type.TEXTURES_RELOADED)
	public static void init(TextureManager manager, Executor backgroundExecutor, Executor gameExecutor) {
		for (var holder : ClientAutoRegister.SCANNED.get()) {
			if (holder.value() instanceof DynamicTextureHolder<?> h) {
				h.texture.forget();
				manager.register(h.id, h.texture.get());
			}
		}
	}
}
