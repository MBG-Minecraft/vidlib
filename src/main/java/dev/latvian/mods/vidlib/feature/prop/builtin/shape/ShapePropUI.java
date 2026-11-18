package dev.latvian.mods.vidlib.feature.prop.builtin.shape;

import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.shape.Shape;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.RenderLightLayer;
import dev.latvian.mods.vidlib.feature.imgui.builder.BooleanImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.FloatImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.RegistryImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.SpriteKeyImBuilder;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropUI;

public class ShapePropUI extends PropUI<ShapeProp> {
	@ClientAutoRegister
	public static final Factory<ShapeProp> FACTORY = new Factory<>(ShapeProp.TYPE, ShapePropUI::new);

	public ShapePropUI(ShapeProp prop) {
		super(prop);
	}

	@Override
	public void collectBuilders(BuilderCollector collector) {
		super.collectBuilders(collector);
		collector.add(Prop.WIDTH, FloatImBuilder.type(0F, 200F).get());
		collector.add(Prop.HEIGHT, FloatImBuilder.type(0F, 200F).get());
		collector.add(Prop.CAN_COLLIDE, BooleanImBuilder.TYPE.get());
		collector.add(Prop.CAN_INTERACT, BooleanImBuilder.TYPE.get());
		collector.add(ShapeProp.SHAPE, new RegistryImBuilder<>(Shape.REGISTRY));
		collector.add(ShapeProp.COLOR, new RegistryImBuilder<>(Gradient.REGISTRY));
		collector.add(ShapeProp.OUTLINE_COLOR, new RegistryImBuilder<>(Gradient.REGISTRY));
		collector.add(ShapeProp.LIGHT_LAYER, new EnumImBuilder<>(RenderLightLayer.VALUES));
		collector.add(ShapeProp.TEXTURE, new SpriteKeyImBuilder());
	}
}
