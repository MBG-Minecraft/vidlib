package dev.latvian.mods.vidlib.feature.visual;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record FluidTextures(ResourceLocation still, ResourceLocation flowing, TerrainRenderLayer type, Color tint, CubeTextures cubeTextures) {
	public static FluidTextures of(ResourceLocation still, ResourceLocation flowing, TerrainRenderLayer type, Color tint) {
		return new FluidTextures(still, flowing, type, tint, new CubeTextures(
			Optional.of(new FaceTexture(SpriteKey.block(flowing), type, true, tint, 0.5F, 0D)),
			Optional.of(new FaceTexture(SpriteKey.block(still), type, true, tint, 1F, 0D)),
			Optional.of(new FaceTexture(SpriteKey.block(still), type, false, tint, 1F, 0D)),
			Optional.empty(),
			Optional.empty(),
			Optional.empty(),
			Optional.empty()
		));
	}

	public static FluidTextures of(ResourceLocation still, ResourceLocation flowing) {
		return of(still, flowing, TerrainRenderLayer.SOLID, Color.WHITE);
	}

	public static final FluidTextures DEBUG = of(
		VidLib.id("block/debug_fluid/still"),
		VidLib.id("block/debug_fluid/flow")
	);

	public static final FluidTextures WATER = of(
		ResourceLocation.withDefaultNamespace("block/water_still"),
		ResourceLocation.withDefaultNamespace("block/water_flow"),
		TerrainRenderLayer.TRANSLUCENT,
		Color.of(0xFF3F76E4)
	);

	public static final FluidTextures LAVA = of(
		ResourceLocation.withDefaultNamespace("block/lava_still"),
		ResourceLocation.withDefaultNamespace("block/lava_flow")
	);

	public static final FluidTextures OPAQUE_WATER = of(
		VidLib.id("block/opaque_water/still"),
		VidLib.id("block/opaque_water/flow")
	);

	public static final FluidTextures PALE_OPAQUE_WATER = of(
		VidLib.id("block/pale_opaque_water/still"),
		VidLib.id("block/pale_opaque_water/flow")
	);
}
