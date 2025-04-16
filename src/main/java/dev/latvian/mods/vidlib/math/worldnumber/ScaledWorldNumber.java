package dev.latvian.mods.vidlib.math.worldnumber;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;

public record ScaledWorldNumber(WorldNumber a, WorldNumber b) implements WorldNumber {
	public static final SimpleRegistryType<ScaledWorldNumber> TYPE = SimpleRegistryType.dynamic(VidLib.id("scaled"), RecordCodecBuilder.mapCodec(instance -> instance.group(
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
