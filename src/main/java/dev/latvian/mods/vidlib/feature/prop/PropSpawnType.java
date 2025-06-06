package dev.latvian.mods.vidlib.feature.prop;

import com.mojang.serialization.Codec;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum PropSpawnType implements StringRepresentable {
	DUMMY(PropListType.LEVEL, "dummy"),
	USER(PropListType.LEVEL, "user"),
	GAME(PropListType.LEVEL, "game"),
	DATA(PropListType.DATA, "data"),
	ASSETS(PropListType.ASSETS, "assets");

	public final PropListType listType;
	private final String name;

	PropSpawnType(PropListType listType, String name) {
		this.listType = listType;
		this.name = name;
	}

	public static final PropSpawnType[] VALUES = values();
	public static final Codec<PropSpawnType> CODEC = StringRepresentable.fromEnum(() -> VALUES);
	public static final StreamCodec<ByteBuf, PropSpawnType> STREAM_CODEC = VLStreamCodecs.enumValue(VALUES);

	@Override
	public String getSerializedName() {
		return name;
	}
}
