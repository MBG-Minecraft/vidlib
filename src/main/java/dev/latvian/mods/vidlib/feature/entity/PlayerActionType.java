package dev.latvian.mods.vidlib.feature.entity;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.Set;

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
	public static final StreamCodec<ByteBuf, PlayerActionType> STREAM_CODEC = KLibStreamCodecs.enumValue(VALUES);

	public static final Set<PlayerActionType> SWAP_SET = Set.of(SWAP);
	public static final Set<PlayerActionType> RELOAD_SET = Set.of(RELOAD);
	public static final Set<PlayerActionType> SWAP_AND_RELOAD_SET = Set.of(SWAP, RELOAD);

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
