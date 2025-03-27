package dev.beast.mods.shimmer.feature.particle;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public enum ParticleMovementType {
	ANGLED,
	CIRCULAR,
	SQUARE;

	public static final KnownCodec<ParticleMovementType> KNOWN_CODEC = KnownCodec.registerEnum(Shimmer.id("particle_movement_type"), values());

	public Vec3 velocity(RandomSource random, double radius, double yaw) {
		if (this == SQUARE) {
			return new Vec3(
				Mth.lerp(random.nextDouble(), -radius, radius),
				0D,
				Mth.lerp(random.nextDouble(), -radius, radius)
			);
		} else {
			double angle = this == ANGLED ? Math.toRadians(-yaw) : random.nextDouble() * Math.PI * 2D;

			return new Vec3(
				Math.cos(angle) * radius,
				0D,
				Math.sin(angle) * radius
			);
		}
	}
}
