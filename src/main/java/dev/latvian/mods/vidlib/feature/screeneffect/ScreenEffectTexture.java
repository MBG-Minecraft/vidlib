package dev.latvian.mods.vidlib.feature.screeneffect;

import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.DynamicTextureHolder;
import dev.latvian.mods.vidlib.util.client.DataTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ScreenEffectTexture extends DataTexture {
	public static final ResourceLocation ID = VidLib.id("textures/effect/screen.png");

	@ClientAutoRegister
	public static final DynamicTextureHolder<ScreenEffectTexture> HOLDER = new DynamicTextureHolder<>(ID, Lazy.of(ScreenEffectTexture::new));

	public ScreenEffectTexture() {
		super("Screen Effect Texture", 16, 4);
	}

	public void update(List<ScreenEffectInstance> instances, float delta) {
		beginUpdate();

		for (var instance : instances) {
			var row = nextRow();
			row.add(instance.shaderType().shaderId);
			instance.upload(row, delta);
		}

		endUpdate();
	}
}
