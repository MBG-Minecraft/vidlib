package dev.latvian.mods.vidlib.feature.atmosphere;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.color.Color;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SkyboxTextureData(
	ClientAsset.ResourceTexture assetId,
	float rotation,
	float rotating,
	Color tint
) {
	public static final Codec<SkyboxTextureData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ClientAsset.ResourceTexture.DEFAULT_FIELD_CODEC.forGetter(SkyboxTextureData::assetId),
		Codec.FLOAT.optionalFieldOf("rotation", 0F).forGetter(SkyboxTextureData::rotation),
		Codec.FLOAT.optionalFieldOf("rotating", 0F).forGetter(SkyboxTextureData::rotating),
		Color.CODEC.optionalFieldOf("tint", Color.WHITE).forGetter(SkyboxTextureData::tint)
	).apply(instance, SkyboxTextureData::new));

	public static final StreamCodec<ByteBuf, SkyboxTextureData> STREAM_CODEC = CompositeStreamCodec.of(
		ClientAsset.ResourceTexture.STREAM_CODEC, SkyboxTextureData::assetId,
		ByteBufCodecs.FLOAT, SkyboxTextureData::rotation,
		ByteBufCodecs.FLOAT, SkyboxTextureData::rotating,
		Color.STREAM_CODEC, SkyboxTextureData::tint,
		SkyboxTextureData::new
	);
}