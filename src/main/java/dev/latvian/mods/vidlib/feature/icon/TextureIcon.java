package dev.latvian.mods.vidlib.feature.icon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.codec.ByteBufCodecs;

public record TextureIcon(
	ClientAsset texture,
	UV uv,
	boolean translucent,
	Color color
) implements ColorIcon {
	public static final SimpleRegistryType<TextureIcon> TYPE = SimpleRegistryType.dynamic("texture", RecordCodecBuilder.mapCodec(instance -> instance.group(
		ClientAsset.CODEC.fieldOf("texture").forGetter(TextureIcon::texture),
		UV.CODEC.optionalFieldOf("uv", UV.FULL).forGetter(TextureIcon::uv),
		Codec.BOOL.optionalFieldOf("translucent", false).forGetter(TextureIcon::translucent),
		Color.CODEC.optionalFieldOf("tint", Color.WHITE).forGetter(TextureIcon::color)
	).apply(instance, TextureIcon::new)), CompositeStreamCodec.of(
		ClientAsset.STREAM_CODEC, TextureIcon::texture,
		KLibStreamCodecs.optional(UV.STREAM_CODEC, UV.FULL), TextureIcon::uv,
		ByteBufCodecs.BOOL, TextureIcon::translucent,
		KLibStreamCodecs.optional(Color.STREAM_CODEC, Color.WHITE), TextureIcon::color,
		TextureIcon::new
	));

	public TextureIcon(ClientAsset texture) {
		this(texture, UV.FULL, false, Color.WHITE);
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}
}
