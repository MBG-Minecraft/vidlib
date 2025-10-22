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
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

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
		PropData.createInt(TextProp.class, "wrap", p -> p.wrap, (p, v) -> p.wrap = v, 0, 1000),
		PropData.createBoolean(TextProp.class, "full_bright", p -> p.fullBright, (p, v) -> p.fullBright = v),
		PropData.create(TextProp.class, "background_color", Color.DATA_TYPE, p -> p.backgroundColor, (p, v) -> p.backgroundColor = v, Color4ImBuilder.TYPE),
		PropData.createBoolean(TextProp.class, "centered", p -> p.centered, (p, v) -> p.centered = v),
		PropData.createBoolean(TextProp.class, "auto_rotate_yaw", p -> p.autoRotateYaw, (p, v) -> p.autoRotateYaw = v),
		PropData.createBoolean(TextProp.class, "auto_rotate_pitch", p -> p.autoRotatePitch, (p, v) -> p.autoRotatePitch = v),
		PropData.createFloat(TextProp.class, "line_height", p -> p.lineHeight, (p, v) -> p.lineHeight = v, 0F, 30F)
	);

	private Component text;
	public Color color;
	public boolean shadow;
	public boolean seeThrough;
	public int wrap;
	public boolean fullBright;
	public Color backgroundColor;
	public boolean centered;
	public boolean autoRotateYaw;
	public boolean autoRotatePitch;
	public float lineHeight;

	CachedTextData cachedData;

	public TextProp(PropContext<?> ctx) {
		super(ctx);
		this.width = 1F;
		this.height = 2F;
		this.text = Component.literal("Text");
		this.color = Color.WHITE;
		this.shadow = true;
		this.seeThrough = false;
		this.wrap = 1000;
		this.fullBright = true;
		this.backgroundColor = Color.TRANSPARENT;
		this.centered = false;
		this.autoRotateYaw = false;
		this.autoRotatePitch = false;
		this.lineHeight = 9F;
	}

	public void setText(Component text) {
		this.text = text;
		this.cachedData = null;
	}

	public Component getText() {
		return text;
	}

	@Override
	public boolean isVisible(double x, double y, double z, FrustumCheck frustum) {
		return cachedData == null || cachedData.lines.length > 0 && frustum.isVisible(x, y, z, cachedData.box);
	}

	@Override
	public void tick() {
		super.tick();

		if (level.isClientSide()) {
			clientTick();
		}
	}

	private void clientTick() {
		if (autoRotateYaw || autoRotatePitch) {
			var r = Rotation.compute(getPos(1F), Minecraft.getInstance().gameRenderer.getMainCamera().getPosition());

			if (autoRotateYaw) {
				rotation.y = r.yawDeg();
			}

			if (autoRotatePitch) {
				rotation.x = -r.pitchDeg();
			}
		}
	}

	@Override
	public void debugVisuals(Visuals visuals, double x, double y, double z, float delta, boolean selected) {
		if (cachedData != null) {
			visuals.addCube(new Vec3(x, y, z), VoxelShapeBox.of(selected ? cachedData.box.inflate(0.0625D) : cachedData.box), Color.TRANSPARENT, selected ? Color.YELLOW : Color.WHITE, Rotation.NONE);
		}
	}
}
