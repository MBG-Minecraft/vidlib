package dev.latvian.mods.vidlib.feature.prop.builtin.shape;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.klib.shape.CubeShape;
import dev.latvian.mods.klib.shape.Shape;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.imgui.builder.GradientImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ResourceLocationImBuilder;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import net.minecraft.resources.ResourceLocation;

public class ShapeProp extends Prop {
	@AutoRegister
	public static final PropType<ShapeProp> TYPE = PropType.create(VidLib.id("shape"), ShapeProp::new,
		TICK,
		LIFESPAN,
		POSITION,
		VELOCITY,
		YAW,
		PITCH,
		GRAVITY,
		WIDTH,
		HEIGHT,
		CAN_COLLIDE,
		CAN_INTERACT,
		PropData.create(ShapeProp.class, "shape", Shape.DATA_TYPE, p -> p.shape, (p, v) -> p.shape = v, null),
		PropData.create(ShapeProp.class, "color", Gradient.DATA_TYPE, p -> p.color, (p, v) -> p.color = v.optimize(), GradientImBuilder.TYPE),
		PropData.create(ShapeProp.class, "outline_color", Gradient.DATA_TYPE, p -> p.outlineColor, (p, v) -> p.outlineColor = v.optimize(), GradientImBuilder.TYPE),
		PropData.createBoolean(ShapeProp.class, "bloom", p -> p.bloom, (p, v) -> p.bloom = v),
		PropData.create(ShapeProp.class, "texture", ID.DATA_TYPE, p -> p.texture, (p, v) -> p.texture = v.equals(Empty.TEXTURE) ? Empty.TEXTURE : v, ResourceLocationImBuilder.DELAYED_TYPE)
	);

	public Shape shape;
	public Gradient color;
	public Gradient outlineColor;
	public boolean bloom;
	public ResourceLocation texture;

	public ShapeProp(PropContext<?> ctx) {
		super(ctx);
		this.gravity = 0F;
		this.shape = CubeShape.UNIT;
		this.color = Color.CYAN.withAlpha(50);
		this.outlineColor = Color.WHITE;
		this.bloom = false;
		this.texture = Empty.TEXTURE;
		this.canCollide = false;
		this.canInteract = false;
	}

	@Override
	public boolean isVisible(double x, double y, double z, FrustumCheck frustum) {
		return true;
	}
}
