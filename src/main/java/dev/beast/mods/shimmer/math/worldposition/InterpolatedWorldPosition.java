package dev.beast.mods.shimmer.math.worldposition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.math.Easing;
import dev.beast.mods.shimmer.math.KMath;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberContext;
import dev.beast.mods.shimmer.util.CompositeStreamCodec;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record InterpolatedWorldPosition(Easing easing, float start, float end, WorldPosition from, WorldPosition to) implements WorldPosition {
	public static final SimpleRegistryType<InterpolatedWorldPosition> TYPE = SimpleRegistryType.dynamic(Shimmer.id("interpolated"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		Easing.CODEC.optionalFieldOf("easing", Easing.LINEAR).forGetter(InterpolatedWorldPosition::easing),
		Codec.FLOAT.optionalFieldOf("start", 0F).forGetter(InterpolatedWorldPosition::start),
		Codec.FLOAT.optionalFieldOf("end", 1F).forGetter(InterpolatedWorldPosition::end),
		WorldPosition.CODEC.fieldOf("from").forGetter(InterpolatedWorldPosition::from),
		WorldPosition.CODEC.fieldOf("to").forGetter(InterpolatedWorldPosition::to)
	).apply(instance, InterpolatedWorldPosition::new)), CompositeStreamCodec.of(
		Easing.STREAM_CODEC, InterpolatedWorldPosition::easing,
		ByteBufCodecs.FLOAT, InterpolatedWorldPosition::start,
		ByteBufCodecs.FLOAT, InterpolatedWorldPosition::end,
		WorldPosition.STREAM_CODEC, InterpolatedWorldPosition::from,
		WorldPosition.STREAM_CODEC, InterpolatedWorldPosition::to,
		InterpolatedWorldPosition::new
	));

	public InterpolatedWorldPosition(Easing easing, WorldPosition a, WorldPosition b) {
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
