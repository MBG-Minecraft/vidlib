package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.feature.imgui.node.NodeEditorInstance;

public class NodeEditorPanel extends AdminPanel {
	public static final NodeEditorPanel INSTANCE = new NodeEditorPanel();

	public static void open(NodeEditorInstance<?> instance) {
		INSTANCE.instance = instance;
		INSTANCE.open();
	}

	public NodeEditorInstance<?> instance;

	private NodeEditorPanel() {
		super("node-editor", "Node Editor");
	}

	@Override
	public void content(ImGraphics graphics) {
		if (instance == null) {
			close();
			return;
		}

		label = instance.type.displayName;
		instance.imgui(graphics);
	}

	@Override
	public void onClosed() {
		instance = null;
	}
}
