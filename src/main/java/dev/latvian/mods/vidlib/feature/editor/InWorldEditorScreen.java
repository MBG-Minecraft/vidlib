package dev.latvian.mods.vidlib.feature.editor;

import dev.latvian.mods.kmath.Rotation;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class InWorldEditorScreen extends Screen {
	public Vec3 prevCameraPosition = Vec3.ZERO;
	public Vec3 cameraPosition = Vec3.ZERO;
	public double prevCameraYaw = 0D;
	public double cameraYaw = 0D;
	public double prevCameraPitch = 0D;
	public double cameraPitch = 0D;

	public InWorldEditorScreen() {
		super(Component.empty());
	}

	@Override
	public boolean renderPlayer() {
		return cameraPosition.distanceToSqr(minecraft.player.getEyePosition()) >= 0.5D * 0.5D;
	}

	@Override
	public boolean overrideCamera() {
		return true;
	}

	@Override
	public Vec3 getCameraPosition(float delta) {
		return prevCameraPosition.lerp(cameraPosition, delta);
	}

	@Override
	public Rotation getCameraRotation(float delta, Vec3 cameraPos) {
		return Rotation.deg((float) Mth.rotLerp(delta, prevCameraYaw, cameraYaw), (float) Mth.rotLerp(delta, prevCameraPitch, cameraPitch));
	}
}
