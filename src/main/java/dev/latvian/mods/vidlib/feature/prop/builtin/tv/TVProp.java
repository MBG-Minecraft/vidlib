package dev.latvian.mods.vidlib.feature.prop.builtin.tv;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.klib.math.VoxelShapeBox;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import dev.latvian.mods.vidlib.feature.visual.Visuals;
import dev.latvian.mods.vidlib.util.RotatedQuadData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class TVProp extends Prop {
	@AutoRegister
	public static final PropType<TVProp> TYPE = PropType.create(VidLib.id("tv"), TVProp::new,
		POSITION,
		YAW,
		PITCH,
		HEIGHT,
		PropData.createInt(TVProp.class, "video_width", p -> p.videoWidth, (p, v) -> p.videoWidth = v, 1, 1920),
		PropData.createInt(TVProp.class, "video_height", p -> p.videoHeight, (p, v) -> p.videoHeight = v, 1, 1080),
		PropData.createBoolean(TVProp.class, "centered", p -> p.centered, (p, v) -> p.centered = v),
		PropData.createBoolean(TVProp.class, "auto_rotate_yaw", p -> p.autoRotateYaw, (p, v) -> p.autoRotateYaw = v),
		PropData.createBoolean(TVProp.class, "auto_rotate_pitch", p -> p.autoRotatePitch, (p, v) -> p.autoRotatePitch = v),
		PropData.createBoolean(TVProp.class, "full_bright", p -> p.fullBright, (p, v) -> p.fullBright = v)
	);

	public int videoWidth;
	public int videoHeight;
	public boolean centered;
	public boolean autoRotateYaw;
	public boolean autoRotatePitch;
	public boolean fullBright;

	public RotatedQuadData rotatedQuadData;

	public TVProp(PropContext<?> ctx) {
		super(ctx);
		this.height = 4D;
		this.videoWidth = 16;
		this.videoHeight = 9;
		this.centered = true;
		this.autoRotateYaw = false;
		this.autoRotatePitch = false;
		this.fullBright = false;

		this.rotatedQuadData = new RotatedQuadData();
	}

	@Override
	public void onAdded() {
		super.onAdded();
		width = height * (double) videoWidth / (double) videoHeight;
	}

	@Override
	public void setHeight(double height) {
		super.setHeight(height);
		setWidth(height * (double) videoWidth / (double) videoHeight);
		this.rotatedQuadData = new RotatedQuadData();
	}

	@Override
	public boolean isVisible(double x, double y, double z, FrustumCheck frustum, Vec3 camera, double squaredCenterDistanceToCamera) {
		return frustum.isVisible(x, y, z, rotatedQuadData.getClipBox());
	}

	@Override
	public void tick() {
		super.tick();

		if (level.isClientSide()) {
			clientTick();
		}

		width = height * (double) videoWidth / (double) videoHeight;
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
		if (rotatedQuadData != null) {
			visuals.addCube(new Vec3(x, y, z), VoxelShapeBox.of(selected ? rotatedQuadData.getClipBox().inflate(0.0625D) : rotatedQuadData.getClipBox()), Color.TRANSPARENT, selected ? Color.YELLOW : Color.WHITE, Rotation.NONE);
		}
	}
}
