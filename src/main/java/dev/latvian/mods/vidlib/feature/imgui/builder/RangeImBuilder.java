package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.math.Range;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.flag.ImGuiSliderFlags;

public class RangeImBuilder implements ImBuilder<Range> {
	public static ImBuilderType<Range> type(Range range) {
		return () -> new RangeImBuilder(range);
	}

	public static final ImBuilderType<Range> TYPE = type(Range.FULL);

	public final float[] value;
	public final Range range;
	public boolean logarithmic = false;

	public RangeImBuilder(Range range) {
		this.value = new float[]{range.min(), range.max()};
		this.range = range;
	}

	public RangeImBuilder logarithmic() {
		this.logarithmic = true;
		return this;
	}

	@Override
	public void set(Range v) {
		value[0] = v.min();
		value[1] = v.max();
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		int flags = logarithmic ? ImGuiSliderFlags.Logarithmic : 0;
		ImGui.sliderFloat2("###range", value, range.min(), range.max(), "%f", flags);
		return ImUpdate.itemEdit();
	}

	@Override
	public Range build() {
		return Range.of(value[0], value[1]);
	}

	@Override
	public boolean equals(Range a, Range b) {
		return Math.abs(a.min() - b.min()) < 0.0001F && Math.abs(a.max() - b.max()) < 0.0001F;
	}
}
