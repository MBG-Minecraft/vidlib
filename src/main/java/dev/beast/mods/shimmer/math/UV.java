package dev.beast.mods.shimmer.math;

public record UV(float u0, float v0, float u1, float v1) {
	public static final UV FULL = new UV(0F, 0F, 1F, 1F);

	public UV mul(UV uv) {
		return new UV(
			KMath.lerp(uv.u0, u0, u1),
			KMath.lerp(uv.v0, v0, v1),
			KMath.lerp(uv.u1, u0, u1),
			KMath.lerp(uv.v1, v0, v1)
		);
	}

	@Override
	public String toString() {
		return "[" + u0 + "," + v0 + "," + u1 + "," + v1 + "]";
	}
}
