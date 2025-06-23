package dev.latvian.mods.vidlib.math.worldvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.easing.Easing;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.FixedWorldNumber;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumber;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberImBuilder;
import imgui.ImGui;
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

	public static class Builder implements WorldVectorImBuilder {
		public static final ImBuilderHolder<WorldVector> TYPE = new ImBuilderHolder<>("Pivoting", Builder::new);

		public final ImBuilder<WorldVector> target = WorldVectorImBuilder.create();
		public final ImBuilder<WorldNumber> distance = WorldNumberImBuilder.create(5D);
		public final Easing[] easing = new Easing[]{Easing.LINEAR};
		public final ImBuilder<WorldNumber> startAngle = WorldNumberImBuilder.create(0D);
		public final ImBuilder<WorldNumber> addedAngle = WorldNumberImBuilder.create(0D);
		public final ImBuilder<WorldNumber> height = WorldNumberImBuilder.create(0D);

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
		public WorldVector build() {
			return new PivotingWorldVector(target.build(), distance.build(), easing[0], startAngle.build(), addedAngle.build(), height.build());
		}
	}

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
