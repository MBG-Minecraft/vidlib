package dev.latvian.mods.vidlib.feature.entity;

import com.mojang.serialization.Codec;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum PlayerActionType implements StringRepresentable {
	ATTACK("attack", true), // Left Click
	INTERACT("interact", true), // Right Click
	JUMP("jump", false), // Space
	SPRINT("sprint", false), // Ctrl
	SNEAK("sneak", false), // Shift
	DROP("drop", false), // Q
	SWAP("swap", false), // F
	RELOAD("reload", false), // R

	;

	public static final PlayerActionType[] VALUES = values();
	public static final Codec<PlayerActionType> CODEC = StringRepresentable.fromEnum(() -> VALUES);
	public static final StreamCodec<ByteBuf, PlayerActionType> STREAM_CODEC = VLStreamCodecs.enumValue(VALUES);

	private final String name;
	private final boolean mouse;

	PlayerActionType(String name, boolean mouse) {
		this.name = name;
		this.mouse = mouse;
	}

	public boolean isMouse() {
		return mouse;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
