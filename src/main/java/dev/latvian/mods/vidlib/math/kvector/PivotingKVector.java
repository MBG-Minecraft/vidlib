package dev.latvian.mods.vidlib.math.kvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.interpolation.Interpolation;
import dev.latvian.mods.klib.interpolation.LinearInterpolation;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.interpolation.InterpolationImBuilder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.knumber.KNumberImBuilder;
import dev.latvian.mods.vidlib.math.knumber.LiteralKNumber;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record PivotingKVector(KNumber progress, KVector target, KNumber distance, Interpolation interpolation, KNumber startAngle, KNumber addedAngle, KNumber height) implements KVector, ImBuilderWithHolder.Factory {
	public static final SimpleRegistryType<PivotingKVector> TYPE = SimpleRegistryType.dynamic("pivoting", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KNumber.CODEC.optionalFieldOf("progress", LiteralKNumber.PROGRESS).forGetter(PivotingKVector::progress),
		KVector.CODEC.optionalFieldOf("target", LiteralKVector.SOURCE).forGetter(PivotingKVector::target),
		KNumber.CODEC.fieldOf("distance").forGetter(PivotingKVector::distance),
		Interpolation.CODEC.optionalFieldOf("easing", LinearInterpolation.INSTANCE).forGetter(PivotingKVector::interpolation),
		KNumber.CODEC.fieldOf("start_angle").forGetter(PivotingKVector::startAngle),
		KNumber.CODEC.optionalFieldOf("added_angle", KNumber.ZERO).forGetter(PivotingKVector::addedAngle),
		KNumber.CODEC.optionalFieldOf("height", KNumber.ZERO).forGetter(PivotingKVector::height)
	).apply(instance, PivotingKVector::new)), CompositeStreamCodec.of(
		KLibStreamCodecs.optional(KNumber.STREAM_CODEC, LiteralKNumber.PROGRESS), PivotingKVector::progress,
		KVector.STREAM_CODEC, PivotingKVector::target,
		KNumber.STREAM_CODEC, PivotingKVector::distance,
		Interpolation.STREAM_CODEC, PivotingKVector::interpolation,
		KNumber.STREAM_CODEC, PivotingKVector::startAngle,
		KNumber.STREAM_CODEC, PivotingKVector::addedAngle,
		KNumber.STREAM_CODEC, PivotingKVector::height,
		PivotingKVector::new
	));

	public static class Builder implements KVectorImBuilder {
		public static final ImBuilderHolder<KVector> TYPE = ImBuilderHolder.of("Pivoting", Builder::new);

		public final ImBuilder<KNumber> progress = KNumberImBuilder.create(LiteralKNumber.PROGRESS);
		public final ImBuilder<KVector> target = KVectorImBuilder.create();
		public final ImBuilder<KNumber> distance = KNumberImBuilder.create(5D);
		public final ImBuilder<Interpolation> interpolation = InterpolationImBuilder.create();
		public final ImBuilder<KNumber> startAngle = KNumberImBuilder.create(0D);
		public final ImBuilder<KNumber> addedAngle = KNumberImBuilder.create(0D);
		public final ImBuilder<KNumber> height = KNumberImBuilder.create(0D);

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

		@Override
		public void set(KVector value) {
			if (value instanceof PivotingKVector v) {
				progress.set(v.progress);
				target.set(v.target);
				distance.set(v.distance);
				interpolation.set(v.interpolation);
				startAngle.set(v.startAngle);
				addedAngle.set(v.addedAngle);
				height.set(v.height);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			update = update.or(progress.imguiKey(graphics, "Progress", "progress"));
			update = update.or(target.imguiKey(graphics, "Target", "target"));
			update = update.or(distance.imguiKey(graphics, "Distance", "distance"));
			update = update.or(interpolation.imguiKey(graphics, "Interpolation", "interpolation"));
			update = update.or(startAngle.imguiKey(graphics, "Start Angle", "start-angle"));
			update = update.or(addedAngle.imguiKey(graphics, "Added Angle", "added-angle"));
			update = update.or(height.imguiKey(graphics, "Height", "height"));
			return update;
		}

		@Override
		public boolean isValid() {
			return progress.isValid() && target.isValid() && distance.isValid() && interpolation.isValid() && startAngle.isValid() && addedAngle.isValid() && height.isValid();
		}

		@Override
		public KVector build() {
			return new PivotingKVector(progress.build(), target.build(), distance.build(), interpolation.build(), startAngle.build(), addedAngle.build(), height.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(KNumberContext ctx) {
		var progress = this.progress.getOrNaN(ctx);

		if (Double.isNaN(progress)) {
			return null;
		}

		Double start = startAngle.get(ctx);

		if (start == null) {
			return null;
		}

		Double dist = distance.get(ctx);

		if (dist == null) {
			return null;
		}

		double angle = Math.toRadians(Mth.rotLerp(interpolation.interpolate(progress), start, start + addedAngle.getOr(ctx, 0D)));

		var pos = target.get(ctx);
		return pos == null ? null : pos.add(Math.cos(angle) * dist, height.getOr(ctx, 0D), Math.sin(angle) * dist);
	}

	@Override
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
