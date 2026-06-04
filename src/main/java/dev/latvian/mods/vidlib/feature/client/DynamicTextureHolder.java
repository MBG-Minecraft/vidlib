package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.auto.TextureReloadParams;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

public record DynamicTextureHolder<T extends AbstractTexture>(ResourceLocation id, Lazy<T> texture) {
	@AutoInit(AutoInit.Type.TEXTURES_RELOADED)
	public static void init(TextureReloadParams params) {
		for (var holder : ClientAutoRegister.SCANNED.get()) {
			if (holder.value() instanceof DynamicTextureHolder<?> h) {
				h.texture.forget();
				params.manager().register(h.id, h.texture.get());
			}
		}
	}
}
