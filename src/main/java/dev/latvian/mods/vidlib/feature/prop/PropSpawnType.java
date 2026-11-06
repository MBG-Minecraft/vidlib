package dev.latvian.mods.vidlib.feature.prop;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum PropSpawnType implements StringRepresentable {
	DUMMY("dummy"),
	USER("user"),
	GAME("game");

	private final String name;

	PropSpawnType(String name) {
		this.name = name;
	}

	public static final PropSpawnType[] VALUES = values();
	public static final Codec<PropSpawnType> CODEC = KLibCodecs.anyEnum(VALUES);
	public static final StreamCodec<ByteBuf, PropSpawnType> STREAM_CODEC = KLibStreamCodecs.anyEnum(VALUES);

	@Override
	public String getSerializedName() {
		return name;
	}
}
