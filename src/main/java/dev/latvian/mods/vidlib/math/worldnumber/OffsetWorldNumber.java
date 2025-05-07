package dev.latvian.mods.vidlib.math.worldnumber;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;

public record OffsetWorldNumber(WorldNumber a, WorldNumber b) implements WorldNumber {
	public static final SimpleRegistryType<OffsetWorldNumber> TYPE = SimpleRegistryType.dynamic("offset", RecordCodecBuilder.mapCodec(instance -> instance.group(
		WorldNumber.CODEC.fieldOf("a").forGetter(OffsetWorldNumber::a),
		WorldNumber.CODEC.fieldOf("b").forGetter(OffsetWorldNumber::b)
	).apply(instance, OffsetWorldNumber::new)), CompositeStreamCodec.of(
		WorldNumber.STREAM_CODEC, OffsetWorldNumber::a,
		WorldNumber.STREAM_CODEC, OffsetWorldNumber::b,
		OffsetWorldNumber::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public double get(WorldNumberContext ctx) {
		var a = this.a.get(ctx);
		var b = this.b.get(ctx);
		return a + b;
	}
}
