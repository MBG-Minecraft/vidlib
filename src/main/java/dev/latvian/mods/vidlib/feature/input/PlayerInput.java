package dev.latvian.mods.vidlib.feature.input;

import dev.latvian.mods.klib.math.Rotation;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public record PlayerInput(
	int flags,
	// Movement
	boolean forward, boolean back, boolean left, boolean right, boolean jumping, boolean sneaking, boolean sprinting,
	// Modifiers
	boolean shift, boolean control, boolean alt, boolean tab,
	// Mouse
	boolean mouseLeft, boolean mouseRight, boolean mouseMiddle, boolean mouseBack, boolean mouseNext
) {
	public static final PlayerInput NONE = new PlayerInput(0);

	public static final StreamCodec<ByteBuf, PlayerInput> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(flags -> flags == 0 ? NONE : new PlayerInput(flags), PlayerInput::hashCode);

	public PlayerInput(int flags) {
		this(
			flags,
			// Movement
			(flags & (1 << 0)) != 0, (flags & (1 << 1)) != 0, (flags & (1 << 2)) != 0, (flags & (1 << 3)) != 0, (flags & (1 << 4)) != 0, (flags & (1 << 5)) != 0, (flags & (1 << 6)) != 0,
			// Keys
			(flags & (1 << 7)) != 0, (flags & (1 << 8)) != 0, (flags & (1 << 9)) != 0, (flags & (1 << 10)) != 0,
			// Mouse
			(flags & (1 << 11)) != 0, (flags & (1 << 12)) != 0, (flags & (1 << 13)) != 0, (flags & (1 << 14)) != 0, (flags & (1 << 15)) != 0
		);
	}

	public static int getFlags(
		// Movement
		boolean forward, boolean back, boolean left, boolean right, boolean jumping, boolean sneaking, boolean sprinting,
		// Modifiers
		boolean shift, boolean control, boolean alt, boolean tab,
		// Mouse
		boolean mouseLeft, boolean mouseRight, boolean mouseMiddle, boolean mouseBack, boolean mouseNext
	) {
		var flags = 0;

		//@formatter:off
		if (forward) flags |= 1 << 0;
		if (back) flags |= 1 << 1;
		if (left) flags |= 1 << 2;
		if (right) flags |= 1 << 3;
		if (jumping) flags |= 1 << 4;
		if (sneaking) flags |= 1 << 5;
		if (sprinting) flags |= 1 << 6;
		if (shift) flags |= 1 << 7;
		if (control) flags |= 1 << 8;
		if (alt) flags |= 1 << 9;
		if (tab) flags |= 1 << 10;
		if (mouseLeft) flags |= 1 << 11;
		if (mouseRight) flags |= 1 << 12;
		if (mouseMiddle) flags |= 1 << 13;
		if (mouseBack) flags |= 1 << 14;
		if (mouseNext) flags |= 1 << 15;
		//@formatter:on

		return flags;
	}

	@Override
	public int hashCode() {
		return flags;
	}

	public int movementX() {
		return (right ? 1 : 0) - (left ? 1 : 0);
	}

	public int movementY() {
		return (jumping ? 1 : 0) - (sneaking ? 1 : 0);
	}

	public int movementZ() {
		return (back ? 1 : 0) - (forward ? 1 : 0);
	}

	public Vec3 movement() {
		return isMoving() ? new Vec3(movementX(), movementY(), movementZ()) : Vec3.ZERO;
	}

	public Vec3 travelVector() {
		return isMoving() ? new Vec3(-movementX(), movementY(), -movementZ()) : Vec3.ZERO;
	}

	public Vec3 movement(double yawDegrees, double pitchDegrees, boolean spherical) {
		if (!isMoving()) {
			return Vec3.ZERO;
		}

		var v = new Vector3d(movementX(), movementY(), movementZ());

		if (spherical) {
			v.normalize();
		} else {
			var len = Math.sqrt(v.x * v.x + v.z * v.z);
			v.x /= len;
			v.z /= len;
		}

		if (yawDegrees != 0D) {
			v.rotateY(Math.toRadians(yawDegrees));
		}

		if (pitchDegrees != 0D) {
			v.rotateX(Math.toRadians(pitchDegrees));
		}

		return new Vec3(v.x, v.y, v.z);
	}

	public Vec3 movement(Rotation rotation, boolean spherical) {
		return movement(rotation.yawDeg(), rotation.pitchDeg(), spherical);
	}

	public boolean isMoving() {
		return forward || back || left || right || jumping || sneaking;
	}

	public boolean isAnyMouseButtonDown() {
		return mouseLeft || mouseRight || mouseMiddle || mouseBack || mouseNext;
	}
}
