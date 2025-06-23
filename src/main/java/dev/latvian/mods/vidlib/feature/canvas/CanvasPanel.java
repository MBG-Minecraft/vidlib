package dev.latvian.mods.vidlib.feature.canvas;

import dev.latvian.mods.vidlib.feature.imgui.AdminPanel;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import imgui.ImGui;

public class CanvasPanel extends AdminPanel {
	public static void menu() {
		// CutsceneEditorPanel.INSTANCE.open();

		for (var canvas : CanvasImpl.ENABLED) {
			if (ImGui.menuItem(canvas.idString)) {
				new CanvasPanel(canvas, true).open();
				new CanvasPanel(canvas, false).open();
			}
		}
	}

	public final Canvas canvas;
	public final boolean color;

	public CanvasPanel(Canvas canvas, boolean color) {
		super("canvas-" + ImGuiUtils.id(canvas.id) + (color ? "-color" : "-depth"), canvas.idString + (color ? " Canvas (Color)" : " Canvas (Depth)"));
		this.canvas = canvas;
		this.color = color;
	}

	@Override
	public int setup(ImGraphics graphics) {
		return super.setup(graphics);
	}

	@Override
	public void content(ImGraphics graphics) {
		try {
			var tex = color ? canvas.getColorTexture() : canvas.getDepthTexture();

			if (tex != null) {
				float texw = tex.getWidth(0);
				float texh = tex.getHeight(0);

				float max = ImGui.getContentRegionAvailX() - 20F;
				ImGui.image(tex.vl$getHandle(), max, max * texh / texw);
			} else {
				ImGui.text("No texture available");
			}
		} catch (Exception ex) {
			graphics.stackTrace(ex);
		}
	}
}
