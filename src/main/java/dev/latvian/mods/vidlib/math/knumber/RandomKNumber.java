package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import org.jetbrains.annotations.Nullable;

public record RandomKNumber(KNumber min, KNumber max) implements KNumber, ImBuilderWrapper.BuilderSupplier {
	public static final SimpleRegistryType<RandomKNumber> TYPE = SimpleRegistryType.dynamic("random", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KNumber.CODEC.optionalFieldOf("min", KNumber.ZERO).forGetter(RandomKNumber::min),
		KNumber.CODEC.optionalFieldOf("max", KNumber.ONE).forGetter(RandomKNumber::max)
	).apply(instance, RandomKNumber::new)), CompositeStreamCodec.of(
		KLibStreamCodecs.optional(KNumber.STREAM_CODEC, KNumber.ZERO), RandomKNumber::min,
		KLibStreamCodecs.optional(KNumber.STREAM_CODEC, KNumber.ONE), RandomKNumber::max,
		RandomKNumber::new
	));

	public static class Builder implements KNumberImBuilder {
		public static final ImBuilderHolder<KNumber> TYPE = new ImBuilderHolder<>("Random [min, max)", Builder::new);

		public final ImBuilder<KNumber> min = KNumberImBuilder.create(0D);
		public final ImBuilder<KNumber> max = KNumberImBuilder.create(0D);

		@Override
		public void set(KNumber value) {
			if (value instanceof RandomKNumber n) {
				min.set(n.min);
				max.set(n.max);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			update = update.or(min.imguiKey(graphics, "Min", "min"));
			update = update.or(max.imguiKey(graphics, "Max", "max"));
			return update;
		}

		@Override
		public boolean isValid() {
			return min.isValid() && max.isValid();
		}

		@Override
		public KNumber build() {
			return new RandomKNumber(min.build(), max.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Double get(KNumberContext ctx) {
		var min = this.min.get(ctx);
		var max = this.max.get(ctx);

		if (min == null || max == null) {
			return null;
		}

		return ctx.level.random.nextRange(min, max);
	}

	@Override
	public ImBuilderHolder<?> getImBuilderHolder() {
		return Builder.TYPE;
	}
}
