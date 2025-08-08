package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;

public class IntImBuilder implements ImBuilder<Integer> {
	public static ImBuilderType<Integer> type(int min, int max) {
		return () -> new IntImBuilder(min, max);
	}

	public static ImBuilderType<Integer> type(int max) {
		return type(0, max);
	}

	public static final ImBuilderType<Integer> TYPE_100 = type(0, 100);
	public static final ImBuilderType<Integer> TYPE_1K = type(0, 1_000);
	public static final ImBuilderType<Integer> TYPE_1M = type(0, 1_000_000);

	public final int[] value;
	public final int min, max;

	public IntImBuilder(int min, int max) {
		this.value = new int[]{0};
		this.min = min;
		this.max = max;
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
