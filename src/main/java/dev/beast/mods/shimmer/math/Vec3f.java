package dev.beast.mods.shimmer.math;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

import java.util.List;
import java.util.function.Function;

public record Vec3f(float x, float y, float z) {
	public static final Vec3f ZERO = new Vec3f(0F, 0F, 0F);
	public static final Vec3f DOWN = new Vec3f(0F, -1F, 0F);
	public static final Vec3f UP = new Vec3f(0F, 1F, 0F);
	public static final Vec3f NORTH = new Vec3f(0F, 0F, -1F);
	public static final Vec3f SOUTH = new Vec3f(0F, 0F, 1F);
	public static final Vec3f WEST = new Vec3f(-1F, 0F, 0F);
	public static final Vec3f EAST = new Vec3f(1F, 0F, 0F);

	public static final Vec3f[] DIRECTIONS = {DOWN, UP, NORTH, SOUTH, WEST, EAST};

	public static Vec3f of(float x, float y, float z) {
		if (x == 0F && y == -1F && z == 0F) {
			return DOWN;
		} else if (x == 0F && y == 1F && z == 0F) {
			return UP;
		} else if (x == 0F && y == 0F && z == -1F) {
			return NORTH;
		} else if (x == 0F && y == 0F && z == 1F) {
			return SOUTH;
		} else if (x == -1F && y == 0F && z == 0F) {
			return WEST;
		} else if (x == 1F && y == 0F && z == 0F) {
			return EAST;
		} else {
			return new Vec3f(x, y, z);
		}
	}

	public static Vec3f of(double x, double y, double z) {
		return of((float) x, (float) y, (float) z);
	}

	public static Vec3f of(Vec3 v) {
		return of((float) v.x, (float) v.y, (float) v.z);
	}

	public static final Codec<Vec3f> DIRECT_CODEC = Codec.FLOAT.listOf(3, 3).xmap(f -> of(f.get(0), f.get(1), f.get(2)), v -> List.of(v.x, v.y, v.z));

	public static final Codec<Vec3f> CODEC = Codec.either(Direction.CODEC, DIRECT_CODEC).xmap(either -> either.map(dir -> DIRECTIONS[dir.get3DDataValue()], Function.identity()), v -> {
		if (v.x == 0F && v.y == -1F && v.z == 0F) {
			return Either.left(Direction.DOWN);
		} else if (v.x == 0F && v.y == 1F && v.z == 0F) {
			return Either.left(Direction.UP);
		} else if (v.x == 0F && v.y == 0F && v.z == -1F) {
			return Either.left(Direction.NORTH);
		} else if (v.x == 0F && v.y == 0F && v.z == 1F) {
			return Either.left(Direction.SOUTH);
		} else if (v.x == -1F && v.y == 0F && v.z == 0F) {
			return Either.left(Direction.WEST);
		} else if (v.x == 1F && v.y == 0F && v.z == 0F) {
			return Either.left(Direction.EAST);
		} else {
			return Either.right(v);
		}
	});

	public static final StreamCodec<ByteBuf, Vec3f> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.FLOAT, Vec3f::x,
		ByteBufCodecs.FLOAT, Vec3f::y,
		ByteBufCodecs.FLOAT, Vec3f::z,
		Vec3f::new
	);

	public float lengthSq() {
		return x * x + y * y + z * z;
	}

	public float length() {
		return (float) Math.sqrt(lengthSq());
	}

	public Vec3f normalize() {
		float ls = lengthSq();

		if (ls == 0F || ls == 1F) {
			return this;
		}

		float l = (float) Math.sqrt(length());
		return of(x / l, y / l, z / l);
	}

	public Vector4f toVec4f() {
		return new Vector4f(x, y, z, 1F);
	}

	public Rotation toRotation() {
		double yaw = Math.atan2(z, x) - Math.PI / 2D;
		double pitch = -Math.atan2(y, Math.sqrt(x * x + z * z));
		return Rotation.rad((float) yaw, (float) pitch, 0F);
	}

	public Vec3f withX(float x) {
		return of(x, y, z);
	}

	public Vec3f withY(float y) {
		return of(x, y, z);
	}

	public Vec3f withZ(float z) {
		return of(x, y, z);
	}
}
