package dev.latvian.mods.vidlib.math.worldvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.easing.Easing;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.FixedWorldNumber;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumber;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record PivotingWorldVector(WorldVector target, WorldNumber distance, Easing easing, WorldNumber startAngle, WorldNumber addedAngle, WorldNumber height) implements WorldVector {
	public static final SimpleRegistryType<PivotingWorldVector> TYPE = SimpleRegistryType.dynamic("pivoting", RecordCodecBuilder.mapCodec(instance -> instance.group(
		WorldVector.VEC3_CODEC.optionalFieldOf("target", SourceWorldVector.TYPE.instance()).forGetter(PivotingWorldVector::target),
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
		double start = startAngle.get(ctx);
		double angle = Math.toRadians(Mth.rotLerp(easing.ease(ctx.progress), start, start + addedAngle.get(ctx)));
		double dist = distance.get(ctx);
		var pos = target.get(ctx);
		return pos == null ? null : pos.add(Math.cos(angle) * dist, height.get(ctx), Math.sin(angle) * dist);
	}
}
