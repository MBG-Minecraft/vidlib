package dev.latvian.mods.vidlib.feature.prop.builtin;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.shape.CubeShape;
import dev.latvian.mods.klib.shape.Shape;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropDataProvider;
import dev.latvian.mods.vidlib.feature.prop.PropType;

public class ShapeProp extends Prop {
	public static final PropData<ShapeProp, Shape> SHAPE = PropData.create(ShapeProp.class, "shape", Shape.DATA_TYPE, p -> p.shape, (p, v) -> p.shape = v);
	public static final PropData<ShapeProp, Gradient> COLOR = PropData.create(ShapeProp.class, "color", Gradient.DATA_TYPE, p -> p.color, (p, v) -> p.color = v.optimize());
	public static final PropData<ShapeProp, Gradient> OUTLINE_COLOR = PropData.create(ShapeProp.class, "outline_color", Gradient.DATA_TYPE, p -> p.outlineColor, (p, v) -> p.outlineColor = v.optimize());
	public static final PropData<ShapeProp, Boolean> BLOOM = PropData.create(ShapeProp.class, "bloom", DataTypes.BOOL, p -> p.bloom, (p, v) -> p.bloom = v);

	@AutoRegister
	public static final PropType<ShapeProp> TYPE = PropType.create(VidLib.id("shape"), ShapeProp::new, PropDataProvider.join(
		TICK,
		LIFESPAN,
		DYNAMIC_POSITION,
		VELOCITY,
		YAW,
		PITCH,
		GRAVITY,
		WIDTH,
		HEIGHT,
		SHAPE,
		COLOR,
		OUTLINE_COLOR,
		BLOOM
	));

	public Shape shape;
	public Gradient color;
	public Gradient outlineColor;
	public boolean bloom;

	public ShapeProp(PropContext<?> ctx) {
		super(ctx);
		this.gravity = 0F;
		this.shape = CubeShape.UNIT;
		this.color = Color.WHITE;
		this.outlineColor = Color.CYAN;
		this.bloom = false;
	}
}
