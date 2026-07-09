package dev.latvian.mods.vidlib.feature.icon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.registry.CustomRegistryType;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.vidlib.feature.visual.SpriteKey;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;

public record AtlasSpriteIcon(SpriteKey sprite, boolean translucent, Color tint) implements Icon {
	public static final DynamicType<RegistryFriendlyByteBuf, Icon> TYPE = DynamicType.create("atlas_sprite", RecordCodecBuilder.mapCodec(instance -> instance.group(
		SpriteKey.PREFER_BLOCK_CODEC.fieldOf("sprite").forGetter(AtlasSpriteIcon::sprite),
		Codec.BOOL.optionalFieldOf("translucent", false).forGetter(AtlasSpriteIcon::translucent),
		Color.CODEC.optionalFieldOf("tint", Color.WHITE).forGetter(AtlasSpriteIcon::tint)
	).apply(instance, AtlasSpriteIcon::new)), CompositeStreamCodec.of(
		SpriteKey.STREAM_CODEC, AtlasSpriteIcon::sprite,
		ByteBufCodecs.BOOL, AtlasSpriteIcon::translucent,
		KLibStreamCodecs.optional(Color.STREAM_CODEC, Color.WHITE), AtlasSpriteIcon::tint,
		AtlasSpriteIcon::new
	));

	@Override
	public CustomRegistryType<RegistryFriendlyByteBuf, Icon> type() {
		return TYPE;
	}
}
