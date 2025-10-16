package dev.latvian.mods.vidlib.math.knumber;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.imgui.node.NodeEditorInstance;
import dev.latvian.mods.vidlib.feature.imgui.node.NodePinType;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImDouble;

public class KNumberNodeImBuilder implements ImBuilder<KNumber> {
	public final NodeEditorInstance<KNumber> instance = new NodeEditorInstance<>(NodePinType.NUMBER);
	private final ImDouble plainNumber = new ImDouble(0D);

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;

		if (graphics.iconButton(ImIcons.SETTINGS, "###open-node-editor", "Edit", null)) {
			ImGui.openPopup("###node-editor-modal");
		}

		if (ImGuiUtils.beginFullScreenModal("Number Editor###node-editor-modal", new ImBoolean(true), ImGuiWindowFlags.NoSavedSettings)) {
			update = update.or(instance.imgui(graphics));
			ImGui.endPopup();
		}

		return update;
	}

	@Override
	public boolean isValid() {
		return false;
	}

	@Override
	public KNumber build() {
		return null;
	}
}
