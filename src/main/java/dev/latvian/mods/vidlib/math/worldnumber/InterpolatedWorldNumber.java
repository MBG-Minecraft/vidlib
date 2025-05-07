package dev.latvian.mods.vidlib.math.worldnumber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.KMath;
import dev.latvian.mods.kmath.easing.Easing;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;

public record InterpolatedWorldNumber(Easing easing, float start, float end, WorldNumber from, WorldNumber to) implements WorldNumber {
	public static final SimpleRegistryType<InterpolatedWorldNumber> TYPE = SimpleRegistryType.dynamic("interpolated", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Easing.CODEC.optionalFieldOf("easing", Easing.LINEAR).forGetter(InterpolatedWorldNumber::easing),
		Codec.FLOAT.optionalFieldOf("start", 0F).forGetter(InterpolatedWorldNumber::start),
		Codec.FLOAT.optionalFieldOf("end", 1F).forGetter(InterpolatedWorldNumber::end),
		WorldNumber.CODEC.fieldOf("from").forGetter(InterpolatedWorldNumber::from),
		WorldNumber.CODEC.fieldOf("to").forGetter(InterpolatedWorldNumber::to)
	).apply(instance, InterpolatedWorldNumber::new)), CompositeStreamCodec.of(
		Easing.STREAM_CODEC, InterpolatedWorldNumber::easing,
		ByteBufCodecs.FLOAT, InterpolatedWorldNumber::start,
		ByteBufCodecs.FLOAT, InterpolatedWorldNumber::end,
		WorldNumber.STREAM_CODEC, InterpolatedWorldNumber::from,
		WorldNumber.STREAM_CODEC, InterpolatedWorldNumber::to,
		InterpolatedWorldNumber::new
	));

	public InterpolatedWorldNumber(Easing easing, WorldNumber a, WorldNumber b) {
		this(easing, 0F, 1F, a, b);
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public double get(WorldNumberContext ctx) {
		var a = this.from.get(ctx);

		if (ctx.progress <= start) {
			return a;
		}

		var b = this.to.get(ctx);

		if (ctx.progress >= end) {
			return b;
		}

		return KMath.lerp(easing.easeClamped(KMath.map(ctx.progress, start, end, 0D, 1D)), a, b);
	}
}
