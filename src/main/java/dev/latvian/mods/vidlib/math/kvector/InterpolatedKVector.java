package dev.latvian.mods.vidlib.math.kvector;

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
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import imgui.ImGui;
import imgui.type.ImFloat;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record InterpolatedKVector(Easing easing, float start, float end, KVector from, KVector to) implements KVector {
	public static final SimpleRegistryType<InterpolatedKVector> TYPE = SimpleRegistryType.dynamic("interpolated", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Easing.CODEC.optionalFieldOf("easing", Easing.LINEAR).forGetter(InterpolatedKVector::easing),
		Codec.FLOAT.optionalFieldOf("start", 0F).forGetter(InterpolatedKVector::start),
		Codec.FLOAT.optionalFieldOf("end", 1F).forGetter(InterpolatedKVector::end),
		KVector.CODEC.fieldOf("from").forGetter(InterpolatedKVector::from),
		KVector.CODEC.fieldOf("to").forGetter(InterpolatedKVector::to)
	).apply(instance, InterpolatedKVector::new)), CompositeStreamCodec.of(
		Easing.STREAM_CODEC, InterpolatedKVector::easing,
		ByteBufCodecs.FLOAT, InterpolatedKVector::start,
		ByteBufCodecs.FLOAT, InterpolatedKVector::end,
		KVector.STREAM_CODEC, InterpolatedKVector::from,
		KVector.STREAM_CODEC, InterpolatedKVector::to,
		InterpolatedKVector::new
	));

	public static class Builder implements KVectorImBuilder {
		public static final ImBuilderHolder<KVector> TYPE = new ImBuilderHolder<>("Interpolated", Builder::new);

		public final Easing[] easing = new Easing[]{Easing.LINEAR};
		public final ImBuilder<KVector> from = KVectorImBuilder.create();
		public final ImBuilder<KVector> to = KVectorImBuilder.create();
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
		public KVector build() {
			return new InterpolatedKVector(easing[0], start.get(), end.get(), from.build(), to.build());
		}
	}

	public InterpolatedKVector(Easing easing, KVector a, KVector b) {
		this(easing, 0F, 1F, a, b);
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(KNumberContext ctx) {
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
