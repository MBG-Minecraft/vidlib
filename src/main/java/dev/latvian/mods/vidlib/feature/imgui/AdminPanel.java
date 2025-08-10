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
	private ImWindowType windowType;
	public AdminPanelStyle style;

	public AdminPanel(String id, String label) {
		this.id = id;
		this.label = label;
		this.canBeClosed = true;
		this.ephemeral = false;
		this.menuBar = false;
		this.isOpen = false;
		this.windowType = ImWindowType.FLOATING;
		this.style = AdminPanelStyle.NORMAL;
	}

	public final void open() {
		if (!isOpen) {
			isOpen = true;

			for (var p : BuiltInImGui.OPEN_PANELS.values()) {
				if (p != this && p.id.equals(id)) {
					p.close();
				}
			}

			BuiltInImGui.OPEN_PANELS.put(id, this);
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

	public final ImWindowType getWindowType() {
		return windowType;
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

	public void postSetup(ImGraphics graphics, boolean menuOpen) {
	}

	public final boolean handle(ImGraphics graphics) {
		int flags = setup(graphics);
		ImGuiUtils.BOOLEAN.set(true);

		if (style != AdminPanelStyle.NORMAL && windowType != ImWindowType.DOCKED) {
			flags |= ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.AlwaysAutoResize;

			if (windowType == ImWindowType.ATTACHED && style == AdminPanelStyle.GLASS) {
				flags |= ImGuiWindowFlags.NoBackground | ImGuiWindowFlags.NoDecoration;
			}

			ImGui.setNextWindowSizeConstraints(0F, 0F, 600F, graphics.mc.getWindow().getHeight() - 80F);
		}

		boolean menuOpen = canBeClosed ? ImGui.begin(label + "###" + id, ImGuiUtils.BOOLEAN, flags) : ImGui.begin(label + "###" + id, flags);
		postSetup(graphics, menuOpen);

		if (menuOpen) {
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

		// check window type - floating, docked, or attached

		if (ImGui.getWindowViewport() == null) {
			windowType = ImWindowType.FLOATING;
		} else if (ImGui.getWindowViewport().getPlatformHandle() != graphics.mc.getWindow().getWindow()) {
			windowType = ImWindowType.FLOATING;
		} else if (ImGui.isWindowDocked()) {
			windowType = ImWindowType.DOCKED;
		} else {
			windowType = ImWindowType.ATTACHED;
		}

		ImGui.end();

		return !isOpen;
	}

	public void content(ImGraphics graphics) {
	}

	public void postContent(ImGraphics graphics) {
	}
}
