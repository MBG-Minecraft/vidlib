package dev.beast.mods.shimmer.feature.explosion;

public record DestroyedClientBlock(DestroyedBlock block, float red, float green, float blue, float alpha) {
	public DestroyedClientBlock(DestroyedBlock block, int tint) {
		this(block,
			(tint >> 16 & 0xFF) / 255F,
			(tint >> 8 & 0xFF) / 255F,
			(tint & 0xFF) / 255F,
			(tint >> 24 & 0xFF) / 255F
		);
	}
}
