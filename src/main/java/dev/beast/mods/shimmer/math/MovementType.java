package dev.beast.mods.shimmer.math;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;

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

	public Vec3 delta(RandomSource random, double radius, Rotation rotation) {
		if (this == SQUARE || this == CUBIC) {
			return new Vec3(
				Mth.lerp(random.nextFloat(), -radius, radius),
				this == SQUARE ? 0D : Mth.lerp(random.nextFloat(), -radius, radius),
				Mth.lerp(random.nextFloat(), -radius, radius)
			);
		}

		var yaw = this == ANGLED ? rotation.yaw() : random.nextFloat() * 360F;
		var pitch = this == ANGLED ? rotation.pitch() : this == SPHERICAL ? (random.nextFloat() * 180F - 90F) : 0F;

		return Rotation.deg(yaw, pitch).lookVec3(radius);
	}

	public Vec3f delta3f(RandomSource random, float radius, Rotation rotation) {
		if (this == SQUARE || this == CUBIC) {
			return new Vec3f(
				Mth.lerp(random.nextFloat(), -radius, radius),
				this == SQUARE ? 0F : Mth.lerp(random.nextFloat(), -radius, radius),
				Mth.lerp(random.nextFloat(), -radius, radius)
			);
		}

		var yaw = this == ANGLED ? rotation.yaw() : random.nextFloat() * 360F;
		var pitch = this == ANGLED ? rotation.pitch() : this == SPHERICAL ? (random.nextFloat() * 180F - 90F) : 0F;

		return Rotation.deg(yaw, pitch).lookVec3f(radius);
	}
}
