package dev.latvian.mods.vidlib.feature.icon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record AtlasSpriteIcon(Optional<ResourceLocation> atlas, ResourceLocation sprite, boolean translucent, Color tint) implements Icon {
	public static final SimpleRegistryType<AtlasSpriteIcon> TYPE = SimpleRegistryType.dynamic("atlas_sprite", RecordCodecBuilder.mapCodec(instance -> instance.group(
		ResourceLocation.CODEC.optionalFieldOf("atlas").forGetter(AtlasSpriteIcon::atlas),
		ResourceLocation.CODEC.fieldOf("sprite").forGetter(AtlasSpriteIcon::sprite),
		Codec.BOOL.optionalFieldOf("translucent", false).forGetter(AtlasSpriteIcon::translucent),
		Color.CODEC.optionalFieldOf("tint", Color.WHITE).forGetter(AtlasSpriteIcon::tint)
	).apply(instance, AtlasSpriteIcon::new)), CompositeStreamCodec.of(
		ResourceLocation.STREAM_CODEC.optional(), AtlasSpriteIcon::atlas,
		ResourceLocation.STREAM_CODEC, AtlasSpriteIcon::sprite,
		ByteBufCodecs.BOOL, AtlasSpriteIcon::translucent,
		Color.STREAM_CODEC.optional(Color.WHITE), AtlasSpriteIcon::tint,
		AtlasSpriteIcon::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}
}
