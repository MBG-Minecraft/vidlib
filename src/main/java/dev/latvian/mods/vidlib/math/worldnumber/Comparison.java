package dev.latvian.mods.vidlib.math.worldnumber;

import com.mojang.serialization.Codec;
import dev.latvian.mods.vidlib.feature.codec.DataType;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum Comparison implements StringRepresentable {
	EQUALS("=="),
	NOT_EQUALS("!="),
	GREATER_THAN(">"),
	GREATER_THAN_OR_EQUALS(">="),
	LESS_THAN("<"),
	LESS_THAN_OR_EQUALS("<=");

	public static final Comparison[] VALUES = values();
	public static final Codec<Comparison> CODEC = StringRepresentable.fromEnum(() -> VALUES);
	public static final StreamCodec<ByteBuf, Comparison> STREAM_CODEC = VLStreamCodecs.enumValue(VALUES);
	public static final DataType<Comparison> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, Comparison.class);

	public final String symbol;

	Comparison(String symbol) {
		this.symbol = symbol;
	}

	public boolean test(double a, double b) {
		return switch (this) {
			case EQUALS -> Math.abs(a - b) < 0.00001;
			case NOT_EQUALS -> Math.abs(a - b) >= 0.00001;
			case GREATER_THAN -> a > b;
			case GREATER_THAN_OR_EQUALS -> a >= b;
			case LESS_THAN -> a < b;
			case LESS_THAN_OR_EQUALS -> a <= b;
		};
	}

	@Override
	public String getSerializedName() {
		return symbol;
	}
}
