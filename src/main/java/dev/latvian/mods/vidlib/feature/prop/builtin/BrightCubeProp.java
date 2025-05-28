package dev.latvian.mods.vidlib.feature.prop.builtin;

import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.codec.DataType;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropDataProvider;
import dev.latvian.mods.vidlib.feature.prop.PropType;

public class BrightCubeProp extends Prop {
	public static final PropData<BrightCubeProp, Color> COLOR = PropData.create(BrightCubeProp.class, "color", DataType.COLOR, p -> p.color, (p, v) -> p.color = v);
	public static final PropData<BrightCubeProp, Color> OUTLINE_COLOR = PropData.create(BrightCubeProp.class, "outline_color", DataType.COLOR, p -> p.outlineColor, (p, v) -> p.outlineColor = v);

	@AutoRegister
	public static final PropType<BrightCubeProp> TYPE = PropType.create(VidLib.id("bright_cube"), BrightCubeProp::new, PropDataProvider.join(
		BUILTIN_DATA,
		GRAVITY,
		WIDTH,
		HEIGHT,
		COLOR,
		OUTLINE_COLOR
	));

	public Color color;
	public Color outlineColor;

	public BrightCubeProp(PropContext<?> ctx) {
		super(ctx);
		this.gravity = 0F;
		this.color = Color.WHITE;
		this.outlineColor = Color.CYAN;
	}
}
