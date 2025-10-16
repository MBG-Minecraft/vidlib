package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.imgui.node.NodePin;
import dev.latvian.mods.vidlib.feature.imgui.node.NodePinType;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record RandomKNumber(KNumber min, KNumber max) implements KNumber, ImBuilderWithHolder.Factory {
	public static final SimpleRegistryType<RandomKNumber> TYPE = SimpleRegistryType.dynamic("random", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KNumber.CODEC.optionalFieldOf("min", KNumber.ZERO).forGetter(RandomKNumber::min),
		KNumber.CODEC.optionalFieldOf("max", KNumber.ONE).forGetter(RandomKNumber::max)
	).apply(instance, RandomKNumber::new)), CompositeStreamCodec.of(
		KLibStreamCodecs.optional(KNumber.STREAM_CODEC, KNumber.ZERO), RandomKNumber::min,
		KLibStreamCodecs.optional(KNumber.STREAM_CODEC, KNumber.ONE), RandomKNumber::max,
		RandomKNumber::new
	));

	public static class Builder implements KNumberImBuilder {
		public static final ImBuilderHolder<KNumber> TYPE = ImBuilderHolder.of("Random [min, max)", Builder::new);

		public static final List<NodePin> PINS = List.of(
			NodePinType.NUMBER.required("Min"),
			NodePinType.NUMBER.required("Max"),
			NodePinType.NUMBER.output("Out")
		);

		public final ImBuilder<KNumber> min = KNumberImBuilder.create(0D);
		public final ImBuilder<KNumber> max = KNumberImBuilder.create(0D);

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

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
		public ImUpdate nodeImgui(ImGraphics graphics) {
			return ImUpdate.NONE;
		}

		@Override
		public boolean isValid() {
			return min.isValid() && max.isValid();
		}

		@Override
		public KNumber build() {
			return new RandomKNumber(min.build(), max.build());
		}

		@Override
		public List<NodePin> getNodePins() {
			return PINS;
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
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
