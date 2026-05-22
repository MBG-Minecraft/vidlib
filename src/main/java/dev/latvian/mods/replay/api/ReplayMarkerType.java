package dev.latvian.mods.replay.api;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import net.minecraft.util.StringRepresentable;

public enum ReplayMarkerType implements StringRepresentable {
	EDITOR("editor"),
	CLIENT_RECORDING("client_recording"),
	SERVER_RECORDING("server_recording"),
	POST_RECORDING("post_recording"),
	NOTE("note");

	public static final Codec<ReplayMarkerType> CODEC = KLibCodecs.anyEnumCodec(values());

	private final String name;

	ReplayMarkerType(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
