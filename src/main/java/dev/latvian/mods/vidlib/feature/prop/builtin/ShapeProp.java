package dev.latvian.mods.vidlib.feature.prop.builtin;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.shape.CubeShape;
import dev.latvian.mods.klib.shape.Shape;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.imgui.builder.BooleanImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.GradientImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ResourceLocationImBuilder;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropImBuilderData;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ShapeProp extends Prop {
	public static final PropData<ShapeProp, Shape> SHAPE = PropData.create(ShapeProp.class, "shape", Shape.DATA_TYPE, p -> p.shape, (p, v) -> p.shape = v);
	public static final PropData<ShapeProp, Gradient> COLOR = PropData.create(ShapeProp.class, "color", Gradient.DATA_TYPE, p -> p.color, (p, v) -> p.color = v.optimize());
	public static final PropData<ShapeProp, Gradient> OUTLINE_COLOR = PropData.create(ShapeProp.class, "outline_color", Gradient.DATA_TYPE, p -> p.outlineColor, (p, v) -> p.outlineColor = v.optimize());
	public static final PropData<ShapeProp, Boolean> BLOOM = PropData.create(ShapeProp.class, "bloom", DataTypes.BOOL, p -> p.bloom, (p, v) -> p.bloom = v);
	public static final PropData<ShapeProp, ResourceLocation> TEXTURE = PropData.create(ShapeProp.class, "texture", ID.DATA_TYPE, p -> p.texture, (p, v) -> p.texture = v.equals(Empty.TEXTURE) ? Empty.TEXTURE : v);

	@AutoRegister
	public static final PropType<ShapeProp> TYPE = PropType.create(VidLib.id("shape"), ShapeProp::new,
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
		BLOOM,
		CAN_COLLIDE,
		CAN_INTERACT,
		TEXTURE
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
	protected void imguiBuilders(List<PropImBuilderData<?>> builders) {
		super.imguiBuilders(builders);
		// builders.add(new PropImBuilderData<>(SHAPE, new Vector3dImBuilder()));
		builders.add(new PropImBuilderData<>(COLOR, GradientImBuilder.SUPPLIER));
		builders.add(new PropImBuilderData<>(OUTLINE_COLOR, GradientImBuilder.SUPPLIER));
		builders.add(new PropImBuilderData<>(BLOOM, BooleanImBuilder.SUPPLIER));
		builders.add(new PropImBuilderData<>(TEXTURE, ResourceLocationImBuilder.DELAYED_SUPPLIER));
	}
}
