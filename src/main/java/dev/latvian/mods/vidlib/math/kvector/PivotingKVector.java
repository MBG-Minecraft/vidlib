package dev.latvian.mods.vidlib.math.kvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.easing.Easing;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.knumber.KNumberImBuilder;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record PivotingKVector(KVector target, KNumber distance, Easing easing, KNumber startAngle, KNumber addedAngle, KNumber height) implements KVector, ImBuilderWrapper.BuilderSupplier {
	public static final SimpleRegistryType<PivotingKVector> TYPE = SimpleRegistryType.dynamic("pivoting", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KVector.CODEC.optionalFieldOf("target", LiteralKVector.SOURCE).forGetter(PivotingKVector::target),
		KNumber.CODEC.fieldOf("distance").forGetter(PivotingKVector::distance),
		Easing.CODEC.optionalFieldOf("easing", Easing.LINEAR).forGetter(PivotingKVector::easing),
		KNumber.CODEC.fieldOf("start_angle").forGetter(PivotingKVector::startAngle),
		KNumber.CODEC.optionalFieldOf("added_angle", KNumber.ZERO).forGetter(PivotingKVector::addedAngle),
		KNumber.CODEC.optionalFieldOf("height", KNumber.ZERO).forGetter(PivotingKVector::height)
	).apply(instance, PivotingKVector::new)), CompositeStreamCodec.of(
		KVector.STREAM_CODEC, PivotingKVector::target,
		KNumber.STREAM_CODEC, PivotingKVector::distance,
		Easing.STREAM_CODEC, PivotingKVector::easing,
		KNumber.STREAM_CODEC, PivotingKVector::startAngle,
		KNumber.STREAM_CODEC, PivotingKVector::addedAngle,
		KNumber.STREAM_CODEC, PivotingKVector::height,
		PivotingKVector::new
	));

	public static class Builder implements KVectorImBuilder {
		public static final ImBuilderHolder<KVector> TYPE = new ImBuilderHolder<>("Pivoting", Builder::new);

		public final ImBuilder<KVector> target = KVectorImBuilder.create();
		public final ImBuilder<KNumber> distance = KNumberImBuilder.create(5D);
		public final ImBuilder<Easing> easing = EnumImBuilder.easing();
		public final ImBuilder<KNumber> startAngle = KNumberImBuilder.create(0D);
		public final ImBuilder<KNumber> addedAngle = KNumberImBuilder.create(0D);
		public final ImBuilder<KNumber> height = KNumberImBuilder.create(0D);

		@Override
		public void set(KVector value) {
			if (value instanceof PivotingKVector v) {
				target.set(v.target);
				distance.set(v.distance);
				easing.set(v.easing);
				startAngle.set(v.startAngle);
				addedAngle.set(v.addedAngle);
				height.set(v.height);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			update = update.or(target.imguiKey(graphics, "Target", "target"));
			update = update.or(distance.imguiKey(graphics, "Distance", "distance"));
			update = update.or(easing.imguiKey(graphics, "Easing", "easing"));
			update = update.or(startAngle.imguiKey(graphics, "Start Angle", "start-angle"));
			update = update.or(addedAngle.imguiKey(graphics, "Added Angle", "added-angle"));
			update = update.or(height.imguiKey(graphics, "Height", "height"));
			return update;
		}

		@Override
		public boolean isValid() {
			return target.isValid() && distance.isValid() && easing.isValid() && startAngle.isValid() && addedAngle.isValid() && height.isValid();
		}

		@Override
		public KVector build() {
			return new PivotingKVector(target.build(), distance.build(), easing.build(), startAngle.build(), addedAngle.build(), height.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(KNumberContext ctx) {
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

	@Override
	public ImBuilderHolder<?> getImBuilderHolder() {
		return Builder.TYPE;
	}
}
