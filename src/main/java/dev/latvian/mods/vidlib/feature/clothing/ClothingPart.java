package dev.latvian.mods.vidlib.feature.clothing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
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

import java.util.AbstractMap;
import java.util.Map;

@AutoInit
public record ClothingPart(ResourceLocation texture, Gradient colors) {
	public static final Codec<ClothingPart> ID_CODEC = ID.CODEC.flatComapMap(texture -> new ClothingPart(texture, Color.TRANSPARENT), part -> {
		if (part.colors == Color.TRANSPARENT) {
			return DataResult.success(part.texture);
		} else {
			return DataResult.error(() -> "Can't convert ClothingPart with color override to ID");
		}
	});

	public static final Codec<ClothingPart> ENTRY_CODEC = VLCodecs.mapEntry(ID.CODEC, VLCodecs.TRANSPARENT_OR_GRADIENT_CODEC).xmap(ClothingPart::new, ClothingPart::createEntry);

	public static final Codec<ClothingPart> CODEC = KLibCodecs.or(ID_CODEC, ENTRY_CODEC);

	public static final StreamCodec<ByteBuf, ClothingPart> STREAM_CODEC = CompositeStreamCodec.of(
		ID.STREAM_CODEC, ClothingPart::texture,
		KLibStreamCodecs.optional(Gradient.STREAM_CODEC, Color.TRANSPARENT), ClothingPart::colors,
		ClothingPart::new
	);

	public ClothingPart(Map.Entry<ResourceLocation, Gradient> entry) {
		this(entry.getKey(), entry.getValue());
	}

	public Map.Entry<ResourceLocation, Gradient> createEntry() {
		return new AbstractMap.SimpleEntry<>(texture, colors);
	}
}
