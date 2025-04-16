package dev.latvian.mods.vidlib.feature.input;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

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

	public static final StreamCodec<ByteBuf, PlayerInput> STREAM_CODEC = ByteBufCodecs.UNSIGNED_SHORT.map(flags -> flags == 0 ? NONE : new PlayerInput(flags), PlayerInput::hashCode);

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
}
