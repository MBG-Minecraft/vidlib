package dev.beast.mods.shimmer.math;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

@AutoInit
public enum MovementType implements StringRepresentable {
	ANGLED("angled"),
	CIRCULAR("circular"),
	SPHERICAL("spherical"),
	SQUARE("square"),
	CUBIC("cubic");

	public static final KnownCodec<MovementType> KNOWN_CODEC = KnownCodec.registerEnum(Shimmer.id("movement_type"), values());

	private final String name;

	MovementType(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public Vec3f delta(RandomSource random, float radius, float deviate, Rotation rotation) {
		if (this == SQUARE || this == CUBIC) {
			return new Vec3f(
				Mth.lerp(random.nextFloat(), -radius, radius),
				this == SQUARE ? 0F : Mth.lerp(random.nextFloat(), -radius, radius),
				Mth.lerp(random.nextFloat(), -radius, radius)
			);
		}

		var yaw = this == ANGLED ? rotation.yaw() : random.nextFloat() * 360F;
		var pitch = this == ANGLED ? rotation.pitch() : this == SPHERICAL ? (random.nextFloat() * 180F - 90F) : 0F;

		if (deviate != 0F) {
			yaw += random.nextFloat() * deviate - deviate / 2F;
			pitch += random.nextFloat() * deviate - deviate / 2F;
		}

		return Rotation.deg(yaw, pitch).lookVec3f(radius);
	}
}
