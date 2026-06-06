package dev.latvian.mods.vidlib.feature.clothing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.codec.VLCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

@AutoInit
public record ClothingPart(ResourceLocation texture, Gradient colors) {
	public static final Codec<ClothingPart> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ID.CODEC.fieldOf("texture").forGetter(ClothingPart::texture),
		VLCodecs.TRANSPARENT_OR_GRADIENT_CODEC.optionalFieldOf("colors", Color.TRANSPARENT).forGetter(ClothingPart::colors)
	).apply(instance, ClothingPart::new));

	public static final Codec<ClothingPart> ID_CODEC = ID.CODEC.flatComapMap(texture -> new ClothingPart(texture, Color.TRANSPARENT), part -> {
		if (part.colors == Color.TRANSPARENT) {
			return DataResult.success(part.texture);
		} else {
			return DataResult.error(() -> "Can't convert ClothingPart with color override to ID");
		}
	});

	public static final Codec<ClothingPart> CODEC = KLibCodecs.or(ID_CODEC, DIRECT_CODEC);

	public static final StreamCodec<ByteBuf, ClothingPart> STREAM_CODEC = CompositeStreamCodec.of(
		ID.STREAM_CODEC, ClothingPart::texture,
		KLibStreamCodecs.optional(Gradient.STREAM_CODEC, Color.TRANSPARENT), ClothingPart::colors,
		ClothingPart::new
	);

	public ClothingPart(Map.Entry<ResourceLocation, Gradient> entry) {
		this(entry.getKey(), entry.getValue());
	}
}
