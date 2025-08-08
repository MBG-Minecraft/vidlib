package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.flag.ImGuiSliderFlags;

public class FloatImBuilder implements ImBuilder<Float> {
	public static ImBuilderType<Float> type(float min, float max, boolean logarithmic) {
		return () -> new FloatImBuilder(min, max, logarithmic);
	}

	public static ImBuilderType<Float> type(float min, float max) {
		return type(min, max, false);
	}

	public static final ImBuilderType<Float> TYPE = type(0F, 1F);

	public final float[] value;
	public final float min, max;
	public final boolean logarithmic;

	public FloatImBuilder(float min, float max, boolean logarithmic) {
		this.value = new float[]{0F};
		this.min = min;
		this.max = max;
		this.logarithmic = logarithmic;
	}

	public FloatImBuilder(float min, float max) {
		this(min, max, false);
	}

	@Override
	public void set(Float v) {
		value[0] = v;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		int flags = logarithmic ? ImGuiSliderFlags.Logarithmic : 0;
		ImGui.sliderFloat("###float", value, min, max, "%f", flags);
		return ImUpdate.itemEdit();
	}

	@Override
	public Float build() {
		return value[0];
	}

	@Override
	public boolean equals(Float a, Float b) {
		return Math.abs(a - b) < 0.0001F;
	}
}
