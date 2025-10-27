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
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@AutoInit
public record VLSkin(
	ResourceLocation texture,
	boolean slim,
	Optional<ResourceLocation> capeTexture,
	Optional<ResourceLocation> elytraTexture
) {

	public static final Codec<VLSkin> CODEC =
		RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC
				.fieldOf("texture")
				.forGetter(VLSkin::texture),
			Codec.BOOL
				.fieldOf("slim")
				.forGetter(VLSkin::slim),
			ResourceLocation.CODEC
				.optionalFieldOf("cape")
				.forGetter(VLSkin::capeTexture),
			ResourceLocation.CODEC
				.optionalFieldOf("elytra")
				.forGetter(VLSkin::elytraTexture)
		).apply(instance, VLSkin::new));

	public static final StreamCodec<ByteBuf, VLSkin> STREAM_CODEC =
		CompositeStreamCodec.of(
			ResourceLocation.STREAM_CODEC, VLSkin::texture,
			ByteBufCodecs.BOOL, VLSkin::slim,
			ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), VLSkin::capeTexture,
			ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), VLSkin::elytraTexture,
			VLSkin::new
		);

	public static final DataType<VLSkin> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, VLSkin.class);

	@Override
	public @NotNull String toString() {
		return "PlayerSkin[" +
			"texture=" + texture +
			", slim=" + slim +
			", capeTexture=" + capeTexture +
			", elytraTexture=" + elytraTexture +
			']';
	}
}
