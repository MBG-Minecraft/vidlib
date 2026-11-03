package dev.latvian.mods.vidlib.feature.prop.builtin.image;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.klib.math.RotatedQuadData;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.klib.texture.SpriteKey;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import dev.latvian.mods.vidlib.feature.visual.Visuals;
import net.minecraft.client.Minecraft;

public class ImageProp extends Prop {
	@AutoRegister
	public static final PropType<ImageProp> TYPE = PropType.create(ID.vidlib("image"), ImageProp::new,
		POSITION,
		YAW,
		PITCH,
		WIDTH,
		HEIGHT,
		PropData.create(ImageProp.class, "texture", SpriteKey.DATA_TYPE, p -> p.texture, (p, v) -> p.texture = v),
		PropData.create(ImageProp.class, "tint", Color.DATA_TYPE, p -> p.tint, (p, v) -> p.tint = v),
		PropData.createBoolean(ImageProp.class, "see_through", p -> p.seeThrough, (p, v) -> p.seeThrough = v),
		PropData.createBoolean(ImageProp.class, "full_bright", p -> p.fullBright, (p, v) -> p.fullBright = v),
		PropData.createBoolean(ImageProp.class, "centered", p -> p.centered, (p, v) -> p.centered = v),
		PropData.createBoolean(ImageProp.class, "auto_rotate_yaw", p -> p.autoRotateYaw, (p, v) -> p.autoRotateYaw = v),
		PropData.createBoolean(ImageProp.class, "auto_rotate_pitch", p -> p.autoRotatePitch, (p, v) -> p.autoRotatePitch = v),
		PropData.createBoolean(ImageProp.class, "translucent", p -> p.translucent, (p, v) -> p.translucent = v)
	);

	public SpriteKey texture;
	public Color tint;
	public boolean seeThrough;
	public boolean fullBright;
	public boolean centered;
	public boolean autoRotateYaw;
	public boolean autoRotatePitch;
	public boolean translucent;

	public RotatedQuadData rotatedQuadData;

	public ImageProp(PropContext<?> ctx) {
		super(ctx);
		this.width = 1F;
		this.height = 1F;
		this.texture = SpriteKey.special(VidLibTextures.LOGO);
		this.tint = Color.WHITE;
		this.seeThrough = false;
		this.fullBright = true;
		this.centered = true;
		this.autoRotateYaw = false;
		this.autoRotatePitch = false;
		this.translucent = true;

		this.rotatedQuadData = new RotatedQuadData();
	}

	@Override
	public void setWidth(double width) {
		super.setWidth(width);
		rotatedQuadData = new RotatedQuadData();
	}

	@Override
	public void setHeight(double height) {
		super.setHeight(height);
		rotatedQuadData = new RotatedQuadData();
	}

	@Override
	public boolean isVisibleInFrustum(double x, double y, double z, FrustumCheck frustum) {
		return frustum.isVisible(rotatedQuadData.getClipBox().aabb(x, y, z));
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
		if (rotatedQuadData != null) {
			visuals.addDebugCube(x, y, z, rotatedQuadData.getClipBox(), selected);
		}
	}
}
