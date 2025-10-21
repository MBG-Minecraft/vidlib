package dev.latvian.mods.vidlib.feature.visual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

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

	public static final StreamCodec<ByteBuf, Optional<CubeTextures>> OPTIONAL_STREAM_CODEC = ByteBufCodecs.optional(STREAM_CODEC);
}
