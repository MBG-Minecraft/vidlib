package dev.latvian.mods.vidlib.feature.imgui;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

public class AdminPanel {
	public final String id;
	public String label;
	public boolean canBeClosed;
	public boolean ephemeral;
	public boolean menuBar;
	boolean isOpen;

	public AdminPanel(String id, String label) {
		this.id = id;
		this.label = label;
		this.canBeClosed = true;
		this.ephemeral = false;
		this.menuBar = false;
		this.isOpen = false;
	}

	public final void open() {
		if (!isOpen) {
			isOpen = true;
			BuiltInImGui.OPEN_TABS.add(this);
			onOpened();
		}
	}

	public final void close() {
		if (isOpen && canBeClosed) {
			isOpen = false;
		}
	}

	public final boolean isOpen() {
		return isOpen;
	}

	public void onOpened() {
	}

	public void onClosed() {
	}

	public boolean isUnsaved() {
		return false;
	}

	public int setup(ImGraphics graphics) {
		int flags = ImGuiWindowFlags.NoCollapse;

		if (isUnsaved()) {
			flags |= ImGuiWindowFlags.UnsavedDocument;
		}

		if (ephemeral) {
			flags |= ImGuiWindowFlags.NoSavedSettings;
		}

		if (menuBar) {
			flags |= ImGuiWindowFlags.MenuBar;
		}

		return flags;
	}

	public final boolean handle(ImGraphics graphics) {
		int flags = setup(graphics);
		ImGuiUtils.BOOLEAN.set(true);

		if (canBeClosed ? ImGui.begin(label + "###" + id, ImGuiUtils.BOOLEAN, flags) : ImGui.begin(label + "###" + id, flags)) {
			boolean shouldClose = !ImGuiUtils.BOOLEAN.get();

			content(graphics);

			if (shouldClose) {
				close();
			}

			if (!isOpen) {
				onClosed();
			}
		}

		postContent(graphics);
		ImGui.end();
		return !isOpen;
	}

	public void content(ImGraphics graphics) {
	}

	public void postContent(ImGraphics graphics) {
	}
}
