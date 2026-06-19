package dev.latvian.mods.vidlib.feature.visual;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record FluidTextures(ClientAsset.ResourceTexture still, ClientAsset.ResourceTexture flowing, TerrainRenderLayer type, Color tint, CubeTextures cubeTextures) {
	public static FluidTextures of(ClientAsset.ResourceTexture still, ClientAsset.ResourceTexture flowing, TerrainRenderLayer type, Color tint) {
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

	public static FluidTextures of(ClientAsset.ResourceTexture still, ClientAsset.ResourceTexture flowing) {
		return of(still, flowing, TerrainRenderLayer.SOLID, Color.WHITE);
	}

	public static final FluidTextures DEBUG = of(
		new ClientAsset.ResourceTexture(VidLib.id("block/debug_fluid/still")),
		new ClientAsset.ResourceTexture(VidLib.id("block/debug_fluid/flow"))
	);

	public static final FluidTextures WATER = of(
		new ClientAsset.ResourceTexture(Identifier.withDefaultNamespace("block/water_still")),
		new ClientAsset.ResourceTexture(Identifier.withDefaultNamespace("block/water_flow")),
		TerrainRenderLayer.TRANSLUCENT,
		Color.of(0xFF3F76E4)
	);

	public static final FluidTextures LAVA = of(
		new ClientAsset.ResourceTexture(Identifier.withDefaultNamespace("block/lava_still")),
		new ClientAsset.ResourceTexture(Identifier.withDefaultNamespace("block/lava_flow"))
	);

	public static final FluidTextures OPAQUE_WATER = of(
		new ClientAsset.ResourceTexture(VidLib.id("block/opaque_water/still")),
		new ClientAsset.ResourceTexture(VidLib.id("block/opaque_water/flow"))
	);

	public static final FluidTextures PALE_OPAQUE_WATER = of(
		new ClientAsset.ResourceTexture(VidLib.id("block/pale_opaque_water/still")),
		new ClientAsset.ResourceTexture(VidLib.id("block/pale_opaque_water/flow"))
	);
}
