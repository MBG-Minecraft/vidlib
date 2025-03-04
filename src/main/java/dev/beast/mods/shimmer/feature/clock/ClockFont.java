package dev.beast.mods.shimmer.feature.clock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.math.Size2;
import dev.beast.mods.shimmer.math.UV;
import dev.beast.mods.shimmer.util.JsonCodecReloadListener;
import dev.beast.mods.shimmer.util.ShimmerCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.function.Supplier;

public record ClockFont(
	ResourceLocation id,
	ResourceLocation texture,
	Size2 size,
	Size2 textureSize,
	int separatorWidth,
	int actualSeparatorWidth,
	UV[] uvs
) {
	public static ClockFont create(ResourceLocation id,
								   ResourceLocation texture,
								   Size2 size,
								   Size2 textureSize,
								   int separatorWidth
	) {
		var uvs = new UV[11];
		int actualSeparatorWidth = separatorWidth > 0 ? separatorWidth : size.w();
		float woff = size.w() + 1F;

		for (int i = 0; i < 11; i++) {
			float w = i == 10 ? actualSeparatorWidth : size.w();
			float h = size.h();
			float tw = textureSize.w();
			float th = textureSize.h();

			uvs[i] = new UV(
				(i * woff) / tw,
				0F,
				((i * woff) + w) / tw,
				h / th
			);
		}

		return new ClockFont(id, texture, size, textureSize, separatorWidth, actualSeparatorWidth, uvs);
	}

	public static final Codec<ClockFont> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ResourceLocation.CODEC.fieldOf("id").forGetter(ClockFont::id),
		ResourceLocation.CODEC.fieldOf("texture").forGetter(ClockFont::texture),
		Size2.CODEC.fieldOf("size").forGetter(ClockFont::size),
		Size2.CODEC.fieldOf("texture_size").forGetter(ClockFont::textureSize),
		Codec.INT.optionalFieldOf("separator_width", 0).forGetter(ClockFont::separatorWidth)
	).apply(instance, ClockFont::create));

	public static final StreamCodec<ByteBuf, ClockFont> DIRECT_STREAM_CODEC = StreamCodec.composite(
		ResourceLocation.STREAM_CODEC,
		ClockFont::id,
		ResourceLocation.STREAM_CODEC,
		ClockFont::texture,
		Size2.STREAM_CODEC,
		ClockFont::size,
		Size2.STREAM_CODEC,
		ClockFont::textureSize,
		ByteBufCodecs.VAR_INT,
		ClockFont::separatorWidth,
		ClockFont::create
	);

	public static Map<ResourceLocation, ClockFont> SERVER = Map.of();
	public static Supplier<Map<ResourceLocation, ClockFont>> CLIENT_SUPPLIER = Map::of;

	public static final Codec<ClockFont> CODEC = ShimmerCodecs.map(() -> SERVER, ShimmerCodecs.SHIMMER_ID, ClockFont::id);

	public static class Loader extends JsonCodecReloadListener<ClockFont> {
		public Loader() {
			super("shimmer/clock_font", DIRECT_CODEC, true);
		}

		@Override
		protected void apply(Map<ResourceLocation, ClockFont> from) {
			SERVER = Map.copyOf(from);
		}
	}

	public int getWidth(char[] string) {
		if (string.length == 0) {
			return 0;
		}

		int w = 1;

		for (char c : string) {
			if (c == ':') {
				w += separatorWidth + 1;
			} else {
				w += size.w() + 1;
			}
		}

		return w;
	}
}
