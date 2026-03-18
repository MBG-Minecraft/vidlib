package dev.latvian.mods.vidlib.feature.note;

import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum NoteVisibility implements StringRepresentable {
	PUBLIC("public"),
	PRIVATE("private"),
	STAFF("staff");

	public static final NoteVisibility[] VALUES = values();
	public static final DataType<NoteVisibility> DATA_TYPE = DataType.of(VALUES);
	public static final StreamCodec<ByteBuf, NoteVisibility> STREAM_CODEC = KLibStreamCodecs.enumValue(VALUES);

	private final String name;

	NoteVisibility(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
