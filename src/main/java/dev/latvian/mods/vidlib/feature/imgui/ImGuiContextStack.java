package dev.latvian.mods.vidlib.feature.imgui;

import imgui.ImGui;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.ImPlotContext;
import imgui.internal.ImGuiContext;

public record ImGuiContextStack(ImGuiContext imGuiContext, ImPlotContext imPlotContext) {
	public ImGuiContextStack push() {
		var prevImGuiContext = new ImGuiContext(ImGui.getCurrentContext().ptr);
		var prevImPlotContext = new ImPlotContext(ImPlot.getCurrentContext().ptr);
		ImGui.setCurrentContext(imGuiContext);
		ImPlot.setCurrentContext(imPlotContext);
		return new ImGuiContextStack(prevImGuiContext, prevImPlotContext);
	}

	public void pop() {
		ImGui.setCurrentContext(imGuiContext);
		ImPlot.setCurrentContext(imPlotContext);
	}

	public void destroy() {
		ImPlot.destroyContext(imPlotContext);
		ImGui.destroyContext(imGuiContext);
	}
}
