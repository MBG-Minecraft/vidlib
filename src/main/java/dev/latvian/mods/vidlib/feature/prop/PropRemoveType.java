package dev.latvian.mods.vidlib.feature.prop;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum PropRemoveType implements StringRepresentable {
	NONE("none"),
	COMMAND("command"),
	GAME("game"),
	TIME_TRAVEL("time_travel"),
	EXPIRED("expired"),
	REPLACED("replaced"),
	RESOURCE_RELOAD("resource_reload"),
	DIMENSION_CHANGE("dimension_change"),
	LOGIN("login");

	private final String name;

	PropRemoveType(String name) {
		this.name = name;
	}

	public static final PropRemoveType[] VALUES = values();
	public static final Codec<PropRemoveType> CODEC = StringRepresentable.fromEnum(() -> VALUES);
	public static final StreamCodec<ByteBuf, PropRemoveType> STREAM_CODEC = KLibStreamCodecs.enumValue(VALUES);

	@Override
	public String getSerializedName() {
		return name;
	}
}
