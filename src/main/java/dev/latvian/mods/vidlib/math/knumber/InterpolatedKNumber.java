package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.easing.Easing;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import imgui.ImGui;
import imgui.type.ImFloat;
import net.minecraft.network.codec.ByteBufCodecs;
import org.jetbrains.annotations.Nullable;

public record InterpolatedKNumber(Easing easing, float start, float end, KNumber from, KNumber to) implements KNumber, ImBuilderWrapper.BuilderSupplier {
	public static final SimpleRegistryType<InterpolatedKNumber> TYPE = SimpleRegistryType.dynamic("interpolated", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Easing.CODEC.optionalFieldOf("easing", Easing.LINEAR).forGetter(InterpolatedKNumber::easing),
		Codec.FLOAT.optionalFieldOf("start", 0F).forGetter(InterpolatedKNumber::start),
		Codec.FLOAT.optionalFieldOf("end", 1F).forGetter(InterpolatedKNumber::end),
		KNumber.CODEC.fieldOf("from").forGetter(InterpolatedKNumber::from),
		KNumber.CODEC.fieldOf("to").forGetter(InterpolatedKNumber::to)
	).apply(instance, InterpolatedKNumber::new)), CompositeStreamCodec.of(
		Easing.STREAM_CODEC, InterpolatedKNumber::easing,
		ByteBufCodecs.FLOAT, InterpolatedKNumber::start,
		ByteBufCodecs.FLOAT, InterpolatedKNumber::end,
		KNumber.STREAM_CODEC, InterpolatedKNumber::from,
		KNumber.STREAM_CODEC, InterpolatedKNumber::to,
		InterpolatedKNumber::new
	));

	public static class Builder implements KNumberImBuilder {
		public static final ImBuilderHolder<KNumber> TYPE = new ImBuilderHolder<>("Interpolated", Builder::new);

		public final ImBuilder<Easing> easing = EnumImBuilder.easing();
		public final ImBuilder<KNumber> from = KNumberImBuilder.create(0D);
		public final ImBuilder<KNumber> to = KNumberImBuilder.create(1D);
		public final ImFloat start = new ImFloat(0F);
		public final ImFloat end = new ImFloat(1F);

		@Override
		public void set(KNumber value) {
			if (value instanceof InterpolatedKNumber n) {
				easing.set(n.easing);
				from.set(n.from);
				to.set(n.to);
				start.set(n.start);
				end.set(n.end);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			update = update.or(easing.imguiKey(graphics, "Easing", "easing"));
			update = update.or(from.imguiKey(graphics, "From", "from"));
			update = update.or(to.imguiKey(graphics, "To", "to"));

			graphics.redTextIf("Start / End", start.get() < 0F || start.get() > 1F || end.get() < 0F || end.get() > 1F || start.get() >= end.get());
			ImGui.dragFloatRange2("###range", start.getData(), end.getData(), 0.01F, 0F, 1F);
			update = update.orItemEdit();
			return update;
		}

		@Override
		public boolean isValid() {
			return easing.isValid() && from.isValid() && to.isValid() && start.get() >= 0F && start.get() <= 1F && end.get() >= 0F && end.get() <= 1F && start.get() < end.get();
		}

		@Override
		public KNumber build() {
			return new InterpolatedKNumber(easing.build(), start.get(), end.get(), from.build(), to.build());
		}
	}

	public InterpolatedKNumber(Easing easing, KNumber a, KNumber b) {
		this(easing, 0F, 1F, a, b);
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Double get(KNumberContext ctx) {
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

	@Override
	public ImBuilderHolder<?> getImBuilderHolder() {
		return Builder.TYPE;
	}
}
