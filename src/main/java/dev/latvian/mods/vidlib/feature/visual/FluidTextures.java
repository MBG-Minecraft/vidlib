package dev.latvian.mods.vidlib.feature.visual;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.texture.CubeTexturePatch;
import dev.latvian.mods.klib.texture.FaceTexture;
import dev.latvian.mods.klib.texture.SpriteKey;
import dev.latvian.mods.klib.util.ID;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record FluidTextures(Identifier still, Identifier flowing, Color tint, CubeTexturePatch cubeTextures) {
	public static FluidTextures of(Identifier still, Identifier flowing, Color tint) {
		return new FluidTextures(still, flowing, tint, new CubeTexturePatch(
			Optional.of(new FaceTexture(SpriteKey.block(flowing), true, tint, 0.5F, 0D)),
			Optional.of(new FaceTexture(SpriteKey.block(still), true, tint, 1F, 0D)),
			Optional.of(new FaceTexture(SpriteKey.block(still), false, tint, 1F, 0D)),
			Optional.empty(),
			Optional.empty(),
			Optional.empty(),
			Optional.empty()
		));
	}

	public static FluidTextures of(Identifier still, Identifier flowing) {
		return of(still, flowing, Color.WHITE);
	}

	public static final FluidTextures DEBUG = of(
		ID.vidlib("block/debug_fluid/still"),
		ID.vidlib("block/debug_fluid/flow")
	);

	public static final FluidTextures WATER = of(
		ID.mc("block/water_still"),
		ID.mc("block/water_flow"),
		Color.of(0xFF3F76E4)
	);

	public static final FluidTextures LAVA = of(
		ID.mc("block/lava_still"),
		ID.mc("block/lava_flow")
	);

	public static final FluidTextures OPAQUE_WATER = of(
		ID.vidlib("block/opaque_water/still"),
		ID.vidlib("block/opaque_water/flow")
	);

	public static final FluidTextures PALE_OPAQUE_WATER = of(
		ID.vidlib("block/pale_opaque_water/still"),
		ID.vidlib("block/pale_opaque_water/flow")
	);
}
