package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.klib.math.KMath;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiHoveredFlags;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImDouble;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public interface ImGuiUtils {
	ImInt INT = new ImInt();
	ImInt INT1_2 = new ImInt();
	int[] INT2 = new int[2];
	int[] INT3 = new int[3];
	int[] INT4 = new int[4];
	ImFloat FLOAT = new ImFloat();
	ImFloat FLOAT1_2 = new ImFloat();
	float[] FLOAT2 = new float[2];
	float[] FLOAT3 = new float[3];
	float[] FLOAT4 = new float[4];
	ImVec2 VEC2 = new ImVec2();
	ImVec4 VEC4 = new ImVec4();
	ImDouble DOUBLE = new ImDouble();
	ImDouble DOUBLE1_2 = new ImDouble();
	double[] DOUBLE2 = new double[2];
	double[] DOUBLE3 = new double[3];
	double[] DOUBLE4 = new double[4];
	ImString STRING = resizableString();
	ImBoolean BOOLEAN = new ImBoolean();
	ImVec4 COLOR = new ImVec4();

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

	final class DragState {
		public static final Int2ObjectMap<DragState> STATES = new Int2ObjectOpenHashMap<>();

		int activeKnob = -1; // -1 none, 0 = min, 1 = max
		boolean wasActive = false;
	}

	static boolean rangeSliderFloat(String label, ImFloat v1, ImFloat v2, float vMin, float vMax, String displayFormat) {
		if (displayFormat == null || displayFormat.isEmpty()) {
			displayFormat = "%.3f - %.3f";
		}

		var drawList = ImGui.getWindowDrawList();

		float min = Math.min(v1.get(), v2.get());
		float max = Math.max(v1.get(), v2.get());
		min = Math.clamp(min, vMin, vMax);
		max = Math.clamp(max, vMin, vMax);

		// Layout similar to SliderFloat
		float w = ImGui.calcItemWidth();
		ImGui.pushID(label);
		ImVec2 p0 = ImGui.getCursorScreenPos();
		ImGuiStyle style = ImGui.getStyle();
		float frameH = ImGui.getFrameHeight();
		ImVec2 frameSize = new ImVec2(w, frameH);

		// We draw a slider-like frame using InvisibleButton for interaction
		ImGui.invisibleButton("range_slider_btn", frameSize.x, frameSize.y);
		boolean hovered = ImGui.isItemHovered();

		// Rect
		float x0 = p0.x;
		float y0 = p0.y;
		float x1 = p0.x + frameSize.x;
		float y1 = p0.y + frameSize.y;

		int colFrame = ImGui.getColorU32(ImGuiCol.FrameBg);
		int colBorder = ImGui.getColorU32(ImGuiCol.Border);
		int colGrab = ImGui.getColorU32(ImGuiCol.SliderGrabActive);
		int colGrabIdle = ImGui.getColorU32(ImGuiCol.SliderGrab);
		int colRange = ImGui.getColorU32(ImGuiCol.PlotHistogram);

		// Frame
		drawList.addRectFilled(x0, y0, x1, y1, colFrame, style.getFrameRounding());
		drawList.addRect(x0, y0, x1, y1, colBorder, style.getFrameRounding());

		// Compute knob positions in pixels
		float pxMin = x0 + (frameSize.x) * KMath.map(min, vMin, vMax, 0f, 1f);
		float pxMax = x0 + (frameSize.x) * KMath.map(max, vMin, vMax, 0f, 1f);
		float grabW = Math.max(6f, frameSize.y * 0.35f);

		// Selected range fill
		drawList.addRectFilled(pxMin, y0, pxMax, y1, colRange, style.getFrameRounding());

		// Interaction (drag nearest knob)
		int id = ImGui.getID("##rs_id");
		var st = DragState.STATES.computeIfAbsent(id, k -> new DragState());

		// On mouse click, pick nearest knob
		if (hovered && ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
			float mx = ImGui.getIO().getMousePosX();

			if (pxMin == pxMax) {
				st.activeKnob = mx < pxMin ? 0 : 1;
			} else {
				st.activeKnob = (Math.abs(mx - pxMin) <= Math.abs(mx - pxMax)) ? 0 : 1;
			}

			st.wasActive = true;
		}

		// Grabs
		drawGrab(drawList, pxMin, y0, y1, grabW, hovered || st.wasActive, colGrabIdle, colGrab);
		drawGrab(drawList, pxMax, y0, y1, grabW, hovered || st.wasActive, colGrabIdle, colGrab);

		// Drag
		if (st.activeKnob != -1 && ImGui.isMouseDown(ImGuiMouseButton.Left)) {
			float mx = ImGui.getIO().getMousePosX();
			float t = Math.clamp((mx - x0) / (x1 - x0), 0f, 1f);
			float val = KMath.lerp(t, vMin, vMax);
			if (st.activeKnob == 0) {
				min = Math.min(val, max);
			} else {
				max = Math.max(val, min);
			}
		}
		if (st.wasActive && ImGui.isMouseReleased(ImGuiMouseButton.Left)) {
			st.activeKnob = -1;
			st.wasActive = false;
		}

		// Value text (centered)
		String txt = String.format(java.util.Locale.ROOT, displayFormat, min, max);
		ImVec2 ts = ImGui.calcTextSize(txt);
		drawList.addText(
			x0 + (frameSize.x - ts.x) * 0.5f,
			y0 + (frameSize.y - ts.y) * 0.5f,
			ImGui.getColorU32(ImGuiCol.Text),
			txt
		);

		/*
		// Render label on the right (if any)
		if (label != null && !label.isEmpty()) {
			ImGui.sameLine();
			ImGui.setCursorScreenPos(x1 + style.getItemInnerSpacingX(), y0 + style.getFramePaddingY());
			ImGui.textUnformatted(label);
		}
		 */

		// Write back preserving original v1/v2 ordering
		boolean changed = (min != Math.min(v1.get(), v2.get())) || (max != Math.max(v1.get(), v2.get()));
		if (v1.get() <= v2.get()) {
			v1.set(min);
			v2.set(max);
		} else {
			v2.set(min);
			v1.set(max);
		}

		ImGui.popID();
		return changed;
	}

	static boolean rangeSliderInt(String label, ImInt v1, ImInt v2, int vMin, int vMax, String displayFormat) {
		if (displayFormat == null || displayFormat.isEmpty()) {
			displayFormat = "%d - %d";
		}

		var drawList = ImGui.getWindowDrawList();

		int min = Math.min(v1.get(), v2.get());
		int max = Math.max(v1.get(), v2.get());
		min = Math.clamp(min, vMin, vMax);
		max = Math.clamp(max, vMin, vMax);

		// Layout similar to SliderFloat
		float w = ImGui.calcItemWidth();
		ImGui.pushID(label);
		ImVec2 p0 = ImGui.getCursorScreenPos();
		ImGuiStyle style = ImGui.getStyle();
		float frameH = ImGui.getFrameHeight();
		ImVec2 frameSize = new ImVec2(w, frameH);

		// We draw a slider-like frame using InvisibleButton for interaction
		ImGui.invisibleButton("range_slider_btn", frameSize.x, frameSize.y);
		boolean hovered = ImGui.isItemHovered();

		// Rect
		float x0 = p0.x;
		float y0 = p0.y;
		float x1 = p0.x + frameSize.x;
		float y1 = p0.y + frameSize.y;

		int colFrame = ImGui.getColorU32(ImGuiCol.FrameBg);
		int colBorder = ImGui.getColorU32(ImGuiCol.Border);
		int colGrab = ImGui.getColorU32(ImGuiCol.SliderGrabActive);
		int colGrabIdle = ImGui.getColorU32(ImGuiCol.SliderGrab);
		int colRange = ImGui.getColorU32(ImGuiCol.PlotHistogram);

		// Frame
		drawList.addRectFilled(x0, y0, x1, y1, colFrame, style.getFrameRounding());
		drawList.addRect(x0, y0, x1, y1, colBorder, style.getFrameRounding());

		// Compute knob positions in pixels
		float pxMin = x0 + (frameSize.x) * KMath.map(min, vMin, vMax, 0f, 1f);
		float pxMax = x0 + (frameSize.x) * KMath.map(max, vMin, vMax, 0f, 1f);
		float grabW = Math.max(6f, frameSize.y * 0.35f);

		// Selected range fill
		drawList.addRectFilled(pxMin, y0, pxMax, y1, colRange, style.getFrameRounding());

		// Interaction (drag nearest knob)
		int id = ImGui.getID("##rs_id");
		var st = DragState.STATES.computeIfAbsent(id, k -> new DragState());

		// On mouse click, pick nearest knob
		if (hovered && ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
			float mx = ImGui.getIO().getMousePosX();

			if (pxMin == pxMax) {
				st.activeKnob = mx < pxMin ? 0 : 1;
			} else {
				st.activeKnob = (Math.abs(mx - pxMin) <= Math.abs(mx - pxMax)) ? 0 : 1;
			}

			st.wasActive = true;
		}

		// Grabs
		drawGrab(drawList, pxMin, y0, y1, grabW, hovered || st.wasActive, colGrabIdle, colGrab);
		drawGrab(drawList, pxMax, y0, y1, grabW, hovered || st.wasActive, colGrabIdle, colGrab);

		// Drag
		if (st.activeKnob != -1 && ImGui.isMouseDown(ImGuiMouseButton.Left)) {
			float mx = ImGui.getIO().getMousePosX();
			float t = Math.clamp((mx - x0) / (x1 - x0), 0f, 1f);
			float val = KMath.lerp(t, vMin, vMax);
			if (st.activeKnob == 0) {
				min = Mth.floor(Math.min(val, max));
			} else {
				max = Mth.ceil(Math.max(val, min));
			}
		}
		if (st.wasActive && ImGui.isMouseReleased(ImGuiMouseButton.Left)) {
			st.activeKnob = -1;
			st.wasActive = false;
		}

		// Value text (centered)
		String txt = String.format(java.util.Locale.ROOT, displayFormat, min, max);
		ImVec2 ts = ImGui.calcTextSize(txt);
		drawList.addText(
			x0 + (frameSize.x - ts.x) * 0.5f,
			y0 + (frameSize.y - ts.y) * 0.5f,
			ImGui.getColorU32(ImGuiCol.Text),
			txt
		);

		/*
		// Render label on the right (if any)
		if (label != null && !label.isEmpty()) {
			ImGui.sameLine();
			ImGui.setCursorScreenPos(x1 + style.getItemInnerSpacingX(), y0 + style.getFramePaddingY());
			ImGui.textUnformatted(label);
		}
		 */

		// Write back preserving original v1/v2 ordering
		boolean changed = (min != Math.min(v1.get(), v2.get())) || (max != Math.max(v1.get(), v2.get()));
		if (v1.get() <= v2.get()) {
			v1.set(min);
			v2.set(max);
		} else {
			v2.set(min);
			v1.set(max);
		}

		ImGui.popID();
		return changed;
	}

	private static void drawGrab(ImDrawList drawList, float cx, float y0, float y1, float w, boolean hovered, int colIdle, int colActive) {
		float x0 = cx - w * 0.5f;
		float x1 = cx + w * 0.5f;
		int col = hovered ? colActive : colIdle;
		drawList.addRectFilled(x0, y0, x1, y1, col, 2f);
		drawList.addRect(x0, y0, x1, y1, ImGui.getColorU32(ImGuiCol.Border), 2f);
	}

	static boolean beginFullScreenModal(String name, @Nullable ImBoolean open, int flags) {
		var viewport = ImGui.getMainViewport();
		float yOff = viewport.getWorkPosY() - viewport.getPosY();
		float popupWidth = viewport.getWorkSizeX() - yOff * 2F;
		float popupHeight = viewport.getWorkSizeY() - yOff;

		if (popupWidth <= 0F || popupHeight <= 0F) {
			return false;
		}

		ImGui.setNextWindowPos(viewport.getWorkPos().x + yOff, viewport.getWorkPosY());
		ImGui.setNextWindowSize(popupWidth, popupHeight);

		if (open != null) {
			return ImGui.beginPopupModal(name, open, flags | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoDocking);
		} else {
			return ImGui.beginPopupModal(name, flags | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoDocking);
		}
	}
}
