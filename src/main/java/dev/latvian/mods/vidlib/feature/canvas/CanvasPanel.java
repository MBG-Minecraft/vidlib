package dev.latvian.mods.vidlib.feature.canvas;

import dev.latvian.mods.vidlib.feature.imgui.AdminPanel;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImText;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;

import java.util.ArrayList;
import java.util.List;

public class CanvasPanel extends AdminPanel {
	public static List<MenuItem> menu(ImGraphics graphics) {
		// CutsceneEditorPanel.INSTANCE.open();
		var list = new ArrayList<MenuItem>(CanvasImpl.ENABLED.size());

		for (var canvas : CanvasImpl.ENABLED) {
			list.add(MenuItem.item(canvas.idString, g -> {
				new CanvasPanel(canvas, true).open();
				new CanvasPanel(canvas, false).open();
			}).withLabel(ImText.of(canvas.idString, canvas.active ? ImText.SUCCESS : ImText.ERROR)));
		}

		return list;
	}

	public final Canvas canvas;
	public final boolean color;

	public CanvasPanel(Canvas canvas, boolean color) {
		super("canvas-" + ImGuiUtils.id(canvas.id) + (color ? "-color" : "-depth"), canvas.idString + (color ? " (Color)" : " (Depth)"));
		this.canvas = canvas;
		this.color = color;
	}

	@Override
	public int setup(ImGraphics graphics) {
		graphics.pushStack();

		if (canvas.active) {
			label = ImIcons.VISIBLE + " " + canvas.idString + (color ? " (Color)" : " (Depth)");
			graphics.setSuccessText();
		} else {
			label = ImIcons.INVISIBLE + " " + canvas.idString + (color ? " (Color)" : " (Depth)");
			graphics.setErrorText();
		}

		return super.setup(graphics);
	}

	@Override
	public void postSetup(ImGraphics graphics, boolean menuOpen) {
		graphics.popStack();
	}

	@Override
	public void content(ImGraphics graphics) {
		try {
			var tex = color ? canvas.getColorTexture() : canvas.getDepthTexture();

			if (tex != null) {
				float texw = tex.getWidth(0);
				float texh = tex.getHeight(0);

				float max = ImGui.getContentRegionAvailX() - 20F;
				ImGui.image(tex.vl$getHandle(), max, max * texh / texw, 0F, 1F, 1F, 0F);
			} else {
				ImGui.text("No texture available");
			}
		} catch (Exception ex) {
			graphics.stackTrace(ex);
		}
	}
}
