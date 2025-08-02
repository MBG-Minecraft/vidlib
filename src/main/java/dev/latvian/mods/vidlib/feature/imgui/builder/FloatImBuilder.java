package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.flag.ImGuiSliderFlags;

public class FloatImBuilder implements ImBuilder<Float> {
	public static final ImBuilderSupplier<Float> SUPPLIER = FloatImBuilder::new;

	public final float[] value;
	public final float min, max;
	public boolean logarithmic = false;

	public FloatImBuilder(float min, float max) {
		this.value = new float[]{0F};
		this.min = min;
		this.max = max;
	}

	public FloatImBuilder(float max) {
		this(0F, max);
	}

	public FloatImBuilder() {
		this(0F, 1F);
	}

	public FloatImBuilder logarithmic() {
		this.logarithmic = true;
		return this;
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
