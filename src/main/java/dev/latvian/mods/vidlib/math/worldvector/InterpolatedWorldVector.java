package dev.latvian.mods.vidlib.math.worldvector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.easing.Easing;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import imgui.ImGui;
import imgui.type.ImFloat;
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

	public static class Builder implements WorldVectorImBuilder {
		public static final ImBuilderHolder<WorldVector> TYPE = new ImBuilderHolder<>("Interpolated", Builder::new);

		public final Easing[] easing = new Easing[]{Easing.LINEAR};
		public final ImBuilder<WorldVector> from = WorldVectorImBuilder.create();
		public final ImBuilder<WorldVector> to = WorldVectorImBuilder.create();
		public final ImFloat start = new ImFloat(0F);
		public final ImFloat end = new ImFloat(1F);

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			ImGui.pushItemWidth(-1F);

			ImGui.alignTextToFramePadding();
			ImGui.text("Easing");
			ImGui.sameLine();
			update = update.or(graphics.easingCombo("###easing", easing));

			ImGui.alignTextToFramePadding();
			ImGui.text("From");
			ImGui.sameLine();
			ImGui.pushID("###from");
			update = update.or(from.imgui(graphics));
			ImGui.popID();

			ImGui.alignTextToFramePadding();
			ImGui.text("To");
			ImGui.sameLine();
			ImGui.pushID("###to");
			update = update.or(to.imgui(graphics));
			ImGui.popID();

			graphics.redTextIf("Start / End", start.get() < 0F || start.get() > 1F || end.get() < 0F || end.get() > 1F || start.get() >= end.get());
			ImGui.dragFloatRange2("###range", start.getData(), end.getData(), 0.01F, 0F, 1F);
			update = update.orItemEdit();

			ImGui.popItemWidth();
			return update;
		}

		@Override
		public boolean isValid() {
			return from.isValid() && to.isValid() && start.get() >= 0F && start.get() <= 1F && end.get() >= 0F && end.get() <= 1F && start.get() < end.get();
		}

		@Override
		public WorldVector build() {
			return new InterpolatedWorldVector(easing[0], start.get(), end.get(), from.build(), to.build());
		}
	}

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
		if (ctx.progress <= start) {
			return from.get(ctx);
		} else if (ctx.progress >= end) {
			return to.get(ctx);
		}

		var a = from.get(ctx);
		var b = to.get(ctx);

		if (a == null || b == null) {
			return null;
		}

		return a.lerp(b, easing.easeClamped(KMath.map(ctx.progress, start, end, 0D, 1D)));
	}
}
