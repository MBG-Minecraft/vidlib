package dev.latvian.mods.vidlib.core;

import net.minecraft.client.particle.SpriteSet;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public interface VLParticleEngine {
	@Nullable
	default SpriteSet vl$getSpriteSet(Identifier id) {
		return null;
	}
}
