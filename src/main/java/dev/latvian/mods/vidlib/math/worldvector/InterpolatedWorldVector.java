package dev.latvian.mods.vidlib.math.worldvector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.KMath;
import dev.latvian.mods.kmath.easing.Easing;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record InterpolatedWorldVector(Easing easing, float start, float end, WorldVector from, WorldVector to) implements WorldVector {
	public static final SimpleRegistryType<InterpolatedWorldVector> TYPE = SimpleRegistryType.dynamic("interpolated", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Easing.CODEC.optionalFieldOf("easing", Easing.LINEAR).forGetter(InterpolatedWorldVector::easing),
		Codec.FLOAT.optionalFieldOf("start", 0F).forGetter(InterpolatedWorldVector::start),
		Codec.FLOAT.optionalFieldOf("end", 1F).forGetter(InterpolatedWorldVector::end),
		WorldVector.CODEC.fieldOf("from").forGetter(InterpolatedWorldVector::from),
		WorldVector.CODEC.fieldOf("to").forGetter(InterpolatedWorldVector::to)
	).apply(instance, InterpolatedWorldVector::new)), CompositeStreamCodec.of(
		Easing.STREAM_CODEC, InterpolatedWorldVector::easing,
		ByteBufCodecs.FLOAT, InterpolatedWorldVector::start,
		ByteBufCodecs.FLOAT, InterpolatedWorldVector::end,
		WorldVector.STREAM_CODEC, InterpolatedWorldVector::from,
		WorldVector.STREAM_CODEC, InterpolatedWorldVector::to,
		InterpolatedWorldVector::new
	));

	public InterpolatedWorldVector(Easing easing, WorldVector a, WorldVector b) {
		this(easing, 0F, 1F, a, b);
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(WorldNumberContext ctx) {
		var a = from.get(ctx);

		if (ctx.progress <= start) {
			return a;
		}

		var b = to.get(ctx);

		if (ctx.progress >= end) {
			return b;
		}

		return a == null || b == null ? null : a.lerp(b, easing.easeClamped(KMath.map(ctx.progress, start, end, 0F, 1F)));
	}
}
