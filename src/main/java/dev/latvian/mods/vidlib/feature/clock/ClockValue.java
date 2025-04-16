package dev.latvian.mods.vidlib.feature.clock;

import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ClockValue(int second, Type type) {
	public enum Type {
		NORMAL,
		FLASH,
		FINISHED;

		public static final StreamCodec<ByteBuf, Type> STREAM_CODEC = VLStreamCodecs.enumValue(values());
	}

	public static final StreamCodec<ByteBuf, ClockValue> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, ClockValue::second,
		Type.STREAM_CODEC, ClockValue::type,
		ClockValue::new
	);

	public ClockValue(int second, Type type) {
		this.second = Math.max(0, second);
		this.type = type;
	}
}
