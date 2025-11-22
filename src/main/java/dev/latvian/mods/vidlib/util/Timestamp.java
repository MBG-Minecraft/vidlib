package dev.latvian.mods.vidlib.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.data.DataType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.time.Instant;

public record Timestamp(long utc, long tick) {
	public static final long EPOCH = 1640995200L; // Sat Jan 01 2022 00:00:00 GMT+0000
	public static final Timestamp NONE = new Timestamp(0L, 0L);

	public static long deflateUTC(long value) {
		return value - EPOCH;
	}

	public static long inflateUTC(long value) {
		return value + EPOCH;
	}

	public static Timestamp of(long utc, long tick) {
		return utc == 0L && tick == 0L ? NONE : new Timestamp(utc, tick);
	}

	public static Timestamp now(long tick) {
		return of(Math.floorDiv(System.currentTimeMillis(), 1000L), tick);
	}

	public static Timestamp read(DataInput in) throws IOException {
		long utc = inflateUTC(IOUtils.readVarInt(in));
		long tick = IOUtils.readVarLong(in);
		return of(utc, tick);
	}

	public static final MapCodec<Timestamp> MAP_CODEC = RecordCodecBuilder.mapCodec(timestampInstance -> timestampInstance.group(
		Codec.LONG.fieldOf("utc").forGetter(Timestamp::utc),
		Codec.LONG.fieldOf("tick").forGetter(Timestamp::tick)
	).apply(timestampInstance, Timestamp::of));

	public static final Codec<Timestamp> CODEC = MAP_CODEC.codec();

	public static final StreamCodec<ByteBuf, Timestamp> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.VAR_LONG, timestamp -> deflateUTC(timestamp.utc),
		ByteBufCodecs.VAR_LONG, Timestamp::tick,
		(utc, tick) -> of(inflateUTC(utc), tick)
	);

	public static final DataType<Timestamp> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, Timestamp.class);

	public void write(DataOutput out) throws IOException {
		IOUtils.writeVarLong(out, deflateUTC(utc));
		IOUtils.writeVarLong(out, tick);
	}

	public boolean isNone() {
		return utc == 0L && tick == 0L;
	}

	public Instant toInstant() {
		return Instant.ofEpochSecond(utc);
	}
}
