package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;

public class IntImBuilder implements ImBuilder<Integer> {
	public static final ImBuilderSupplier<Integer> SUPPLIER = IntImBuilder::new;

	public final int[] value;
	public final int min, max;

	public IntImBuilder(int min, int max) {
		this.value = new int[]{0};
		this.min = min;
		this.max = max;
	}

	public IntImBuilder(int max) {
		this(0, max);
	}

	public IntImBuilder() {
		this(0, Integer.MAX_VALUE);
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
