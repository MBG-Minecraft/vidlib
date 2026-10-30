package dev.latvian.mods.vidlib.feature.clothing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.AbstractMap;
import java.util.Map;

@AutoInit
public record ClothingPart(Identifier texture, Ref<Gradient> colors) {
	public static final Codec<ClothingPart> ID_CODEC = ID.CODEC.flatComapMap(texture -> new ClothingPart(texture, Gradient.EMPTY), part -> {
		if (part.colors == Gradient.EMPTY) {
			return DataResult.success(part.texture);
		} else {
			return DataResult.error(() -> "Can't convert ClothingPart with color override to ID");
		}
	});

	public static final Codec<ClothingPart> ENTRY_CODEC = KLibCodecs.mapEntry(ID.CODEC, Gradient.CODEC).xmap(ClothingPart::new, ClothingPart::createEntry);

	public static final Codec<ClothingPart> CODEC = KLibCodecs.or(ID_CODEC, ENTRY_CODEC);

	public static final StreamCodec<ByteBuf, ClothingPart> STREAM_CODEC = CompositeStreamCodec.of(
		ID.STREAM_CODEC, ClothingPart::texture,
		KLibStreamCodecs.optional(Gradient.STREAM_CODEC, Gradient.EMPTY), ClothingPart::colors,
		ClothingPart::new
	);

	public ClothingPart(Map.Entry<Identifier, Ref<Gradient>> entry) {
		this(entry.getKey(), entry.getValue());
	}

	public Map.Entry<Identifier, Ref<Gradient>> createEntry() {
		return new AbstractMap.SimpleEntry<>(texture, colors);
	}
}
