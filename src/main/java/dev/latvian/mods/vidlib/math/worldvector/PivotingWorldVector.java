package dev.latvian.mods.vidlib.math.worldvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.easing.Easing;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.FixedWorldNumber;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumber;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record PivotingWorldVector(WorldVector target, WorldNumber distance, Easing easing, WorldNumber startAngle, WorldNumber addedAngle, WorldNumber height) implements WorldVector {
	public static final SimpleRegistryType<PivotingWorldVector> TYPE = SimpleRegistryType.dynamic("pivoting", RecordCodecBuilder.mapCodec(instance -> instance.group(
		WorldVector.VEC3_CODEC.optionalFieldOf("target", LiteralWorldVector.SOURCE).forGetter(PivotingWorldVector::target),
		WorldNumber.CODEC.fieldOf("distance").forGetter(PivotingWorldVector::distance),
		Easing.CODEC.optionalFieldOf("easing", Easing.LINEAR).forGetter(PivotingWorldVector::easing),
		WorldNumber.CODEC.fieldOf("start_angle").forGetter(PivotingWorldVector::startAngle),
		WorldNumber.CODEC.optionalFieldOf("added_angle", FixedWorldNumber.ZERO.instance()).forGetter(PivotingWorldVector::addedAngle),
		WorldNumber.CODEC.optionalFieldOf("height", FixedWorldNumber.ZERO.instance()).forGetter(PivotingWorldVector::height)
	).apply(instance, PivotingWorldVector::new)), CompositeStreamCodec.of(
		WorldVector.VEC3_STREAM_CODEC, PivotingWorldVector::target,
		WorldNumber.STREAM_CODEC, PivotingWorldVector::distance,
		Easing.STREAM_CODEC, PivotingWorldVector::easing,
		WorldNumber.STREAM_CODEC, PivotingWorldVector::startAngle,
		WorldNumber.STREAM_CODEC, PivotingWorldVector::addedAngle,
		WorldNumber.STREAM_CODEC, PivotingWorldVector::height,
		PivotingWorldVector::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(WorldNumberContext ctx) {
		Double start = startAngle.get(ctx);

		if (start == null) {
			return null;
		}

		Double dist = distance.get(ctx);

		if (dist == null) {
			return null;
		}

		double angle = Math.toRadians(Mth.rotLerp(easing.ease(ctx.progress), start, start + addedAngle.getOr(ctx, 0D)));

		var pos = target.get(ctx);
		return pos == null ? null : pos.add(Math.cos(angle) * dist, height.getOr(ctx, 0D), Math.sin(angle) * dist);
	}
}
