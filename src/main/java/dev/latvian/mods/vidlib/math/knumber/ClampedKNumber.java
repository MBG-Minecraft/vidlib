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

public record ClampedKNumber(KNumber value, KNumber min, KNumber max) implements KNumber, ImBuilderWithHolder.Factory {
	public static final SimpleRegistryType<ClampedKNumber> TYPE = SimpleRegistryType.dynamic("clamped", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KNumber.CODEC.fieldOf("value").forGetter(ClampedKNumber::value),
		KNumber.CODEC.optionalFieldOf("min", KNumber.ZERO).forGetter(ClampedKNumber::min),
		KNumber.CODEC.optionalFieldOf("max", KNumber.ONE).forGetter(ClampedKNumber::max)
	).apply(instance, ClampedKNumber::new)), CompositeStreamCodec.of(
		KNumber.STREAM_CODEC, ClampedKNumber::value,
		KLibStreamCodecs.optional(KNumber.STREAM_CODEC, KNumber.ZERO), ClampedKNumber::min,
		KLibStreamCodecs.optional(KNumber.STREAM_CODEC, KNumber.ONE), ClampedKNumber::max,
		ClampedKNumber::new
	));

	public static class Builder implements KNumberImBuilder {
		public static final ImBuilderHolder<KNumber> TYPE = ImBuilderHolder.of("Clamped", Builder::new);

		public static final List<NodePin> PINS = List.of(
			NodePinType.NUMBER.optional("Min"),
			NodePinType.NUMBER.optional("Max"),
			NodePinType.NUMBER.output("Out")
		);

		public final ImBuilder<KNumber> value = KNumberImBuilder.create(0D);
		public final ImBuilder<KNumber> min = KNumberImBuilder.create(0D);
		public final ImBuilder<KNumber> max = KNumberImBuilder.create(1D);

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

		@Override
		public void set(KNumber value) {
			if (value instanceof ClampedKNumber n) {
				this.value.set(n.value);
				min.set(n.min);
				max.set(n.max);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			update = update.or(value.imguiKey(graphics, "Value", "value"));
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
			return value.isValid() && min.isValid() && max.isValid();
		}

		@Override
		public KNumber build() {
			return new ClampedKNumber(value.build(), min.build(), max.build());
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
		var value = this.value.getOrNaN(ctx);
		var min = this.min.getOrNaN(ctx);
		var max = this.max.getOrNaN(ctx);

		if (Double.isNaN(value)) {
			return null;
		}

		if (!Double.isNaN(min)) {
			value = Math.max(value, min);
		}

		if (!Double.isNaN(max)) {
			value = Math.min(value, max);
		}

		return value;
	}

	@Override
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
