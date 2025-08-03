package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.flag.ImGuiSliderFlags;

public class AngleImBuilder extends FloatImBuilder {
	public static final ImBuilderSupplier<Float> SUPPLIER_360 = () -> new AngleImBuilder(0F, 360F);
	public static final ImBuilderSupplier<Float> SUPPLIER_180 = () -> new AngleImBuilder(-180F, 180F);
	public static final ImBuilderSupplier<Float> SUPPLIER_90 = () -> new AngleImBuilder(-90F, 90F);

	public AngleImBuilder(float min, float max) {
		super(min, max);
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		int flags = logarithmic ? ImGuiSliderFlags.Logarithmic : 0;
		ImGui.dragFloat("###float", value, 0.25F, min, max, "%f", flags);
		return ImUpdate.itemEdit();
	}
}
