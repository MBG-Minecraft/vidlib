package dev.latvian.mods.vidlib.feature.prop;

import com.mojang.serialization.Codec;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum PropSpawnType implements StringRepresentable {
	DUMMY("dummy"),
	USER("user"),
	GAME("game"),
	DATA("data"),
	ASSETS("assets");

	private final String name;

	PropSpawnType(String name) {
		this.name = name;
	}

	public static final PropSpawnType[] VALUES = values();
	public static final Codec<PropSpawnType> CODEC = StringRepresentable.fromEnum(() -> VALUES);
	public static final StreamCodec<ByteBuf, PropSpawnType> STREAM_CODEC = VLStreamCodecs.enumValue(VALUES);

	@Override
	public String getSerializedName() {
		return name;
	}

	public boolean isCommandKillable() {
		return this == USER || this == GAME;
	}
}
