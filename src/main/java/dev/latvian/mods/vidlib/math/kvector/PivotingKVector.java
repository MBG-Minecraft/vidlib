package dev.latvian.mods.vidlib.math.kvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.easing.Easing;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.knumber.KNumberImBuilder;
import imgui.ImGui;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record PivotingKVector(KVector target, KNumber distance, Easing easing, KNumber startAngle, KNumber addedAngle, KNumber height) implements KVector {
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
		public final Easing[] easing = new Easing[]{Easing.LINEAR};
		public final ImBuilder<KNumber> startAngle = KNumberImBuilder.create(0D);
		public final ImBuilder<KNumber> addedAngle = KNumberImBuilder.create(0D);
		public final ImBuilder<KNumber> height = KNumberImBuilder.create(0D);

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			ImGui.pushItemWidth(-1F);

			ImGui.alignTextToFramePadding();
			ImGui.text("Target");
			ImGui.sameLine();
			ImGui.pushID("###target");
			update = update.or(target.imgui(graphics));
			ImGui.popID();

			ImGui.alignTextToFramePadding();
			ImGui.text("Distance");
			ImGui.sameLine();
			ImGui.pushID("###distance");
			update = update.or(distance.imgui(graphics));
			ImGui.popID();

			ImGui.alignTextToFramePadding();
			ImGui.text("Easing");
			ImGui.sameLine();
			update = update.or(graphics.easingCombo("###easing", easing));

			ImGui.alignTextToFramePadding();
			ImGui.text("Start Angle");
			ImGui.sameLine();
			ImGui.pushID("###start-angle");
			update = update.or(startAngle.imgui(graphics));
			ImGui.popID();

			ImGui.alignTextToFramePadding();
			ImGui.text("Added Angle");
			ImGui.sameLine();
			ImGui.pushID("###added-angle");
			update = update.or(addedAngle.imgui(graphics));
			ImGui.popID();

			ImGui.alignTextToFramePadding();
			ImGui.text("Height");
			ImGui.sameLine();
			ImGui.pushID("###height");
			update = update.or(height.imgui(graphics));
			ImGui.popID();

			ImGui.popItemWidth();
			return update;
		}

		@Override
		public boolean isValid() {
			return target.isValid() && distance.isValid() && startAngle.isValid() && addedAngle.isValid() && height.isValid();
		}

		@Override
		public KVector build() {
			return new PivotingKVector(target.build(), distance.build(), easing[0], startAngle.build(), addedAngle.build(), height.build());
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
}
