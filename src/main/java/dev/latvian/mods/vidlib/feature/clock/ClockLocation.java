package dev.latvian.mods.vidlib.feature.clock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.registry.RegistryRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record ClockLocation(
	RegistryRef<ClockFont> font,
	EntityFilter visible,
	ResourceKey<Level> dimension,
	BlockPos pos,
	float offset,
	float scale,
	Direction facing,
	String format,
	Color color,
	boolean fullbright
) {
	public static final Codec<ClockLocation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ClockFont.REF_DATA_TYPE.codec().fieldOf("font").forGetter(ClockLocation::font),
		EntityFilter.CODEC.optionalFieldOf("visible", EntityFilter.ANY.instance()).forGetter(ClockLocation::visible),
		MCCodecs.DIMENSION.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(ClockLocation::dimension),
		BlockPos.CODEC.fieldOf("pos").forGetter(ClockLocation::pos),
		Codec.FLOAT.optionalFieldOf("offset", 0F).forGetter(ClockLocation::offset),
		Codec.FLOAT.optionalFieldOf("scale", 1F).forGetter(ClockLocation::scale),
		Direction.CODEC.fieldOf("facing").forGetter(ClockLocation::facing),
		Codec.STRING.optionalFieldOf("format", "%02d:%02d").forGetter(ClockLocation::format),
		Color.CODEC.optionalFieldOf("color", Color.WHITE).forGetter(ClockLocation::color),
		Codec.BOOL.optionalFieldOf("fullbright", true).forGetter(ClockLocation::fullbright)
	).apply(instance, ClockLocation::new));
}
