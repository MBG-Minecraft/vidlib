package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.klib.math.Rotation;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class FreeCamera implements ControlledCameraOverride {
	public Vec3 prevPosition;
	public Vec3 position;
	public Rotation prevRotation;
	public Rotation rotation;
	public final Vector3d move;
	public double dx, dy, dz;

	public FreeCamera(Vec3 position, Rotation rotation) {
		this.prevPosition = this.position = position;
		this.prevRotation = this.rotation = rotation;
		this.move = new Vector3d();
	}

	@Override
	public boolean renderPlayer() {
		return true;
	}

	@Override
	public Vec3 getCameraPosition(float delta) {
		return prevPosition.lerp(position, delta);
	}

	@Override
	public Rotation getCameraRotation(float delta, Vec3 cameraPos) {
		return prevRotation.lerp(delta, rotation);
	}

	@Override
	public boolean tick() {
		prevPosition = position;
		prevRotation = rotation;
		position = position.add(dx, dy, dz);
		dx *= 0.8D;
		dy *= 0.8D;
		dz *= 0.8D;
		dx += move.x * 0.3D;
		dy += move.y * 0.3D;
		dz += move.z * 0.3D;
		return false;
	}

	@Override
	public boolean move(KeyboardInput in) {
		double mx = 0D;
		double my = 0D;
		double mz = 0D;

		if (in.keyPresses.shift()) {
			my -= 1D;
		}

		if (in.keyPresses.jump()) {
			my += 1D;
		}

		if (in.moveVector.lengthSquared() > 0F) {
			var vec = new Vector3f(-in.moveVector.x, 0F, -in.moveVector.y);
			vec.rotateY((float) Math.toRadians(180F - rotation.yawDeg()));
			mx += vec.x;
			mz += vec.z;
		}

		move.set(mx, my, mz);
		return true;
	}

	@Override
	public boolean turn(double yaw, double pitch) {
		rotation = Rotation.deg(rotation.yawDeg() + (float) (yaw * 0.15D), Math.clamp(rotation.pitchDeg() + (float) (pitch * 0.15D), -90F, 90F));
		return true;
	}
}
