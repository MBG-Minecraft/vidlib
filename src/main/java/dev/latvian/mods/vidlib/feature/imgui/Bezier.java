package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.klib.interpolation.BezierPreset;
import dev.latvian.mods.klib.math.KMath;
import imgui.ImColor;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiDir;
import imgui.flag.ImGuiHoveredFlags;
import imgui.flag.ImGuiMouseButton;
import org.joml.Vector2f;

public final class Bezier {
	private Bezier() {
	}

	// Visuals (match C++)
	private static final float GRAB_RADIUS = 8f;      // handle radius
	private static final float[] TEMP_4 = new float[4];
	private static final Vector2f TEMP_P = new Vector2f();

	// ---------------- Public API ----------------

	public static float bezierValue(float t, float[] P) {
		float a = 3f * P[1] - 3f * P[3] + 1f;
		float b = -6f * P[1] + 3f * P[3];
		float c = 3f * P[1];
		return ((a * t + b) * t + c) * t;
	}

	public static boolean draw(String label, Vector2f p1, Vector2f p2, BezierPreset[] preset, float size, int smoothness) {
		boolean changed = false;
		ImGui.pushID(label);

		// --- Preset selector (left arrow, popup, right arrow) ---
		if (ImGui.arrowButton("##lt", ImGuiDir.Left) && (preset[0] == null || preset[0].ordinal() > 0)) {
			preset[0] = preset[0] == null ? BezierPreset.LINEAR : BezierPreset.VALUES[preset[0].ordinal() - 1];
			preset[0].set(p1, p2);
			changed = true;
		}
		ImGui.sameLine();
		if (ImGui.button(preset[0] == null ? "Custom" : preset[0].name)) {
			ImGui.openPopup("!Presets");
		}
		if (ImGui.beginPopup("!Presets")) {
			for (int i = 0; i < BezierPreset.VALUES.length; ++i) {
				if (i == 1 || i == 9 || i == 17) {
					ImGui.separator();
				}

				var itemPreset = BezierPreset.VALUES[i];

				if (ImGui.menuItem(itemPreset.name, "", preset[0] == itemPreset)) {
					preset[0] = itemPreset;
					preset[0].set(p1, p2);
					changed = true;
				}
			}
			ImGui.endPopup();
		}
		ImGui.sameLine();
		if (ImGui.arrowButton("##rt", ImGuiDir.Right) && (preset[0] == null || preset[0].ordinal() < BezierPreset.VALUES.length - 1)) {
			preset[0] = preset[0] == null ? BezierPreset.LINEAR : BezierPreset.VALUES[preset[0].ordinal() + 1];
			preset[0].set(p1, p2);
			changed = true;
		}
		ImGui.popID();

		// --- Header slider (like original) ---
		TEMP_4[0] = p1.x;
		TEMP_4[1] = p1.y;
		TEMP_4[2] = p2.x;
		TEMP_4[3] = p2.y;
		changed |= ImGui.sliderFloat4(label, TEMP_4, 0f, 1f, "%.3f");
		p1.x = TEMP_4[0];
		p1.y = TEMP_4[1];
		p2.x = TEMP_4[2];
		p2.y = TEMP_4[3];
		boolean hoveredHeader = ImGui.isItemHovered();
		ImGui.dummy(0, 3);

		// --- Canvas size & rect ---
		float avail = ImGui.getContentRegionAvailX();
		float dim = (size > 0F ? size : avail);
		ImVec2 canvas = new ImVec2(dim, dim);

		ImVec2 bbMin = ImGui.getCursorScreenPos();
		ImVec2 bbMax = new ImVec2(bbMin.x + canvas.x, bbMin.y + canvas.y);

		// Use an invisible button to own hover/active state of the canvas
		ImGui.invisibleButton("##bezier-canvas", canvas.x, canvas.y);
		boolean hoveredCanvas = ImGui.isItemHovered(ImGuiHoveredFlags.AllowWhenBlockedByActiveItem);

		var dl = ImGui.getWindowDrawList();
		int frameCol = ImGui.getColorU32(ImGuiCol.FrameBg, 1f);
		float rounding = ImGui.getStyle().getFrameRounding();
		dl.addRectFilled(bbMin.x, bbMin.y, bbMax.x, bbMax.y, frameCol, rounding);

		// Grid
		int gridCol = ImGui.getColorU32(ImGuiCol.TextDisabled, 1f);
		for (int i = 0; i <= 4; ++i) {
			float gx = bbMin.x + i * canvas.x / 4f;
			float gy = bbMin.y + i * canvas.y / 4f;
			dl.addLine(gx, bbMin.y, gx, bbMax.y, gridCol, 1f);
			dl.addLine(bbMin.x, gy, bbMax.x, gy, gridCol, 1f);
		}

		// Curve
		int curveCol = ImGui.getColorU32(ImGuiCol.PlotLines);
		for (int i = 0; i < smoothness; ++i) {
			KMath.bezier(i / (float) smoothness, p1.x, p1.y, p2.x, p2.y, TEMP_P);
			float x1 = KMath.lerp(TEMP_P.x, bbMin.x, bbMax.x);
			float y1 = KMath.lerp(TEMP_P.y, bbMax.y, bbMin.y);
			KMath.bezier((i + 1F) / (float) smoothness, p1.x, p1.y, p2.x, p2.y, TEMP_P);
			float x2 = KMath.lerp(TEMP_P.x, bbMin.x, bbMax.x);
			float y2 = KMath.lerp(TEMP_P.y, bbMax.y, bbMin.y);
			dl.addLine(x1, y1, x2, y2, curveCol, 4F);
		}

		// --- Grabbing (pick closest of the two handles) ---
		float p1x = KMath.lerp(p1.x, bbMin.x, bbMax.x);
		float p1y = KMath.lerp(p1.y, bbMax.y, bbMin.y);
		float p2x = KMath.lerp(p2.x, bbMin.x, bbMax.x);
		float p2y = KMath.lerp(p2.y, bbMax.y, bbMin.y);

		float mx = ImGui.getIO().getMousePosX();
		float my = ImGui.getIO().getMousePosY();
		float d1 = KMath.sq(p1x - mx) + KMath.sq(p1y - my);
		float d2 = KMath.sq(p2x - mx) + KMath.sq(p2y - my);
		int selected = (d1 < d2) ? 0 : 1;
		float pickR2 = KMath.sq(4f * GRAB_RADIUS);

		if (hoveredCanvas && (selected == 0 ? d1 : d2) < pickR2) {
			var p = selected == 0 ? p1 : p2;

			ImGui.setTooltip("(%.3f, %.3f)".formatted(p.x, p.y));
			if (ImGui.isMouseClicked(ImGuiMouseButton.Left) || ImGui.isMouseDragging(ImGuiMouseButton.Left)) {
				float dx = ImGui.getIO().getMouseDeltaX() / canvas.x;
				float dy = ImGui.getIO().getMouseDeltaY() / canvas.y;
				p.x += dx;
				p.y -= dy;
				changed = true;
				preset[0] = null;
			}
		}

		// --- Handle lines + grabbers ---
		int white = ImGui.getColorU32(ImGuiCol.Text, 1f);
		dl.addLine(bbMin.x, bbMax.y, p1x, p1y, white, 1F);
		dl.addLine(bbMax.x, bbMin.y, p2x, p2y, white, 1F);

		int pink = ImColor.floatToColor(1.00f, 0.00f, 0.75f, (hoveredHeader || hoveredCanvas) ? 0.5f : 1.0f);
		int cyan = ImColor.floatToColor(0.00f, 0.75f, 1.00f, (hoveredHeader || hoveredCanvas) ? 0.5f : 1.0f);

		drawGrabber(dl, p1x, p1y, white, pink);
		drawGrabber(dl, p2x, p2y, white, cyan);

		return changed;
	}

	private static void drawGrabber(imgui.ImDrawList dl, float x, float y, int outer, int inner) {
		dl.addCircleFilled(x, y, GRAB_RADIUS, outer, 24);
		dl.addCircleFilled(x, y, GRAB_RADIUS - 2F, inner, 24);
	}
}
