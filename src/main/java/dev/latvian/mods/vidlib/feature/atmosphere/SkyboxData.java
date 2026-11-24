package dev.latvian.mods.vidlib.feature.atmosphere;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.color.Color;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SkyboxData(
	SkyboxTextureId texture,
	float rotation,
	float rotating,
	Color tint
) {
	public static final SkyboxData NONE = new SkyboxData(SkyboxTextureId.NONE, 0F, 0F, Color.WHITE);

	public static SkyboxData of(SkyboxTextureId texture, float rotation, float rotating, Color tint) {
		return texture.equals(SkyboxTextureId.NONE) ? NONE : new SkyboxData(texture, rotation, rotating, tint);
	}

	public static SkyboxData of(SkyboxTextureId texture) {
		return texture.equals(SkyboxTextureId.NONE) ? NONE : new SkyboxData(texture, 0F, 0F, Color.WHITE);
	}

	public static final Codec<SkyboxData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		SkyboxTextureId.CODEC.fieldOf("texture").forGetter(SkyboxData::texture),
		Codec.FLOAT.optionalFieldOf("rotation", 0F).forGetter(SkyboxData::rotation),
		Codec.FLOAT.optionalFieldOf("rotating", 0F).forGetter(SkyboxData::rotating),
		Color.CODEC.optionalFieldOf("tint", Color.WHITE).forGetter(SkyboxData::tint)
	).apply(instance, SkyboxData::of));

	public static final StreamCodec<ByteBuf, SkyboxData> STREAM_CODEC = CompositeStreamCodec.of(
		SkyboxTextureId.STREAM_CODEC, SkyboxData::texture,
		ByteBufCodecs.FLOAT, SkyboxData::rotation,
		ByteBufCodecs.FLOAT, SkyboxData::rotating,
		Color.STREAM_CODEC, SkyboxData::tint,
		SkyboxData::of
	);
}