package dev.beast.mods.shimmer.math;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record Rotation(float yaw, float pitch, float roll, Type type) {
	public enum Type {
		DEG(180D / Math.PI, 360D),
		RAD(Math.PI / 180D, Math.PI * 2D);

		public final double scaled;
		public final float scalef;
		public final double fulld;
		public final float fullf;
		public final Rotation none;

		Type(double scaled, double fulld) {
			this.scaled = scaled;
			this.scalef = (float) scaled;
			this.fulld = fulld;
			this.fullf = (float) fulld;
			this.none = new Rotation(0F, 0F, 0F, this);
		}

		public Rotation of(float yaw, float pitch, float roll) {
			return yaw == 0F && pitch == 0F && roll == 0F ? none : new Rotation(yaw, pitch, roll, this);
		}

		public Rotation of(float yaw, float pitch) {
			return yaw == 0F && pitch == 0F ? none : new Rotation(yaw, pitch, 0F, this);
		}

		public Rotation of(float yaw) {
			return yaw == 0F ? none : new Rotation(yaw, 0F, 0F, this);
		}
	}

	public static final float RAD = (float) (Math.PI / 180D);
	public static final float DEG = (float) (180D / Math.PI);

	public static final Codec<Rotation> CODEC = Codec.either(Codec.FLOAT, Codec.FLOAT.listOf()).xmap(
		either -> either.map(Rotation::deg, list -> switch (list.size()) {
			case 1 -> deg(list.get(0));
			case 2 -> deg(list.get(0), list.get(1));
			case 3 -> deg(list.get(0), list.get(1), list.get(2));
			default -> throw new IllegalArgumentException("Invalid Rotation list size: " + list.size());
		}),
		r -> {
			if (r.pitch == 0F && r.roll == 0F) {
				return Either.left(r.yawDeg());
			} else if (r.roll == 0F) {
				return Either.right(List.of(r.yawDeg(), r.pitchDeg()));
			} else {
				return Either.right(List.of(r.yawDeg(), r.pitchDeg(), r.rollDeg()));
			}
		}
	);

	public static final StreamCodec<ByteBuf, Rotation> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.FLOAT, Rotation::yawDeg,
		ByteBufCodecs.FLOAT, Rotation::pitchDeg,
		ByteBufCodecs.FLOAT, Rotation::rollDeg,
		Rotation::deg
	);

	public static final StreamCodec<ByteBuf, Rotation> STREAM_CODEC_NO_ROLL = CompositeStreamCodec.of(
		ByteBufCodecs.FLOAT, Rotation::yawDeg,
		ByteBufCodecs.FLOAT, Rotation::pitchDeg,
		Rotation::deg
	);

	public static final Rotation NONE = new Rotation(0F, 0F, 0F, Type.RAD);

	public static Rotation deg(float yaw, float pitch, float roll) {
		return yaw == 0F && pitch == 0F && roll == 0F ? NONE : new Rotation(yaw, pitch, roll, Type.DEG);
	}

	public static Rotation deg(float yaw, float pitch) {
		return yaw == 0F && pitch == 0F ? NONE : new Rotation(yaw, pitch, 0F, Type.DEG);
	}

	public static Rotation deg(float yaw) {
		return yaw == 0F ? NONE : new Rotation(yaw, 0F, 0F, Type.DEG);
	}

	public static Rotation rad(float yaw, float pitch, float roll) {
		return yaw == 0F && pitch == 0F && roll == 0F ? NONE : new Rotation(yaw, pitch, roll, Type.RAD);
	}

	public static Rotation rad(float yaw, float pitch) {
		return yaw == 0F && pitch == 0F ? NONE : new Rotation(yaw, pitch, 0F, Type.RAD);
	}

	public static Rotation rad(float yaw) {
		return yaw == 0F ? NONE : new Rotation(yaw, 0F, 0F, Type.RAD);
	}

	public static Rotation compute(Vec3 source, Vec3 target, float roll) {
		double dx = target.x() - source.x();
		double dy = target.y() - source.y();
		double dz = target.z() - source.z();

		double yaw = Math.atan2(dz, dx) - Math.PI / 2D;
		double pitch = -Math.atan2(dy, Math.sqrt(dx * dx + dz * dz));

		return rad((float) yaw, (float) pitch, roll);
	}

	public static Rotation compute(Vec3 source, Vec3 target) {
		return compute(source, target, 0F);
	}

	public float yawDeg() {
		return type == Type.DEG ? yaw : yaw * DEG;
	}

	public float pitchDeg() {
		return type == Type.DEG ? pitch : pitch * DEG;
	}

	public float rollDeg() {
		return type == Type.DEG ? roll : roll * DEG;
	}

	public float yawRad() {
		return type == Type.RAD ? yaw : yaw * RAD;
	}

	public float pitchRad() {
		return type == Type.RAD ? pitch : pitch * RAD;
	}

	public float rollRad() {
		return type == Type.RAD ? roll : roll * RAD;
	}

	public Rotation toDeg() {
		return this == NONE || type == Type.DEG ? this : new Rotation(yaw * DEG, pitch * DEG, roll * DEG, Type.DEG);
	}

	public Rotation toRad() {
		return this == NONE || type == Type.RAD ? this : new Rotation(yaw * RAD, pitch * RAD, roll * RAD, Type.RAD);
	}

	public Rotation lerp(float delta, Rotation to) {
		if (type == to.type) {
			return type.of(
				KMath.lerp(delta, yaw, to.yaw),
				KMath.lerp(delta, pitch, to.pitch),
				KMath.lerp(delta, roll, to.roll)
			);
		} else {
			return deg(
				KMath.lerp(delta, yawDeg(), to.yawDeg()),
				KMath.lerp(delta, pitchDeg(), to.pitchDeg()),
				KMath.lerp(delta, rollDeg(), to.rollDeg())
			);
		}
	}

	public Vec3 lookVec3(double dist) {
		float p = pitchRad();
		float y = -yawRad();
		float yc = Mth.cos(y);
		float ys = Mth.sin(y);
		float pc = Mth.cos(p);
		float ps = Mth.sin(p);
		return new Vec3(ys * pc * dist, -ps * dist, yc * pc * dist);
	}

	public Vec3f lookVec3f(float dist) {
		float p = pitchRad();
		float y = -yawRad();
		float yc = Mth.cos(y);
		float ys = Mth.sin(y);
		float pc = Mth.cos(p);
		float ps = Mth.sin(p);
		return new Vec3f(ys * pc * dist, -ps * dist, yc * pc * dist);
	}

	@Override
	public String toString() {
		if (roll == 0F) {
			if (pitch == 0F) {
				return "Rotation[" + yawDeg() + "]";
			}

			return "Rotation[" + yawDeg() + ", " + pitchDeg() + "]";
		}

		return "Rotation[" + yawDeg() + ", " + pitchDeg() + ", " + rollDeg() + "]";
	}
}
