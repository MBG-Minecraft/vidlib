package dev.latvian.mods.vidlib.feature.clock;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropImBuilderData;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import dev.latvian.mods.vidlib.feature.registry.RegistryRef;

import java.util.List;

public class ClockProp extends Prop {
	public static final PropData<ClockProp, RegistryRef<ClockFont>> FONT = PropData.create(ClockProp.class, "font", ClockFont.REF_DATA_TYPE, p -> p.font, (p, v) -> p.font = v).required();

	@AutoRegister
	public static final PropType<ClockProp> TYPE = PropType.create(VidLib.id("clock"), ClockProp::new,
		TICK,
		POSITION,
		YAW,
		FONT
	);

	public RegistryRef<ClockFont> font;

	public ClockProp(PropContext<?> ctx) {
		super(ctx);
	}

	@Override
	protected void imguiBuilders(List<PropImBuilderData<?>> builders) {
		super.imguiBuilders(builders);
		var allFonts = ClockFont.REGISTRY.getMap().values().stream().map(f -> ClockFont.REGISTRY.asRef(f, ClockFont::id)).toList();
		builders.add(new PropImBuilderData<>(FONT, new EnumImBuilder<RegistryRef<ClockFont>>(RegistryRef[]::new, allFonts, null)));
	}
}
