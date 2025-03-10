package dev.beast.mods.shimmer.math.worldposition;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.math.Easing;
import dev.beast.mods.shimmer.math.worldnumber.FixedWorldNumber;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumber;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberContext;
import dev.beast.mods.shimmer.util.CompositeStreamCodec;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record PivotingWorldPosition(WorldPosition target, WorldNumber distance, Easing easing, WorldNumber startAngle, WorldNumber addedAngle, WorldNumber height) implements WorldPosition {
	public static final SimpleRegistryType<PivotingWorldPosition> TYPE = SimpleRegistryType.dynamic(Shimmer.id("pivoting"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		WorldPosition.CODEC.optionalFieldOf("target", SourceWorldPosition.TYPE.instance()).forGetter(PivotingWorldPosition::target),
		WorldNumber.CODEC.fieldOf("distance").forGetter(PivotingWorldPosition::distance),
		Easing.CODEC.optionalFieldOf("easing", Easing.LINEAR).forGetter(PivotingWorldPosition::easing),
		WorldNumber.CODEC.fieldOf("start_angle").forGetter(PivotingWorldPosition::startAngle),
		WorldNumber.CODEC.optionalFieldOf("added_angle", FixedWorldNumber.ZERO.instance()).forGetter(PivotingWorldPosition::addedAngle),
		WorldNumber.CODEC.optionalFieldOf("height", FixedWorldNumber.ZERO.instance()).forGetter(PivotingWorldPosition::height)
	).apply(instance, PivotingWorldPosition::new)), CompositeStreamCodec.of(
		WorldPosition.STREAM_CODEC, PivotingWorldPosition::target,
		WorldNumber.STREAM_CODEC, PivotingWorldPosition::distance,
		Easing.STREAM_CODEC, PivotingWorldPosition::easing,
		WorldNumber.STREAM_CODEC, PivotingWorldPosition::startAngle,
		WorldNumber.STREAM_CODEC, PivotingWorldPosition::addedAngle,
		WorldNumber.STREAM_CODEC, PivotingWorldPosition::height,
		PivotingWorldPosition::new
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
