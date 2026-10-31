package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLParticleEngine;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin implements VLParticleEngine {
	@Shadow
	@Final
	private ParticleResources resourceManager;

	@Override
	@Nullable
	public SpriteSet vl$getSpriteSet(Identifier id) {
		return resourceManager.spriteSets.get(id);
	}
}
