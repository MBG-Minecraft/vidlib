package dev.latvian.mods.vidlib.feature.prop;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum PropListType implements StringRepresentable {
	LEVEL("level"),
	DATA("data"),
	ASSETS("assets");

	private final String name;

	PropListType(String name) {
		this.name = name;
	}

	public static final PropListType[] VALUES = values();
	public static final Codec<PropListType> CODEC = StringRepresentable.fromEnum(() -> VALUES);
	public static final StreamCodec<ByteBuf, PropListType> STREAM_CODEC = KLibStreamCodecs.enumValue(VALUES);

	@Override
	public String getSerializedName() {
		return name;
	}
}
