package dev.beast.mods.shimmer.feature.prop;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.clock.ClockFont;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;

public class ClockProp extends Prop {
	@AutoRegister
	public static final PropType<ClockProp> TYPE = new PropType<>(Shimmer.id("clock"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		ClockFont.KNOWN_CODEC.codec().fieldOf("font").forGetter(p -> p.font)
	).apply(instance, ClockProp::new)), CompositeStreamCodec.of(
		ClockFont.KNOWN_CODEC.streamCodec(), p -> p.font,
		ClockProp::new
	));

	public ClockFont font;

	public ClockProp(ClockFont font) {
		super(TYPE);
		this.font = font;
	}
}
