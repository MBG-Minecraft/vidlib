package dev.latvian.mods.vidlib.feature.imgui;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

public class AdminPanel {
	public final String id;
	public String label;
	public boolean canBeClosed;
	public boolean ephemeral;
	boolean isOpen;

	public AdminPanel(String id, String label) {
		this.id = id;
		this.label = label;
		this.canBeClosed = true;
		this.ephemeral = false;
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

	public final boolean handle() {
		int flags = ImGuiWindowFlags.NoCollapse;

		if (isUnsaved()) {
			flags |= ImGuiWindowFlags.UnsavedDocument;
		}

		if (ephemeral) {
			flags |= ImGuiWindowFlags.NoSavedSettings;
		}

		ImGuiUtils.BOOLEAN.set(true);

		if (canBeClosed ? ImGui.begin(label + "###" + id, ImGuiUtils.BOOLEAN, flags) : ImGui.begin(label + "###" + id, flags)) {
			content();

			if (!ImGuiUtils.BOOLEAN.get()) {
				close();
			}

			if (!isOpen) {
				onClosed();
			}
		}

		ImGui.end();
		return !isOpen;
	}

	public void content() {
	}
}
