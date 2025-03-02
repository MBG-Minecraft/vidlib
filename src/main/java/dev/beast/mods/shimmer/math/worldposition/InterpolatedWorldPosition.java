package dev.beast.mods.shimmer.math.worldposition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.math.Easing;
import dev.beast.mods.shimmer.math.KMath;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberContext;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record InterpolatedWorldPosition(Easing easing, float start, float end, WorldPosition a, WorldPosition b) implements WorldPosition {
	public static final SimpleRegistryType<InterpolatedWorldPosition> TYPE = SimpleRegistryType.dynamic(Shimmer.id("interpolated"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		Easing.CODEC.fieldOf("easing").forGetter(InterpolatedWorldPosition::easing),
		Codec.FLOAT.fieldOf("start").forGetter(InterpolatedWorldPosition::start),
		Codec.FLOAT.fieldOf("end").forGetter(InterpolatedWorldPosition::end),
		WorldPosition.CODEC.fieldOf("a").forGetter(InterpolatedWorldPosition::a),
		WorldPosition.CODEC.fieldOf("b").forGetter(InterpolatedWorldPosition::b)
	).apply(instance, InterpolatedWorldPosition::new)), StreamCodec.composite(
		Easing.STREAM_CODEC,
		InterpolatedWorldPosition::easing,
		ByteBufCodecs.FLOAT,
		InterpolatedWorldPosition::start,
		ByteBufCodecs.FLOAT,
		InterpolatedWorldPosition::end,
		WorldPosition.STREAM_CODEC,
		InterpolatedWorldPosition::a,
		WorldPosition.STREAM_CODEC,
		InterpolatedWorldPosition::b,
		InterpolatedWorldPosition::new
	));

	public static final SimpleRegistryType<InterpolatedWorldPosition> SIMPLE_TYPE = SimpleRegistryType.dynamic(Shimmer.id("simple_interpolated"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		Easing.CODEC.fieldOf("easing").forGetter(InterpolatedWorldPosition::easing),
		WorldPosition.CODEC.fieldOf("a").forGetter(InterpolatedWorldPosition::a),
		WorldPosition.CODEC.fieldOf("b").forGetter(InterpolatedWorldPosition::b)
	).apply(instance, InterpolatedWorldPosition::new)), StreamCodec.composite(
		Easing.STREAM_CODEC,
		InterpolatedWorldPosition::easing,
		WorldPosition.STREAM_CODEC,
		InterpolatedWorldPosition::a,
		WorldPosition.STREAM_CODEC,
		InterpolatedWorldPosition::b,
		InterpolatedWorldPosition::new
	));

	public InterpolatedWorldPosition(Easing easing, WorldPosition a, WorldPosition b) {
		this(easing, 0F, 1F, a, b);
	}

	@Override
	public SimpleRegistryType<?> type() {
		return start == 0F && end == 1F ? SIMPLE_TYPE : TYPE;
	}

	@Override
	public Vec3 get(WorldNumberContext ctx) {
		var a = this.a.get(ctx);

		if (ctx.progress <= start) {
			return a;
		}

		var b = this.b.get(ctx);

		if (ctx.progress >= end) {
			return b;
		}

		return a.lerp(b, easing.easeClamped(KMath.map(ctx.progress, start, end, 0F, 1F)));
	}
}
