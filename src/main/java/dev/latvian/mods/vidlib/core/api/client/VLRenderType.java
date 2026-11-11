package dev.latvian.mods.vidlib.core.api.client;

import dev.latvian.mods.klib.util.Empty;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Map;

public interface VLRenderType {
	@Nullable
	default Identifier vl$getTexture() {
		try {
			Field stateField = this.getClass().getDeclaredField("state");
			stateField.setAccessible(true);
			Object setup = stateField.get(this);
			Field texturesField = setup.getClass().getDeclaredField("textures");
			texturesField.setAccessible(true);
			Map<?, ?> textures = (Map<?, ?>) texturesField.get(setup);

			for (var entry : textures.entrySet()) {
				if ("Sampler0".equals(entry.getKey())) {
					return textureBindingLocation(entry.getValue());
				}
			}

			for (var entry : textures.entrySet()) {
				return textureBindingLocation(entry.getValue());
			}
		} catch (Throwable ignored) {
		}

		return null;
	}

	@Nullable
	private static Identifier textureBindingLocation(Object binding) {
		try {
			Field locationField = binding.getClass().getDeclaredField("location");
			locationField.setAccessible(true);
			return (Identifier) locationField.get(binding);
		} catch (Throwable ignored) {
			return null;
		}
	}

	default Identifier vl$getTextureSafe() {
		var tex = vl$getTexture();
		return tex == null ? Empty.TEXTURE.texturePath() : tex;
	}
}
