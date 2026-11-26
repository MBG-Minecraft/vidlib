package dev.latvian.mods.vidlib.feature.atmosphere;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.math.Range;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public record SkyboxData(
	SkyboxTextureId texture,
	float rotation,
	float rotating,
	Color tint,
	boolean equirectangular,
	Color chromaKey,
	Range chromaKeyRange
) {
	public static final Range DEFAULT_CHROMA_KEY_RANGE = Range.of(0.11F, 0.22F);
	public static final Color GREEN_SCREEN = Color.of(0xFF0DA124);

	public static final SkyboxData NONE = new SkyboxData(SkyboxTextureId.NONE, 0F, 0F, Color.WHITE, false, Color.TRANSPARENT, DEFAULT_CHROMA_KEY_RANGE);

	public static SkyboxData of(SkyboxTextureId texture, float rotation, float rotating, Color tint, boolean equirectangular, Color chromaKey, Range chromaKeyRange) {
		return texture.equals(SkyboxTextureId.NONE) ? NONE : new SkyboxData(texture, rotation, rotating, tint, equirectangular, chromaKey, chromaKeyRange);
	}

	public static SkyboxData of(SkyboxTextureId texture) {
		return texture.equals(SkyboxTextureId.NONE) ? NONE : new SkyboxData(texture, 0F, 0F, Color.WHITE, false, Color.TRANSPARENT, DEFAULT_CHROMA_KEY_RANGE);
	}

	public static final Codec<SkyboxData> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		SkyboxTextureId.CODEC.fieldOf("texture").forGetter(SkyboxData::texture),
		Codec.FLOAT.optionalFieldOf("rotation", 0F).forGetter(SkyboxData::rotation),
		Codec.FLOAT.optionalFieldOf("rotating", 0F).forGetter(SkyboxData::rotating),
		Color.CODEC.optionalFieldOf("tint", Color.WHITE).forGetter(SkyboxData::tint),
		Codec.BOOL.optionalFieldOf("equirectangular", false).forGetter(SkyboxData::equirectangular),
		Color.CODEC.optionalFieldOf("chroma_key", Color.TRANSPARENT).forGetter(SkyboxData::chromaKey),
		Range.CODEC.optionalFieldOf("chroma_key_range", DEFAULT_CHROMA_KEY_RANGE).forGetter(SkyboxData::chromaKeyRange)
	).apply(instance, SkyboxData::of));

	public static final Codec<SkyboxData> CODEC = Codec.either(DIRECT_CODEC, SkyboxTextureId.CODEC).xmap(either -> either.map(Function.identity(), SkyboxData::of), Either::left);

	public static final StreamCodec<ByteBuf, SkyboxData> STREAM_CODEC = CompositeStreamCodec.of(
		SkyboxTextureId.STREAM_CODEC, SkyboxData::texture,
		ByteBufCodecs.FLOAT, SkyboxData::rotation,
		ByteBufCodecs.FLOAT, SkyboxData::rotating,
		Color.STREAM_CODEC, SkyboxData::tint,
		ByteBufCodecs.BOOL, SkyboxData::equirectangular,
		Color.STREAM_CODEC, SkyboxData::chromaKey,
		Range.STREAM_CODEC, SkyboxData::chromaKeyRange,
		SkyboxData::of
	);

	@Nullable
	public SkyboxData resolve() {
		return texture.equals(SkyboxTextureId.NONE) ? null : this;
	}
}