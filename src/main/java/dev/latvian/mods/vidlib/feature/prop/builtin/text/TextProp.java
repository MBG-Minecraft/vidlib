package dev.latvian.mods.vidlib.feature.prop.builtin.text;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import dev.latvian.mods.vidlib.feature.visual.Visuals;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class TextProp extends Prop {
	public static final PropData<TextProp, Component> TEXT = PropData.create(TextProp.class, "text", DataTypes.TEXT_COMPONENT, p -> p.text, TextProp::setText);
	public static final PropData<TextProp, Color> COLOR = PropData.create(TextProp.class, "color", Color.DATA_TYPE, p -> p.color, (p, v) -> p.color = v);
	public static final PropData<TextProp, Boolean> SHADOW = PropData.createBoolean(TextProp.class, "shadow", p -> p.shadow, (p, v) -> p.shadow = v);
	public static final PropData<TextProp, Boolean> SEE_THROUGH = PropData.createBoolean(TextProp.class, "see_through", p -> p.seeThrough, (p, v) -> p.seeThrough = v);
	public static final PropData<TextProp, Integer> WRAP = PropData.createInt(TextProp.class, "wrap", p -> p.wrap, (p, v) -> p.wrap = v, 0, 1000);
	public static final PropData<TextProp, Boolean> FULL_BRIGHT = PropData.createBoolean(TextProp.class, "full_bright", p -> p.fullBright, (p, v) -> p.fullBright = v);
	public static final PropData<TextProp, Color> BACKGROUND_COLOR = PropData.create(TextProp.class, "background_color", Color.DATA_TYPE, p -> p.backgroundColor, (p, v) -> p.backgroundColor = v);
	public static final PropData<TextProp, Boolean> CENTERED = PropData.createBoolean(TextProp.class, "centered", p -> p.centered, (p, v) -> p.centered = v);
	public static final PropData<TextProp, Boolean> AUTO_ROTATE_YAW = PropData.createBoolean(TextProp.class, "auto_rotate_yaw", p -> p.autoRotateYaw, (p, v) -> p.autoRotateYaw = v);
	public static final PropData<TextProp, Boolean> AUTO_ROTATE_PITCH = PropData.createBoolean(TextProp.class, "auto_rotate_pitch", p -> p.autoRotatePitch, (p, v) -> p.autoRotatePitch = v);
	public static final PropData<TextProp, Float> LINE_HEIGHT = PropData.createFloat(TextProp.class, "line_height", p -> p.lineHeight, (p, v) -> p.lineHeight = v, 0F, 30F);
	public static final PropData<TextProp, Float> SHADOW_DISTANCE = PropData.createFloat(TextProp.class, "shadow_distance", p -> p.shadowDistance, (p, v) -> p.shadowDistance = v, 0F, 1F);

	@AutoRegister
	public static final PropType<TextProp> TYPE = PropType.create("text", TextProp::new,
		POSITION,
		YAW,
		PITCH,
		HEIGHT,
		TEXT,
		COLOR,
		SHADOW,
		SEE_THROUGH,
		WRAP,
		FULL_BRIGHT,
		BACKGROUND_COLOR,
		CENTERED,
		AUTO_ROTATE_YAW,
		AUTO_ROTATE_PITCH,
		LINE_HEIGHT,
		SHADOW_DISTANCE
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
	public float shadowDistance;

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
		this.shadowDistance = 0F;
	}

	public void setText(Component text) {
		this.text = text;
		this.cachedData = null;
	}

	public Component getText() {
		return text;
	}

	@Override
	public boolean isVisibleInFrustum(double x, double y, double z, FrustumCheck frustum) {
		return cachedData == null || cachedData.lines.length > 0 && frustum.isVisible(cachedData.box.aabb(x, y, z));
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
			var r = Rotation.compute(getPos(1F), Minecraft.getInstance().gameRenderer.mainCamera().position());

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
			visuals.addDebugCube(x, y, z, cachedData.box, selected);
		}
	}
}
