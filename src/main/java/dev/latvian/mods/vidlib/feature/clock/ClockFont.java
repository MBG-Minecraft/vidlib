package dev.latvian.mods.vidlib.feature.clock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.JOMLCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.vidlib.feature.registry.RegistryRef;
import dev.latvian.mods.vidlib.feature.registry.VLRegistry;
import dev.latvian.mods.vidlib.util.JsonRegistryReloadListener;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2i;

import java.util.List;

public record ClockFont(
	ResourceLocation id,
	ResourceLocation texture,
	Vector2i size,
	Vector2i textureSize,
	int separatorWidth,
	int actualSeparatorWidth,
	List<UV> uvs
) {
	public static ClockFont create(ResourceLocation id,
								   ResourceLocation texture,
								   Vector2i size,
								   Vector2i textureSize,
								   int separatorWidth
	) {
		var uvs = new UV[11];
		int actualSeparatorWidth = separatorWidth > 0 ? separatorWidth : size.x();
		float woff = size.x() + 1F;

		for (int i = 0; i < 11; i++) {
			float w = i == 10 ? actualSeparatorWidth : size.x();
			float h = size.y();
			float tw = textureSize.x();
			float th = textureSize.y();

			uvs[i] = new UV(
				(i * woff) / tw,
				0F,
				((i * woff) + w) / tw,
				h / th
			);
		}

		return new ClockFont(id, texture, size, textureSize, separatorWidth, actualSeparatorWidth, List.of(uvs));
	}

	public static final Codec<ClockFont> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ResourceLocation.CODEC.fieldOf("id").forGetter(ClockFont::id),
		ResourceLocation.CODEC.fieldOf("texture").forGetter(ClockFont::texture),
		JOMLCodecs.IVEC2S.fieldOf("size").forGetter(ClockFont::size),
		JOMLCodecs.IVEC2S.fieldOf("texture_size").forGetter(ClockFont::textureSize),
		Codec.INT.optionalFieldOf("separator_width", 0).forGetter(ClockFont::separatorWidth)
	).apply(instance, ClockFont::create));

	public static final VLRegistry<ClockFont> REGISTRY = VLRegistry.createClient("clock_font", ClockFont.class);

	public static final DataType<RegistryRef<ClockFont>> REF_DATA_TYPE = REGISTRY.refDataType();

	public static class Loader extends JsonRegistryReloadListener<ClockFont> {
		public Loader() {
			super("vidlib/clock_font", DIRECT_CODEC, true, REGISTRY);
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
				w += size.x() + 1;
			}
		}

		return w;
	}
}
