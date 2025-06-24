package dev.latvian.mods.vidlib.math.worldvector;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum PositionType implements StringRepresentable {
	BOTTOM("bottom"),
	CENTER("center"),
	TOP("top"),
	EYES("eyes"),
	LEASH("leash"),
	SOUND_SOURCE("sound_source"),
	LOOK_TARGET("look_target"),

	;

	public static final PositionType[] VALUES = values();
	public static final Codec<PositionType> CODEC = StringRepresentable.fromEnum(() -> VALUES);
	public static final StreamCodec<ByteBuf, PositionType> STREAM_CODEC = KLibStreamCodecs.enumValue(VALUES);

	private final String name;

	PositionType(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
