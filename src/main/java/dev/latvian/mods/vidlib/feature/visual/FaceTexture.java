package dev.latvian.mods.vidlib.feature.visual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record FaceTexture(SpriteKey sprite, TerrainRenderLayer layer, boolean cull, Color tint, float uvScale, double fade) {
	public static final FaceTexture EMPTY = new FaceTexture(SpriteKey.EMPTY, TerrainRenderLayer.SOLID, true);
	public static final FaceTexture WHITE = new FaceTexture(SpriteKey.WHITE, TerrainRenderLayer.SOLID, true);
	public static final FaceTexture BLOOM = new FaceTexture(SpriteKey.WHITE, TerrainRenderLayer.BLOOM, true);

	public static final Codec<FaceTexture> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		SpriteKey.CODEC.fieldOf("sprite").forGetter(FaceTexture::sprite),
		TerrainRenderLayer.CODEC.optionalFieldOf("layer", TerrainRenderLayer.SOLID).forGetter(FaceTexture::layer),
		Codec.BOOL.optionalFieldOf("cull", true).forGetter(FaceTexture::cull),
		Color.CODEC.optionalFieldOf("tint", Color.WHITE).forGetter(FaceTexture::tint),
		Codec.FLOAT.optionalFieldOf("uv_scale", 1F).forGetter(FaceTexture::uvScale),
		Codec.DOUBLE.optionalFieldOf("fade", 0D).forGetter(FaceTexture::fade)
	).apply(instance, FaceTexture::new));

	public static final StreamCodec<ByteBuf, FaceTexture> STREAM_CODEC = CompositeStreamCodec.of(
		SpriteKey.STREAM_CODEC, FaceTexture::sprite,
		TerrainRenderLayer.STREAM_CODEC, FaceTexture::layer,
		ByteBufCodecs.BOOL, FaceTexture::cull,
		Color.STREAM_CODEC, FaceTexture::tint,
		KLibStreamCodecs.optional(ByteBufCodecs.FLOAT, 1F), FaceTexture::uvScale,
		KLibStreamCodecs.optional(ByteBufCodecs.DOUBLE, 0D), FaceTexture::fade,
		FaceTexture::new
	);

	public static final StreamCodec<ByteBuf, Optional<FaceTexture>> OPTIONAL_STREAM_CODEC = ByteBufCodecs.optional(STREAM_CODEC);

	public FaceTexture(SpriteKey sprite, TerrainRenderLayer layer, boolean cull) {
		this(sprite, layer, cull, Color.WHITE, 1F, 0D);
	}

	public FaceTexture merge(FaceTexture other) {
		return other == EMPTY ? this : other;
	}

	public FaceTexture withTint(Color tint) {
		return new FaceTexture(sprite, layer, cull, tint, uvScale, fade);
	}
}
