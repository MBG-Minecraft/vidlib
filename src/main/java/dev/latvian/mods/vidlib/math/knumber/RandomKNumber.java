package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import imgui.ImGui;
import org.jetbrains.annotations.Nullable;

public record RandomKNumber(KNumber min, KNumber max) implements KNumber {
	public static final SimpleRegistryType<RandomKNumber> TYPE = SimpleRegistryType.dynamic("random", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KNumber.CODEC.optionalFieldOf("min", KNumber.ZERO).forGetter(RandomKNumber::min),
		KNumber.CODEC.optionalFieldOf("max", KNumber.ONE).forGetter(RandomKNumber::max)
	).apply(instance, RandomKNumber::new)), CompositeStreamCodec.of(
		KNumber.STREAM_CODEC.optional(KNumber.ZERO), RandomKNumber::min,
		KNumber.STREAM_CODEC.optional(KNumber.ONE), RandomKNumber::max,
		RandomKNumber::new
	));

	public static class Builder implements KNumberImBuilder {
		public static final ImBuilderHolder<KNumber> TYPE = new ImBuilderHolder<>("Random [min, max)", Builder::new);

		public final ImBuilder<KNumber> min = KNumberImBuilder.create(0D);
		public final ImBuilder<KNumber> max = KNumberImBuilder.create(0D);

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			ImGui.pushItemWidth(-1F);

			ImGui.alignTextToFramePadding();
			graphics.redTextIf("Min", !min.isValid());
			ImGui.sameLine();
			ImGui.pushID("###min");
			update = update.or(min.imgui(graphics));
			ImGui.popID();

			ImGui.alignTextToFramePadding();
			graphics.redTextIf("Max", !max.isValid());
			ImGui.sameLine();
			ImGui.pushID("###max");
			update = update.or(max.imgui(graphics));
			ImGui.popID();

			ImGui.popItemWidth();
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
}
