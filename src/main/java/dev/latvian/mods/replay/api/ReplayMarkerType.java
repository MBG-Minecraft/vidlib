package dev.latvian.mods.replay.api;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import net.minecraft.util.StringRepresentable;

public enum ReplayMarkerType implements StringRepresentable {
	USER("user"),
	PACKET("packet"),
	EVENT_MARKER("event_marker"),
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
