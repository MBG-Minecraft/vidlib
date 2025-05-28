package dev.latvian.mods.vidlib.feature.clock;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropDataProvider;
import dev.latvian.mods.vidlib.feature.prop.PropType;

public class ClockProp extends Prop {
	public static final PropData<ClockProp, ClockFont> FONT = PropData.create(ClockProp.class, "font", ClockFont.REGISTERED_DATA_TYPE.type(), p -> p.font, (p, v) -> p.font = v).required();

	@AutoRegister
	public static final PropType<ClockProp> TYPE = PropType.create(VidLib.id("clock"), ClockProp::new, PropDataProvider.join(
		BUILTIN_DATA,
		FONT
	));

	public ClockFont font;

	public ClockProp(PropContext<?> ctx) {
		super(ctx);
	}
}
