package dev.beast.mods.shimmer.feature.icon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.kmath.texture.UV;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;

public record TextureIcon(ResourceLocation texture, UV uv, boolean translucent, Color tint) implements Icon {
	public static final SimpleRegistryType<TextureIcon> TYPE = SimpleRegistryType.dynamic(Shimmer.id("texture"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		ResourceLocation.CODEC.fieldOf("texture").forGetter(TextureIcon::texture),
		UV.CODEC.optionalFieldOf("uv", UV.FULL).forGetter(TextureIcon::uv),
		Codec.BOOL.optionalFieldOf("translucent", false).forGetter(TextureIcon::translucent),
		Color.CODEC.optionalFieldOf("tint", Color.WHITE).forGetter(TextureIcon::tint)
	).apply(instance, TextureIcon::new)), CompositeStreamCodec.of(
		ResourceLocation.STREAM_CODEC, TextureIcon::texture,
		UV.STREAM_CODEC.optional(UV.FULL), TextureIcon::uv,
		ByteBufCodecs.BOOL, TextureIcon::translucent,
		Color.STREAM_CODEC.optional(Color.WHITE), TextureIcon::tint,
		TextureIcon::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}
}
