package dev.latvian.mods.vidlib.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record CubeTextures(
	Optional<FaceTexture> all,
	Optional<FaceTexture> down,
	Optional<FaceTexture> up,
	Optional<FaceTexture> north,
	Optional<FaceTexture> south,
	Optional<FaceTexture> west,
	Optional<FaceTexture> east
) {
	public static final CubeTextures EMPTY = new CubeTextures(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

	public static final Codec<CubeTextures> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		FaceTexture.CODEC.optionalFieldOf("all").forGetter(CubeTextures::all),
		FaceTexture.CODEC.optionalFieldOf("down").forGetter(CubeTextures::down),
		FaceTexture.CODEC.optionalFieldOf("up").forGetter(CubeTextures::up),
		FaceTexture.CODEC.optionalFieldOf("north").forGetter(CubeTextures::north),
		FaceTexture.CODEC.optionalFieldOf("south").forGetter(CubeTextures::south),
		FaceTexture.CODEC.optionalFieldOf("west").forGetter(CubeTextures::west),
		FaceTexture.CODEC.optionalFieldOf("east").forGetter(CubeTextures::east)
	).apply(instance, CubeTextures::new));

	public static final StreamCodec<ByteBuf, CubeTextures> STREAM_CODEC = CompositeStreamCodec.of(
		FaceTexture.OPTIONAL_STREAM_CODEC, CubeTextures::all,
		FaceTexture.OPTIONAL_STREAM_CODEC, CubeTextures::down,
		FaceTexture.OPTIONAL_STREAM_CODEC, CubeTextures::up,
		FaceTexture.OPTIONAL_STREAM_CODEC, CubeTextures::north,
		FaceTexture.OPTIONAL_STREAM_CODEC, CubeTextures::south,
		FaceTexture.OPTIONAL_STREAM_CODEC, CubeTextures::west,
		FaceTexture.OPTIONAL_STREAM_CODEC, CubeTextures::east,
		CubeTextures::new
	);

	public static final StreamCodec<ByteBuf, Optional<CubeTextures>> OPTIONAL_STREAM_CODEC = STREAM_CODEC.optional();

	public static CubeTextures fluid(ResourceLocation still, ResourceLocation flowing, TerrainRenderLayer type, Color tint) {
		return new CubeTextures(
			Optional.of(new FaceTexture(SpriteKey.block(flowing), type, true, tint, 0.5F)),
			Optional.of(new FaceTexture(SpriteKey.block(still), type, true, tint, 1F)),
			Optional.of(new FaceTexture(SpriteKey.block(still), type, false, tint, 1F)),
			Optional.empty(),
			Optional.empty(),
			Optional.empty(),
			Optional.empty()
		);
	}

	public static CubeTextures fluid(ResourceLocation still, ResourceLocation flowing) {
		return fluid(still, flowing, TerrainRenderLayer.SOLID, Color.WHITE);
	}

	public static final CubeTextures WATER = fluid(
		ResourceLocation.withDefaultNamespace("block/water_still"),
		ResourceLocation.withDefaultNamespace("block/water_flow"),
		TerrainRenderLayer.TRANSLUCENT,
		Color.of(0xFF3F76E4)
	);

	public static final CubeTextures LAVA = fluid(
		ResourceLocation.withDefaultNamespace("block/lava_still"),
		ResourceLocation.withDefaultNamespace("block/lava_flow")
	);

	public static final CubeTextures OPAQUE_WATER = fluid(
		VidLib.id("block/opaque_water_still"),
		VidLib.id("block/opaque_water_flow")
	);

	public static final CubeTextures PALE_OPAQUE_WATER = fluid(
		VidLib.id("block/pale_opaque_water_still"),
		VidLib.id("block/pale_opaque_water_flow")
	);
}
