package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;

public class LongImBuilder implements ImBuilder<Long> {
	public static final ImBuilderSupplier<Long> SUPPLIER = LongImBuilder::new;

	public final long[] value;
	public final long min, max;

	public LongImBuilder(long min, long max) {
		this.value = new long[]{0L};
		this.min = min;
		this.max = max;
	}

	public LongImBuilder(long max) {
		this(0, max);
	}

	public LongImBuilder() {
		this(0, Long.MAX_VALUE);
	}

	@Override
	public void set(Long v) {
		value[0] = v;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var value = new int[]{(int) this.value[0]};
		int min = (int) Math.max(this.min, Integer.MIN_VALUE);
		int max = (int) Math.min(this.max, Integer.MAX_VALUE);
		ImGui.sliderInt("###long", value, min, max);
		this.value[0] = value[0];
		return ImUpdate.itemEdit();
	}

	@Override
	public Long build() {
		return value[0];
	}
}
