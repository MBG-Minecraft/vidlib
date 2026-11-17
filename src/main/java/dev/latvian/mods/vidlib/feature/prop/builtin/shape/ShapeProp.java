package dev.latvian.mods.vidlib.feature.prop.builtin.shape;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.shape.CubeShape;
import dev.latvian.mods.klib.shape.Shape;
import dev.latvian.mods.klib.texture.SpriteKey;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.client.RenderLightLayer;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropType;

public class ShapeProp extends Prop {
	public static final PropData<ShapeProp, Ref<Shape>> SHAPE = PropData.create(ShapeProp.class, "shape", Shape.DATA_TYPE, p -> p.shape, (p, v) -> p.shape = v);
	public static final PropData<ShapeProp, Ref<Gradient>> COLOR = PropData.create(ShapeProp.class, "color", Gradient.DATA_TYPE, p -> p.color, (p, v) -> p.color = v);
	public static final PropData<ShapeProp, Ref<Gradient>> OUTLINE_COLOR = PropData.create(ShapeProp.class, "outline_color", Gradient.DATA_TYPE, p -> p.outlineColor, (p, v) -> p.outlineColor = v);
	public static final PropData<ShapeProp, RenderLightLayer> LIGHT_LAYER = PropData.create(ShapeProp.class, "light_layer", RenderLightLayer.DATA_TYPE, p -> p.lightLayer, (p, v) -> p.lightLayer = v);
	public static final PropData<ShapeProp, SpriteKey> TEXTURE = PropData.create(ShapeProp.class, "texture", SpriteKey.DATA_TYPE, p -> p.texture, (p, v) -> p.texture = v);

	@AutoRegister
	public static final PropType<ShapeProp> TYPE = PropType.create("shape", ShapeProp::new,
		TICK,
		LIFESPAN,
		POSITION,
		YAW,
		PITCH,
		WIDTH,
		HEIGHT,
		CAN_COLLIDE,
		CAN_INTERACT,
		SHAPE,
		COLOR,
		OUTLINE_COLOR,
		LIGHT_LAYER,
		TEXTURE
	);

	public Ref<Shape> shape;
	public Ref<Gradient> color;
	public Ref<Gradient> outlineColor;
	public RenderLightLayer lightLayer;
	public SpriteKey texture;

	public ShapeProp(PropContext<?> ctx) {
		super(ctx);
		this.shape = CubeShape.UNIT_CUBE;
		this.color = Color.CYAN.withAlpha(50).toGradient().ref();
		this.outlineColor = Gradient.WHITE;
		this.lightLayer = RenderLightLayer.NORMAL;
		this.texture = SpriteKey.EMPTY;
		this.canCollide = false;
		this.canInteract = false;
	}

	@Override
	public boolean isVisibleInFrustum(double x, double y, double z, FrustumCheck frustum) {
		return true;
	}
}
