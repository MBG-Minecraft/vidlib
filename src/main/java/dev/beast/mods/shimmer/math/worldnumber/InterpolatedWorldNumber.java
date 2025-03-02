package dev.beast.mods.shimmer.math.worldnumber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.math.Easing;
import dev.beast.mods.shimmer.math.KMath;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record InterpolatedWorldNumber(Easing easing, float start, float end, WorldNumber a, WorldNumber b) implements WorldNumber {
	public static final SimpleRegistryType<InterpolatedWorldNumber> TYPE = SimpleRegistryType.dynamic(Shimmer.id("interpolated"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		Easing.CODEC.fieldOf("easing").forGetter(InterpolatedWorldNumber::easing),
		Codec.FLOAT.fieldOf("start").forGetter(InterpolatedWorldNumber::start),
		Codec.FLOAT.fieldOf("end").forGetter(InterpolatedWorldNumber::end),
		WorldNumber.CODEC.fieldOf("a").forGetter(InterpolatedWorldNumber::a),
		WorldNumber.CODEC.fieldOf("b").forGetter(InterpolatedWorldNumber::b)
	).apply(instance, InterpolatedWorldNumber::new)), StreamCodec.composite(
		Easing.STREAM_CODEC,
		InterpolatedWorldNumber::easing,
		ByteBufCodecs.FLOAT,
		InterpolatedWorldNumber::start,
		ByteBufCodecs.FLOAT,
		InterpolatedWorldNumber::end,
		WorldNumber.STREAM_CODEC,
		InterpolatedWorldNumber::a,
		WorldNumber.STREAM_CODEC,
		InterpolatedWorldNumber::b,
		InterpolatedWorldNumber::new
	));

	public static final SimpleRegistryType<InterpolatedWorldNumber> SIMPLE_TYPE = SimpleRegistryType.dynamic(Shimmer.id("simple_interpolated"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		Easing.CODEC.fieldOf("easing").forGetter(InterpolatedWorldNumber::easing),
		WorldNumber.CODEC.fieldOf("a").forGetter(InterpolatedWorldNumber::a),
		WorldNumber.CODEC.fieldOf("b").forGetter(InterpolatedWorldNumber::b)
	).apply(instance, InterpolatedWorldNumber::new)), StreamCodec.composite(
		Easing.STREAM_CODEC,
		InterpolatedWorldNumber::easing,
		WorldNumber.STREAM_CODEC,
		InterpolatedWorldNumber::a,
		WorldNumber.STREAM_CODEC,
		InterpolatedWorldNumber::b,
		InterpolatedWorldNumber::new
	));

	public InterpolatedWorldNumber(Easing easing, WorldNumber a, WorldNumber b) {
		this(easing, 0F, 1F, a, b);
	}

	@Override
	public SimpleRegistryType<?> type() {
		return start == 0F && end == 1F ? SIMPLE_TYPE : TYPE;
	}

	@Override
	public double get(WorldNumberContext ctx) {
		var a = this.a.get(ctx);

		if (ctx.progress <= start) {
			return a;
		}

		var b = this.b.get(ctx);

		if (ctx.progress >= end) {
			return b;
		}

		return KMath.lerp(easing.easeClamped(KMath.map(ctx.progress, start, end, 0D, 1D)), a, b);
	}
}
