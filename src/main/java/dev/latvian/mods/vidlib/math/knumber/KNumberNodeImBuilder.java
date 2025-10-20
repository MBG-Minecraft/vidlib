package dev.latvian.mods.vidlib.math.knumber;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.NodeEditorPanel;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.imgui.node.NodeEditorInstance;
import dev.latvian.mods.vidlib.feature.imgui.node.NodePinType;
import imgui.type.ImDouble;
import org.jetbrains.annotations.Nullable;

public class KNumberNodeImBuilder implements ImBuilder<KNumber> {
	public final NodeEditorInstance<KNumber> instance = new NodeEditorInstance<>(NodePinType.NUMBER);
	private final ImDouble plainNumber = new ImDouble(0D);

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;

		if (graphics.iconButton(ImIcons.SETTINGS, "###open-node-editor", "Advanced", null)) {
			NodeEditorPanel.open(instance);
		}

		return update;
	}

	@Override
	public void set(@Nullable KNumber value) {
		instance.rootBuilder.set(value);
	}

	@Override
	public boolean isValid() {
		return instance.rootBuilder.isValid();
	}

	@Override
	public KNumber build() {
		return instance.rootBuilder.build();
	}
}
