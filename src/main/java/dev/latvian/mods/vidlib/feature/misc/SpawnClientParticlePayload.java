package dev.latvian.mods.vidlib.feature.misc;

import com.mojang.datafixers.util.Either;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Function;

public record SpawnClientParticlePayload(
	List<Either<ParticleOptions, String>> particles,
	Vec3 pos,
	Vec3 spread,
	int spreadType,
	Vec3 velocity,
	Vec3 velocityMultiplierMin,
	Vec3 velocityMultiplierMax,
	int count
) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<SpawnClientParticlePayload> TYPE = VidLibPacketType.internal("spawn_client_particles", CompositeStreamCodec.of(
		KLibStreamCodecs.listOf(ByteBufCodecs.either(ParticleTypes.STREAM_CODEC, ByteBufCodecs.STRING_UTF8)), SpawnClientParticlePayload::particles,
		MCStreamCodecs.VEC3, SpawnClientParticlePayload::pos,
		MCStreamCodecs.VEC3S, SpawnClientParticlePayload::spread,
		ByteBufCodecs.VAR_INT, SpawnClientParticlePayload::spreadType,
		KLibStreamCodecs.optional(MCStreamCodecs.VEC3S, Vec3.ZERO), SpawnClientParticlePayload::velocity,
		KLibStreamCodecs.optional(MCStreamCodecs.VEC3S, KMath.ONE_VEC3), SpawnClientParticlePayload::velocityMultiplierMin,
		KLibStreamCodecs.optional(MCStreamCodecs.VEC3S, KMath.ONE_VEC3), SpawnClientParticlePayload::velocityMultiplierMax,
		ByteBufCodecs.VAR_INT, SpawnClientParticlePayload::count,
		SpawnClientParticlePayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var level = ctx.level();
		var random = level.getRandom();
		var particleList = particles.stream().map(either -> either.map(Function.identity(), string -> ctx.player().vl$sessionData().getParticleOptions(string))).toList();

		var hasSpread = !spread.equals(Vec3.ZERO);

		var hasVM = !velocityMultiplierMin.equals(KMath.ONE_VEC3) || !velocityMultiplierMax.equals(KMath.ONE_VEC3);

		for (int i = 0; i < count; i++) {
			var particle = particleList.size() == 1 ? particleList.getFirst() : particleList.get(random.nextInt(particleList.size()));
			double x = pos.x;
			double y = pos.y;
			double z = pos.z;

			if (hasSpread) {
				// TODO: Spread types
				x += Mth.lerp(random.nextFloat(), -spread.x, spread.x);
				y += Mth.lerp(random.nextFloat(), -spread.y, spread.y);
				z += Mth.lerp(random.nextFloat(), -spread.z, spread.z);
			}

			double vx = velocity.x;
			double vy = velocity.y;
			double vz = velocity.z;

			if (hasVM) {
				vx *= Mth.lerp(random.nextFloat(), velocityMultiplierMin.x, velocityMultiplierMax.x);
				vy *= Mth.lerp(random.nextFloat(), velocityMultiplierMin.y, velocityMultiplierMax.y);
				vz *= Mth.lerp(random.nextFloat(), velocityMultiplierMin.z, velocityMultiplierMax.z);
			}

			level.addParticle(particle, true, true, x, y, z, vx, vy, vz);
		}
	}
}
