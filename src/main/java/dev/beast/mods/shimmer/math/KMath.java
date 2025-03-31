package dev.beast.mods.shimmer.math;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

@SuppressWarnings("ManualMinMaxCalculation")
public interface KMath {
	enum NumberFormat {
		VERY_SHORT("#.#"),
		SHORT("#.##"),
		LONG("#.####");

		private final DecimalFormat format;

		NumberFormat(String format) {
			this.format = new DecimalFormat(format);
			this.format.setRoundingMode(RoundingMode.FLOOR);
		}

		public String format(float value) {
			if (value == (int) value) {
				return Integer.toString((int) value);
			}

			return format.format(value);
		}

		public String format(double value) {
			if (value == (long) value) {
				return Long.toString((long) value);
			}

			return format.format(value);
		}

		public String format(Vec3 pos) {
			return String.format("%s, %s, %s", format(pos.x), format(pos.y), format(pos.z));
		}

		public String format(Vec3f pos) {
			return String.format("%s, %s, %s", format(pos.x()), format(pos.y()), format(pos.z()));
		}
	}

	List<AABB> CLIP_BOX_LIST = List.of(new AABB(-0.5D, -0.5D, -0.5D, 0.5D, 0.5D, 0.5D));
	Vec3 CENTER = new Vec3(0.5D, 0.5D, 0.5D);

	static String veryShortFormat(float value) {
		return NumberFormat.VERY_SHORT.format(value);
	}

	static String veryShortFormat(double value) {
		return NumberFormat.VERY_SHORT.format(value);
	}

	static String shortFormat(float value) {
		return NumberFormat.SHORT.format(value);
	}

	static String shortFormat(double value) {
		return NumberFormat.SHORT.format(value);
	}

	static String format(float value) {
		return NumberFormat.LONG.format(value);
	}

	static String format(double value) {
		return NumberFormat.LONG.format(value);
	}

	static String formatBlockPos(BlockPos pos) {
		return String.format("%d, %d, %d", pos.getX(), pos.getY(), pos.getZ());
	}

	static String formatVec3(Vec3 pos) {
		return NumberFormat.LONG.format(pos);
	}

	static NumericTag efficient(float num) {
		var i = (int) num;

		if (i == num) {
			if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) {
				return ByteTag.valueOf((byte) i);
			}

			if (i >= Short.MIN_VALUE && i <= Short.MAX_VALUE) {
				return ShortTag.valueOf((short) i);
			}

			return IntTag.valueOf(i);
		} else {
			return FloatTag.valueOf(num);
		}
	}

	static double sq(double t) {
		return t * t;
	}

	static float sq(float t) {
		return t * t;
	}

	static int floor(double value) {
		int i = (int) value;
		return value < i ? i - 1 : i;
	}

	static long lfloor(double value) {
		long i = (long) value;
		return value < i ? i - 1L : i;
	}

	static int floor(float value) {
		int i = (int) value;
		return value < i ? i - 1 : i;
	}

	static int ceil(double value) {
		int i = (int) value;
		return value > i ? i + 1 : i;
	}

	static long lceil(double value) {
		long i = (long) value;
		return value > i ? i + 1L : i;
	}

	static int ceil(float value) {
		int i = (int) value;
		return value > i ? i + 1 : i;
	}

	static double clamp(double value, double min, double max) {
		return value < min ? min : value <= max ? value : max;
	}

	static float clamp(float value, float min, float max) {
		return value < min ? min : value <= max ? value : max;
	}

	static int clamp(int value, int min, int max) {
		return value < min ? min : value <= max ? value : max;
	}

	static double lerp(double delta, double min, double max) {
		return min + delta * (max - min);
	}

	static float lerp(float delta, float min, float max) {
		return min + delta * (max - min);
	}

	static double clerp(double value, double min, double max) {
		return value < 0D ? min : value > 1D ? max : (min + value * (max - min));
	}

	static float clerp(float value, float min, float max) {
		return value < 0F ? min : value > 1F ? max : (min + value * (max - min));
	}

	static double map(double value, double min0, double max0, double min1, double max1) {
		return min1 + (max1 - min1) * ((value - min0) / (max0 - min0));
	}

	static float map(float value, float min0, float max0, float min1, float max1) {
		return min1 + (max1 - min1) * ((value - min0) / (max0 - min0));
	}

	static double smoothstep(double t) {
		return t * t * (3D - 2D * t);
	}

	static float smoothstep(float t) {
		return t * t * (3F - 2F * t);
	}

	static double ismoothstep(double t) {
		return t + (t - (t * t * (3D - 2D * t)));
	}

	static float ismoothstep(float t) {
		return t + (t - (t * t * (3F - 2F * t)));
	}

	static double smootherstep(double t) {
		return t * t * t * (t * (t * 6D - 15D) + 10D);
	}

	static float smootherstep(float t) {
		return t * t * t * (t * (t * 6F - 15F) + 10F);
	}

	static double curve(double t, double p1x, double p2x, double p3x) {
		double t1 = 1D - t;
		double v = -(p1x - 8D * p2x + p3x) / 6D;
		return t1 * t1 * t1 * p1x + 3D * t * t1 * t1 * v + 3D * t * t * t1 * v + t * t * t * p3x;
	}

	static double min8(double a, double b, double c, double d, double e, double f, double g, double h) {
		return Math.min(Math.min(Math.min(Math.min(Math.min(Math.min(a, b), c), d), e), f), Math.min(g, h));
	}

	static double max8(double a, double b, double c, double d, double e, double f, double g, double h) {
		return Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(a, b), c), d), e), f), Math.max(g, h));
	}

	static float min8(float a, float b, float c, float d, float e, float f, float g, float h) {
		return Math.min(Math.min(Math.min(Math.min(Math.min(Math.min(a, b), c), d), e), f), Math.min(g, h));
	}

	static float max8(float a, float b, float c, float d, float e, float f, float g, float h) {
		return Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(a, b), c), d), e), f), Math.max(g, h));
	}

	static BlockPos min(BlockPos a, BlockPos b) {
		return new BlockPos(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
	}

	static BlockPos max(BlockPos a, BlockPos b) {
		return new BlockPos(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
	}
}