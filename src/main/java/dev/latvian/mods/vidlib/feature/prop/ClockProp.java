package dev.latvian.mods.vidlib.feature.prop;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.clock.ClockFont;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;

public class ClockProp extends Prop {
	@AutoRegister
	public static final PropType<ClockProp> TYPE = new PropType<>(VidLib.id("clock"), RecordCodecBuilder.mapCodec(instance -> instance.group(
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
