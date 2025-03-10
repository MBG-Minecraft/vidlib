package dev.beast.mods.shimmer.math.worldnumber;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.CompositeStreamCodec;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;

public record ScaledWorldNumber(WorldNumber a, WorldNumber b) implements WorldNumber {
	public static final SimpleRegistryType<ScaledWorldNumber> TYPE = SimpleRegistryType.dynamic(Shimmer.id("scaled"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		WorldNumber.CODEC.fieldOf("a").forGetter(ScaledWorldNumber::a),
		WorldNumber.CODEC.fieldOf("b").forGetter(ScaledWorldNumber::b)
	).apply(instance, ScaledWorldNumber::new)), CompositeStreamCodec.of(
		WorldNumber.STREAM_CODEC, ScaledWorldNumber::a,
		WorldNumber.STREAM_CODEC, ScaledWorldNumber::b,
		ScaledWorldNumber::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public double get(WorldNumberContext ctx) {
		var a = this.a.get(ctx);
		var b = this.b.get(ctx);
		return a * b;
	}
}
