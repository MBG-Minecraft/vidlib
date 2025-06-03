package dev.latvian.mods.vidlib.core;

import net.minecraft.client.particle.SpriteSet;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface VLParticleEngine {
	@Nullable
	default SpriteSet getSpriteSet(ResourceLocation id) {
		return null;
	}
}
