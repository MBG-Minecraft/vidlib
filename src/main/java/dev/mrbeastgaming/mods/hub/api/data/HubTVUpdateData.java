package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public sealed interface HubTVUpdateData {
	static MapCodec<? extends HubTVUpdateData> typeOf(String type) {
		return switch (type) {
			case "text" -> Text.MAP_CODEC;
			case "image" -> Image.MAP_CODEC;
			default -> throw new IllegalArgumentException("Invalid type: " + type);
		};
	}

	Codec<HubTVUpdateData> CODEC = Codec.STRING.dispatch("type", HubTVUpdateData::type, HubTVUpdateData::typeOf);

	String type();

	record Text(String text) implements HubTVUpdateData {
		public static final MapCodec<Text> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.fieldOf("text").forGetter(Text::text)
		).apply(i, Text::new));

		@Override
		public String type() {
			return "text";
		}
	}

	record Image(String src) implements HubTVUpdateData {
		public static final MapCodec<Image> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.fieldOf("src").forGetter(Image::src)
		).apply(i, Image::new));

		@Override
		public String type() {
			return "image";
		}
	}
}
