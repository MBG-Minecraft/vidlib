package dev.latvian.mods.vidlib.feature.screeneffect;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.DynamicTextureHolder;
import dev.latvian.mods.vidlib.util.client.DataTexture;

import java.util.List;

public class ScreenEffectTexture extends DataTexture {
	@ClientAutoRegister
	public static final DynamicTextureHolder<ScreenEffectTexture> HOLDER = new DynamicTextureHolder<>(ID.vidlib("effect/screen"), Lazy.of(ScreenEffectTexture::new));

	public ScreenEffectTexture() {
		super("Screen Effect Texture", 8, 4);
	}

	public int update(List<ScreenEffectInstance> instances, float delta) {
		beginUpdate();

		for (var instance : instances) {
			if (instance.shaderType() != ScreenEffectShaderType.NONE) {
				var row = nextRow();
				row.add(instance.shaderType().shaderId); // 0
				instance.upload(row, delta);
			}
		}

		return endUpdate();
	}
}
