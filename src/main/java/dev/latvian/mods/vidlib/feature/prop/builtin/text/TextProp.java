package dev.latvian.mods.vidlib.feature.prop.builtin.text;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.klib.math.VoxelShapeBox;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.imgui.builder.Color4ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.TextComponentImBuilder;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import dev.latvian.mods.vidlib.feature.visual.Visuals;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TextProp extends Prop {
	@AutoRegister
	public static final PropType<TextProp> TYPE = PropType.create(VidLib.id("text"), TextProp::new,
		POSITION,
		YAW,
		PITCH,
		HEIGHT,
		PropData.create(TextProp.class, "text", DataTypes.TEXT_COMPONENT, p -> p.text, TextProp::setText, TextComponentImBuilder.MULTILINE_TYPE),
		PropData.create(TextProp.class, "color", Color.DATA_TYPE, p -> p.color, (p, v) -> p.color = v, Color4ImBuilder.TYPE),
		PropData.createBoolean(TextProp.class, "shadow", p -> p.shadow, (p, v) -> p.shadow = v),
		PropData.createBoolean(TextProp.class, "see_through", p -> p.seeThrough, (p, v) -> p.seeThrough = v),
		PropData.createInt(TextProp.class, "wrap", p -> p.wrap, (p, v) -> p.wrap = v)
	);

	private Component text;
	public Color color;
	public boolean shadow;
	public boolean seeThrough;
	public int wrap;
	List<FormattedCharSequence> cachedText;

	public TextProp(PropContext<?> ctx) {
		super(ctx);
		this.width = 1F;
		this.height = 2F;
		this.gravity = 0F;
		this.text = Component.literal("Text");
		this.color = Color.WHITE;
		this.shadow = true;
		this.seeThrough = false;
		this.wrap = 1000;
	}

	public void setText(Component text) {
		this.text = text;
		this.cachedText = null;
	}

	public Component getText() {
		return text;
	}

	@Override
	public boolean isVisible(double x, double y, double z, FrustumCheck frustum) {
		return true;
	}

	@Override
	public void debugVisuals(Visuals visuals, double x, double y, double z, float delta, boolean selected) {
		//noinspection deprecation
		visuals.addCube(new Vec3(x, y, z), VoxelShapeBox.of(new AABB(-width / 2D, 0D, -0.03125, width / 2D, height, 0.03125)), Color.TRANSPARENT, selected ? Color.YELLOW : Color.WHITE, Rotation.deg(getYaw(delta), -getPitch(delta)));
	}
}
