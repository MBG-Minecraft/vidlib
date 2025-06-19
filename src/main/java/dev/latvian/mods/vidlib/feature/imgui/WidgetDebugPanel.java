package dev.latvian.mods.vidlib.feature.imgui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiColorEditFlags;
import imgui.type.ImBoolean;
import imgui.type.ImDouble;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;

public class WidgetDebugPanel extends AdminPanel {
	public final ImInt intData = new ImInt();
	public final int[] int2Data = new int[2];
	public final int[] int3Data = new int[3];
	public final int[] int4Data = new int[4];
	public final ImFloat floatData = new ImFloat();
	public final float[] float2Data = new float[2];
	public final float[] float3Data = new float[3];
	public final float[] float4Data = new float[4];
	public final ImVec2 vec2Data = new ImVec2();
	public final ImVec4 vec4Data = new ImVec4();
	public final ImDouble doubleData = new ImDouble();
	public final double[] double2Data = new double[2];
	public final double[] double3Data = new double[3];
	public final double[] double4Data = new double[4];
	public final ImString stringData = new ImString();
	public final ImBoolean booleanData = new ImBoolean();

	public WidgetDebugPanel() {
		super("widget-debug", "Widget Debug");
	}

	@Override
	public void content() {
		ImGui.pushItemWidth(-1F);

		ImGui.text("Button");
		ImGui.button("Button###button");
		ImGui.separator();

		ImGui.text("Small Button");
		ImGui.sameLine();
		ImGui.smallButton("S###small-button");
		ImGui.separator();

		ImGui.text("Checkbox");
		ImGui.checkbox("Checkbox###checkbox", booleanData);
		ImGui.separator();

		ImGui.text("Color 3");
		ImGui.colorEdit3("Color###color-3", float3Data, ImGuiColorEditFlags.NoInputs);
		ImGui.separator();

		ImGui.text("Color 4");
		ImGui.colorEdit4("Color###color-4", float4Data, ImGuiColorEditFlags.NoInputs | ImGuiColorEditFlags.AlphaPreview);
		ImGui.separator();

		ImGui.text("Int Slider 1");
		ImGui.sliderInt("###int-slider-1", intData.getData(), 0, 100);
		ImGui.separator();

		ImGui.text("Int Slider 2");
		ImGui.sliderInt2("###int-slider-2", int2Data, 0, 100);
		ImGui.separator();

		ImGui.text("Int Slider 3");
		ImGui.sliderInt3("###int-slider-3", int3Data, 0, 100);
		ImGui.separator();

		ImGui.text("Int Slider 4");
		ImGui.sliderInt4("###int-slider-4", int4Data, 0, 100);
		ImGui.separator();

		ImGui.text("Float Slider 1");
		ImGui.sliderFloat("###float-slider-1", floatData.getData(), 0F, 1F);
		ImGui.separator();

		ImGui.text("Float Slider 2");
		ImGui.sliderFloat2("###float-slider-2", float2Data, 0F, 1F);
		ImGui.separator();

		ImGui.text("Float Slider 3");
		ImGui.sliderFloat3("###float-slider-3", float3Data, 0F, 1F);
		ImGui.separator();

		ImGui.text("Float Slider 4");
		ImGui.sliderFloat4("###float-slider-4", float4Data, 0F, 1F);
		ImGui.separator();

		ImGui.text("Bar Plot");

		// if (ImPlot.beginPlot("Bar Plot###bar-plot")) {
		// 	ImPlot.endPlot();
		// }

		ImGui.separator();

		ImGui.popItemWidth();
	}
}
