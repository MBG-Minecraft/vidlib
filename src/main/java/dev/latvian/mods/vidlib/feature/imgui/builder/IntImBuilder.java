package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;

public class IntImBuilder implements ImBuilder<Integer> {
	public final int[] value;
	public final int min, max;

	public IntImBuilder(int def, int min, int max) {
		this.value = new int[]{def};
		this.min = min;
		this.max = max;
	}

	public IntImBuilder(int def, int max) {
		this(def, 0, max);
	}

	public IntImBuilder(int def) {
		this(def, 0, Integer.MAX_VALUE);
	}

	@Override
	public void set(Integer v) {
		value[0] = v;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		ImGui.sliderInt("###int", value, min, max);
		return ImUpdate.itemEdit();
	}

	@Override
	public Integer build() {
		return value[0];
	}
}
