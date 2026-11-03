package dev.latvian.mods.vidlib.feature.actor;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.vidlib.feature.particle.VidLibParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.players.NameAndId;

import java.util.Optional;

public record ActorParticleOptions(String mocap, boolean relativePosition, int extraLifespan, Optional<NameAndId> profile) implements ParticleOptions {
	public static final MapCodec<ActorParticleOptions> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.STRING.fieldOf("mocap").forGetter(ActorParticleOptions::mocap),
		Codec.BOOL.optionalFieldOf("relative_position", false).forGetter(ActorParticleOptions::relativePosition),
		KLibCodecs.TICKS.optionalFieldOf("extra_lifespan", 0).forGetter(ActorParticleOptions::extraLifespan),
		NameAndId.CODEC.optionalFieldOf("profile").forGetter(ActorParticleOptions::profile)
	).apply(instance, ActorParticleOptions::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ActorParticleOptions> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.STRING_UTF8, ActorParticleOptions::mocap,
		ByteBufCodecs.BOOL, ActorParticleOptions::relativePosition,
		ByteBufCodecs.VAR_INT, ActorParticleOptions::extraLifespan,
		ByteBufCodecs.GAME_PROFILE.map(NameAndId::new, profile -> new GameProfile(profile.id(), profile.name())).apply(ByteBufCodecs::optional), ActorParticleOptions::profile,
		ActorParticleOptions::new
	);

	@Override
	public ParticleType<?> getType() {
		return VidLibParticles.ACTOR.get();
	}
}
