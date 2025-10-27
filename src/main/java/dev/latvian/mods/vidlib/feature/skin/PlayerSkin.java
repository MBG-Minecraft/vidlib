package dev.latvian.mods.vidlib.feature.skin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

@AutoInit
public record PlayerSkin(
	ResourceLocation texture,
	boolean slim,
	Optional<ResourceLocation> capeTexture,
	Optional<ResourceLocation> elytraTexture
) {

	public static final Codec<PlayerSkin> CODEC =
		RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC
				.fieldOf("texture")
				.forGetter(PlayerSkin::texture),
			Codec.BOOL
				.fieldOf("slim")
				.forGetter(PlayerSkin::slim),
			ResourceLocation.CODEC
				.optionalFieldOf("cape")
				.forGetter(PlayerSkin::capeTexture),
			ResourceLocation.CODEC
				.optionalFieldOf("elytra")
				.forGetter(PlayerSkin::elytraTexture)
		).apply(instance, PlayerSkin::new));

	public static final StreamCodec<ByteBuf, PlayerSkin> STREAM_CODEC =
		CompositeStreamCodec.of(
			ResourceLocation.STREAM_CODEC, PlayerSkin::texture,
			ByteBufCodecs.BOOL, PlayerSkin::slim,
			ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), PlayerSkin::capeTexture,
			ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), PlayerSkin::elytraTexture,
			PlayerSkin::new
		);

	public static final DataType<PlayerSkin> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, PlayerSkin.class);

}
