package dev.latvian.mods.vidlib.math.worldnumber;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import imgui.ImGui;
import org.jetbrains.annotations.Nullable;

public record RandomWorldNumber(WorldNumber min, WorldNumber max) implements WorldNumber {
	public static final SimpleRegistryType<RandomWorldNumber> TYPE = SimpleRegistryType.dynamic("random", RecordCodecBuilder.mapCodec(instance -> instance.group(
		WorldNumber.CODEC.optionalFieldOf("min", FixedWorldNumber.ZERO.instance()).forGetter(RandomWorldNumber::min),
		WorldNumber.CODEC.optionalFieldOf("max", FixedWorldNumber.ONE.instance()).forGetter(RandomWorldNumber::max)
	).apply(instance, RandomWorldNumber::new)), CompositeStreamCodec.of(
		WorldNumber.STREAM_CODEC.optional(FixedWorldNumber.ZERO.instance()), RandomWorldNumber::min,
		WorldNumber.STREAM_CODEC.optional(FixedWorldNumber.ONE.instance()), RandomWorldNumber::max,
		RandomWorldNumber::new
	));

	public static class Builder implements WorldNumberImBuilder {
		public static final ImBuilderHolder<WorldNumber> TYPE = new ImBuilderHolder<>("Random [min, max)", Builder::new);

		public final ImBuilder<WorldNumber> min = WorldNumberImBuilder.create(0D);
		public final ImBuilder<WorldNumber> max = WorldNumberImBuilder.create(0D);

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
		public WorldNumber build() {
			return new RandomWorldNumber(min.build(), max.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Double get(WorldNumberContext ctx) {
		var min = this.min.get(ctx);
		var max = this.max.get(ctx);

		if (min == null || max == null) {
			return null;
		}

		return ctx.level.random.nextRange(min, max);
	}
}
