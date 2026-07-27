package dev.latvian.mods.vidlib.feature.clock;

import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropType;

public class ClockProp extends Prop {
	public static final PropData<ClockProp, Ref<ClockFont>> FONT = PropData.create(ClockProp.class, "font", ClockFont.DATA_TYPE, p -> p.font, (p, v) -> p.font = v, ClockFont.IM_TYPE).required();

	@AutoRegister
	public static final PropType<ClockProp> TYPE = PropType.create(ID.vidlib("clock"), ClockProp::new,
		TICK,
		POSITION,
		YAW,
		FONT
	);

	public Ref<ClockFont> font;

	public ClockProp(PropContext<?> ctx) {
		super(ctx);
	}
}
