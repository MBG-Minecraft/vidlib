package dev.latvian.mods.vidlib.feature.imgui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiHoveredFlags;
import imgui.type.ImBoolean;
import imgui.type.ImDouble;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.resources.ResourceLocation;

public interface ImGuiUtils {
	ImInt INT = new ImInt();
	int[] INT2 = new int[2];
	int[] INT3 = new int[3];
	int[] INT4 = new int[4];
	ImFloat FLOAT = new ImFloat();
	float[] FLOAT2 = new float[2];
	float[] FLOAT3 = new float[3];
	float[] FLOAT4 = new float[4];
	ImVec2 VEC2 = new ImVec2();
	ImVec4 VEC4 = new ImVec4();
	ImDouble DOUBLE = new ImDouble();
	double[] DOUBLE2 = new double[2];
	double[] DOUBLE3 = new double[3];
	double[] DOUBLE4 = new double[4];
	ImString STRING = resizableString();
	ImBoolean BOOLEAN = new ImBoolean();

	static ImString resizableString(String initial) {
		var s = new ImString(initial);
		s.inputData.isResizable = true;
		return s;
	}

	static ImString resizableString() {
		return resizableString("");
	}

	static String id(ResourceLocation id) {
		return id.getNamespace() + "-" + id.getPath().replace('/', '-');
	}

	static float getDpiScale() {
		return ImGuiHooks.dpiScale;
	}

	static int getDockId() {
		return ImGuiHooks.dockId;
	}

	static float calcTextWidth(String text) {
		ImVec2 textSizeVec = new ImVec2();
		ImGui.calcTextSize(textSizeVec, text);
		return textSizeVec.x;
	}

	static void separatorWithText(String text) {
		float cursorX = ImGui.getCursorScreenPosX();
		float cursorY = ImGui.getCursorScreenPosY();
		float textStartX = cursorX + ImGui.getStyle().getIndentSpacing();
		float size = ImGui.getWindowSizeX();
		int fontSize = ImGui.getFontSize();

		if (ImGui.isRectVisible(size, fontSize)) {
			float textEndX = textStartX + calcTextWidth(text);
			float lineEndX = ImGui.getWindowPosX() + size;
			float lineY = cursorY + fontSize / 2F;
			var drawList = ImGui.getWindowDrawList();
			var sepColor = ImGui.getColorU32(ImGuiCol.Separator);

			drawList.addLine(cursorX - 4, lineY, Math.min(lineEndX, textStartX) - 4, lineY, sepColor);

			if (textEndX + 4 < lineEndX) {
				drawList.addLine(textEndX + 4, lineY, lineEndX - 4, lineY, sepColor);
			}
		}

		ImGui.setCursorScreenPos(textStartX, cursorY);
		ImGui.textColored(ImGui.getColorU32(ImGuiCol.TextDisabled), text);
	}

	static void hoveredTooltip(String tooltip) {
		if (!tooltip.isEmpty() && ImGui.isItemHovered(ImGuiHoveredFlags.AllowWhenDisabled)) {
			ImGui.setTooltip(tooltip);
		}
	}
}
