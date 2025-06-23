package dev.latvian.mods.vidlib.math.worldnumber;

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
import imgui.ImGui;
import imgui.type.ImFloat;
import net.minecraft.network.codec.ByteBufCodecs;
import org.jetbrains.annotations.Nullable;

public record InterpolatedWorldNumber(Easing easing, float start, float end, WorldNumber from, WorldNumber to) implements WorldNumber {
	public static final SimpleRegistryType<InterpolatedWorldNumber> TYPE = SimpleRegistryType.dynamic("interpolated", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Easing.CODEC.optionalFieldOf("easing", Easing.LINEAR).forGetter(InterpolatedWorldNumber::easing),
		Codec.FLOAT.optionalFieldOf("start", 0F).forGetter(InterpolatedWorldNumber::start),
		Codec.FLOAT.optionalFieldOf("end", 1F).forGetter(InterpolatedWorldNumber::end),
		WorldNumber.CODEC.fieldOf("from").forGetter(InterpolatedWorldNumber::from),
		WorldNumber.CODEC.fieldOf("to").forGetter(InterpolatedWorldNumber::to)
	).apply(instance, InterpolatedWorldNumber::new)), CompositeStreamCodec.of(
		Easing.STREAM_CODEC, InterpolatedWorldNumber::easing,
		ByteBufCodecs.FLOAT, InterpolatedWorldNumber::start,
		ByteBufCodecs.FLOAT, InterpolatedWorldNumber::end,
		WorldNumber.STREAM_CODEC, InterpolatedWorldNumber::from,
		WorldNumber.STREAM_CODEC, InterpolatedWorldNumber::to,
		InterpolatedWorldNumber::new
	));

	public static class Builder implements WorldNumberImBuilder {
		public static final ImBuilderHolder<WorldNumber> TYPE = new ImBuilderHolder<>("Interpolated", Builder::new);

		public final Easing[] easing = new Easing[]{Easing.LINEAR};
		public final ImBuilder<WorldNumber> from = WorldNumberImBuilder.create(0D);
		public final ImBuilder<WorldNumber> to = WorldNumberImBuilder.create(1D);
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
			graphics.redTextIf("From", !from.isValid());
			ImGui.sameLine();
			ImGui.pushID("###from");
			update = update.or(from.imgui(graphics));
			ImGui.popID();

			ImGui.alignTextToFramePadding();
			graphics.redTextIf("To", !to.isValid());
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
		public WorldNumber build() {
			return new InterpolatedWorldNumber(easing[0], start.get(), end.get(), from.build(), to.build());
		}
	}

	public InterpolatedWorldNumber(Easing easing, WorldNumber a, WorldNumber b) {
		this(easing, 0F, 1F, a, b);
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Double get(WorldNumberContext ctx) {
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

		return KMath.lerp(easing.easeClamped(KMath.map(ctx.progress, start, end, 0D, 1D)), a, b);
	}
}
