package dev.latvian.mods.vidlib.feature.clock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.entity.filter.EntityFilter;
import dev.latvian.mods.klib.registry.Ref;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record ClockLocation(
	Ref<ClockFont> font,
	Ref<EntityFilter> visible,
	ResourceKey<Level> dimension,
	BlockPos pos,
	float offset,
	float scale,
	Direction facing,
	String format,
	Color color,
	Color flashingColor,
	boolean fullBright
) {
	public static final Codec<ClockLocation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ClockFont.CODEC.fieldOf("font").forGetter(ClockLocation::font),
		EntityFilter.CODEC.optionalFieldOf("visible", EntityFilter.ANY).forGetter(ClockLocation::visible),
		MCCodecs.DIMENSION.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(ClockLocation::dimension),
		BlockPos.CODEC.fieldOf("pos").forGetter(ClockLocation::pos),
		Codec.FLOAT.optionalFieldOf("offset", 0F).forGetter(ClockLocation::offset),
		Codec.FLOAT.optionalFieldOf("scale", 1F).forGetter(ClockLocation::scale),
		Direction.CODEC.fieldOf("facing").forGetter(ClockLocation::facing),
		Codec.STRING.optionalFieldOf("format", "%02d:%02d").forGetter(ClockLocation::format),
		Color.CODEC.optionalFieldOf("color", Color.WHITE).forGetter(ClockLocation::color),
		Color.SOLID_CODEC.optionalFieldOf("flashing_color", Color.of(1F, 1F, 0.3F, 0.3F)).forGetter(ClockLocation::flashingColor),
		Codec.BOOL.optionalFieldOf("full_bright", true).forGetter(ClockLocation::fullBright)
	).apply(instance, ClockLocation::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ClockLocation> STREAM_CODEC = CompositeStreamCodec.of(
		ClockFont.STREAM_CODEC, ClockLocation::font,
		KLibStreamCodecs.optional(EntityFilter.STREAM_CODEC, EntityFilter.ANY), ClockLocation::visible,
		MCStreamCodecs.DIMENSION, ClockLocation::dimension,
		BlockPos.STREAM_CODEC, ClockLocation::pos,
		KLibStreamCodecs.optional(ByteBufCodecs.FLOAT, 0F), ClockLocation::offset,
		KLibStreamCodecs.optional(ByteBufCodecs.FLOAT, 1F), ClockLocation::scale,
		Direction.STREAM_CODEC, ClockLocation::facing,
		KLibStreamCodecs.optional(ByteBufCodecs.STRING_UTF8, "%02d:%02d"), ClockLocation::format,
		Color.STREAM_CODEC, ClockLocation::color,
		Color.STREAM_CODEC, ClockLocation::flashingColor,
		ByteBufCodecs.BOOL, ClockLocation::fullBright,
		ClockLocation::new
	);
}
