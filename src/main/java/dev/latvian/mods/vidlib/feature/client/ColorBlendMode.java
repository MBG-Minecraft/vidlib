package dev.latvian.mods.vidlib.feature.client;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum ColorBlendMode implements StringRepresentable {
	MULTIPLICATIVE("multiplicative", "Multiplicative"),
	ADDITIVE("additive", "Additive"),
	SUBTRACTIVE("subtractive", "Subtractive"),

	;

	public static final ColorBlendMode[] VALUES = values();
	public static final Codec<ColorBlendMode> CODEC = StringRepresentable.fromEnum(() -> VALUES);
	public static final StreamCodec<ByteBuf, ColorBlendMode> STREAM_CODEC = KLibStreamCodecs.enumValue(VALUES);
	public static final DataType<ColorBlendMode> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, ColorBlendMode.class);

	private final String name;
	public final String displayName;

	ColorBlendMode(String name, String displayName) {
		this.name = name;
		this.displayName = displayName;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
