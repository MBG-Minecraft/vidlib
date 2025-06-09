package dev.latvian.mods.vidlib.feature.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.particle.VidLibParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

import java.util.Optional;

public record NPCParticleOptions(String npc, boolean relativePosition, int extraLifespan, Optional<GameProfile> profile) implements ParticleOptions {
	public static final MapCodec<NPCParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.STRING.fieldOf("npc").forGetter(NPCParticleOptions::npc),
		Codec.BOOL.optionalFieldOf("relative_position", false).forGetter(NPCParticleOptions::relativePosition),
		Codec.INT.optionalFieldOf("extra_lifespan", 0).forGetter(NPCParticleOptions::extraLifespan),
		ExtraCodecs.GAME_PROFILE.optionalFieldOf("profile").forGetter(NPCParticleOptions::profile)
	).apply(instance, NPCParticleOptions::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, NPCParticleOptions> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.STRING_UTF8, NPCParticleOptions::npc,
		ByteBufCodecs.BOOL, NPCParticleOptions::relativePosition,
		ByteBufCodecs.VAR_INT, NPCParticleOptions::extraLifespan,
		ByteBufCodecs.GAME_PROFILE.optional(), NPCParticleOptions::profile,
		NPCParticleOptions::new
	);

	@Override
	public ParticleType<?> getType() {
		return VidLibParticles.NPC.get();
	}
}
