package dev.latvian.mods.vidlib.feature.prop.builtin.shape;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.shape.CubeShape;
import dev.latvian.mods.klib.shape.Shape;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.client.RenderLightLayer;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import net.minecraft.core.ClientAsset;

public class ShapeProp extends Prop {
	@AutoRegister
	public static final PropType<ShapeProp> TYPE = PropType.create(ID.vidlib("shape"), ShapeProp::new,
		TICK,
		LIFESPAN,
		POSITION,
		YAW,
		PITCH,
		WIDTH,
		HEIGHT,
		CAN_COLLIDE,
		CAN_INTERACT,
		PropData.create(ShapeProp.class, "shape", Shape.DATA_TYPE, p -> p.shape, (p, v) -> p.shape = v),
		PropData.create(ShapeProp.class, "color", Gradient.DATA_TYPE, p -> p.color, (p, v) -> p.color = v),
		PropData.create(ShapeProp.class, "outline_color", Gradient.DATA_TYPE, p -> p.outlineColor, (p, v) -> p.outlineColor = v),
		PropData.create(ShapeProp.class, "light_layer", RenderLightLayer.DATA_TYPE, p -> p.lightLayer, (p, v) -> p.lightLayer = v),
		PropData.create(ShapeProp.class, "texture", DataTypes.RESOURCE_TEXTURE, p -> p.texture, (p, v) -> p.texture = v.equals(Empty.TEXTURE) ? Empty.TEXTURE : v)
	);

	public Ref<Shape> shape;
	public Ref<Gradient> color;
	public Ref<Gradient> outlineColor;
	public RenderLightLayer lightLayer;
	public ClientAsset.ResourceTexture texture;

	public ShapeProp(PropContext<?> ctx) {
		super(ctx);
		this.shape = CubeShape.UNIT_CUBE;
		this.color = Color.CYAN.withAlpha(50).toGradient().ref();
		this.outlineColor = Color.WHITE.toGradient().ref();
		this.lightLayer = RenderLightLayer.NORMAL;
		this.texture = Empty.TEXTURE;
		this.canCollide = false;
		this.canInteract = false;
	}

	@Override
	public boolean isVisibleInFrustum(double x, double y, double z, FrustumCheck frustum) {
		return true;
	}
}
