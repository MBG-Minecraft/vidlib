package dev.beast.mods.shimmer.math;

public class Vec2d {
	public static final Vec2d ZERO = new Vec2d(0D, 0D);
	public static final Vec2d ONE = new Vec2d(1D, 1D);
	public static final Vec2d UNIT_X = new Vec2d(1D, 0D);
	public static final Vec2d NEG_UNIT_X = new Vec2d(-1D, 0D);
	public static final Vec2d UNIT_Y = new Vec2d(0D, 1D);
	public static final Vec2d NEG_UNIT_Y = new Vec2d(0D, -1D);
	public static final Vec2d MAX = new Vec2d(Double.MAX_VALUE, Double.MAX_VALUE);
	public static final Vec2d MIN = new Vec2d(Double.MIN_VALUE, Double.MIN_VALUE);

	public final double x;
	public final double y;

	public Vec2d(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vec2d scale(double factor) {
		return new Vec2d(this.x * factor, this.y * factor);
	}

	public double dot(Vec2d other) {
		return this.x * other.x + this.y * other.y;
	}

	public Vec2d add(Vec2d other) {
		return new Vec2d(this.x + other.x, this.y + other.y);
	}

	public Vec2d add(float value) {
		return new Vec2d(this.x + value, this.y + value);
	}

	public boolean equals(Vec2d other) {
		return this.x == other.x && this.y == other.y;
	}

	public Vec2d normalized() {
		double f = Math.sqrt(this.x * this.x + this.y * this.y);
		return f < 1.0E-4F ? ZERO : new Vec2d(this.x / f, this.y / f);
	}

	public double length() {
		return Math.sqrt(this.x * this.x + this.y * this.y);
	}

	public double lengthSquared() {
		return this.x * this.x + this.y * this.y;
	}

	public double distanceToSqr(Vec2d other) {
		var dx = other.x - this.x;
		var dy = other.y - this.y;
		return dx * dx + dy * dy;
	}

	public Vec2d negated() {
		return new Vec2d(-this.x, -this.y);
	}

	public Vec2d lerp(Vec2d to, double delta) {
		if (delta == 0D || to == this) {
			return this;
		} else if (delta == 1D) {
			return to;
		} else {
			return new Vec2d(this.x + (to.x - this.x) * delta, this.y + (to.y - this.y) * delta);
		}
	}
}
