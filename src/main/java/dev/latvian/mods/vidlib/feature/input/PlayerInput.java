package dev.latvian.mods.vidlib.feature.input;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.math.Rotation;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public record PlayerInput(
	int flags,
	// Movement
	boolean forward,
	boolean back,
	boolean left,
	boolean right,
	boolean jumping,
	boolean crouching,
	boolean sprinting,
	// Modifiers
	boolean shift,
	boolean control,
	boolean alt,
	boolean tab,
	// Mouse
	boolean mouseLeft,
	boolean mouseRight,
	boolean mouseMiddle,
	boolean mouseBack,
	boolean mouseNext
) {
	public static final PlayerInput NONE = new PlayerInput(0);

	public static PlayerInput of(int flags) {
		return flags == 0 ? NONE : new PlayerInput(flags);
	}

	public static PlayerInput of(
		// Movement
		boolean forward,
		boolean back,
		boolean left,
		boolean right,
		boolean jumping,
		boolean crouching,
		boolean sprinting,
		// Modifiers
		boolean shift,
		boolean control,
		boolean alt,
		boolean tab,
		// Mouse
		boolean mouseLeft,
		boolean mouseRight,
		boolean mouseMiddle,
		boolean mouseBack,
		boolean mouseNext
	) {
		var flags = 0;

		//@formatter:off
		// Movement
		if (forward) flags |= 1 << 0;
		if (back) flags |= 1 << 1;
		if (left) flags |= 1 << 2;
		if (right) flags |= 1 << 3;
		if (jumping) flags |= 1 << 4;
		if (crouching) flags |= 1 << 5;
		if (sprinting) flags |= 1 << 6;
		// Modifiers
		if (shift) flags |= 1 << 7;
		if (control) flags |= 1 << 8;
		if (alt) flags |= 1 << 9;
		if (tab) flags |= 1 << 10;
		// Mouse
		if (mouseLeft) flags |= 1 << 11;
		if (mouseRight) flags |= 1 << 12;
		if (mouseMiddle) flags |= 1 << 13;
		if (mouseBack) flags |= 1 << 14;
		if (mouseNext) flags |= 1 << 15;
		//@formatter:on

		return new PlayerInput(
			flags,
			// Movement
			forward,
			back,
			left,
			right,
			jumping,
			crouching,
			sprinting,
			// Modifiers
			shift,
			control,
			alt,
			tab,
			// Mouse
			mouseLeft,
			mouseRight,
			mouseMiddle,
			mouseBack,
			mouseNext
		);
	}

	public static final MapCodec<PlayerInput> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		// Movement
		Codec.BOOL.optionalFieldOf("forward", false).forGetter(PlayerInput::forward),
		Codec.BOOL.optionalFieldOf("back", false).forGetter(PlayerInput::back),
		Codec.BOOL.optionalFieldOf("left", false).forGetter(PlayerInput::left),
		Codec.BOOL.optionalFieldOf("right", false).forGetter(PlayerInput::right),
		Codec.BOOL.optionalFieldOf("jumping", false).forGetter(PlayerInput::jumping),
		Codec.BOOL.optionalFieldOf("crouching", false).forGetter(PlayerInput::crouching),
		Codec.BOOL.optionalFieldOf("sprinting", false).forGetter(PlayerInput::sprinting),
		// Modifiers
		Codec.BOOL.optionalFieldOf("shift", false).forGetter(PlayerInput::shift),
		Codec.BOOL.optionalFieldOf("control", false).forGetter(PlayerInput::control),
		Codec.BOOL.optionalFieldOf("alt", false).forGetter(PlayerInput::alt),
		Codec.BOOL.optionalFieldOf("tab", false).forGetter(PlayerInput::tab),
		// Mouse
		Codec.BOOL.optionalFieldOf("mouse_left", false).forGetter(PlayerInput::mouseLeft),
		Codec.BOOL.optionalFieldOf("mouse_right", false).forGetter(PlayerInput::mouseRight),
		Codec.BOOL.optionalFieldOf("mouse_middle", false).forGetter(PlayerInput::mouseMiddle),
		Codec.BOOL.optionalFieldOf("mouse_back", false).forGetter(PlayerInput::mouseBack),
		Codec.BOOL.optionalFieldOf("mouse_next", false).forGetter(PlayerInput::mouseNext)
	).apply(instance, PlayerInput::of));

	public static final Codec<PlayerInput> CODEC = MAP_CODEC.codec();
	public static final StreamCodec<ByteBuf, PlayerInput> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(PlayerInput::of, PlayerInput::hashCode);
	public static final DataType<PlayerInput> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, PlayerInput.class);

	private PlayerInput(int flags) {
		this(
			flags,
			// Movement
			(flags & (1 << 0)) != 0,
			(flags & (1 << 1)) != 0,
			(flags & (1 << 2)) != 0,
			(flags & (1 << 3)) != 0,
			(flags & (1 << 4)) != 0,
			(flags & (1 << 5)) != 0,
			(flags & (1 << 6)) != 0,
			// Modifiers
			(flags & (1 << 7)) != 0,
			(flags & (1 << 8)) != 0,
			(flags & (1 << 9)) != 0,
			(flags & (1 << 10)) != 0,
			// Mouse
			(flags & (1 << 11)) != 0,
			(flags & (1 << 12)) != 0,
			(flags & (1 << 13)) != 0,
			(flags & (1 << 14)) != 0,
			(flags & (1 << 15)) != 0
		);
	}

	@Override
	public int hashCode() {
		return flags;
	}

	public int movementX() {
		return (right ? 1 : 0) - (left ? 1 : 0);
	}

	public int movementY() {
		return (jumping ? 1 : 0) - (crouching ? 1 : 0);
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
		return forward || back || left || right || jumping;
	}

	public boolean isMovingOrCrouching() {
		return crouching || isMoving();
	}

	public boolean isAnyMouseButtonDown() {
		return mouseLeft || mouseRight || mouseMiddle || mouseBack || mouseNext;
	}
}
