package dev.latvian.mods.vidlib.feature.icon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;

public record TextureIcon(ResourceLocation texture, UV uv, boolean translucent, Color tint) implements Icon {
	public static final SimpleRegistryType<TextureIcon> TYPE = SimpleRegistryType.dynamic("texture", RecordCodecBuilder.mapCodec(instance -> instance.group(
		ResourceLocation.CODEC.fieldOf("texture").forGetter(TextureIcon::texture),
		UV.CODEC.optionalFieldOf("uv", UV.FULL).forGetter(TextureIcon::uv),
		Codec.BOOL.optionalFieldOf("translucent", false).forGetter(TextureIcon::translucent),
		Color.CODEC.optionalFieldOf("tint", Color.WHITE).forGetter(TextureIcon::tint)
	).apply(instance, TextureIcon::new)), CompositeStreamCodec.of(
		ResourceLocation.STREAM_CODEC, TextureIcon::texture,
		KLibStreamCodecs.optional(UV.STREAM_CODEC, UV.FULL), TextureIcon::uv,
		ByteBufCodecs.BOOL, TextureIcon::translucent,
		KLibStreamCodecs.optional(Color.STREAM_CODEC, Color.WHITE), TextureIcon::tint,
		TextureIcon::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}
}
