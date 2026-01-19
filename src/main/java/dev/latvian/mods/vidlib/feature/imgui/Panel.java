package dev.latvian.mods.vidlib.feature.imgui;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

public class Panel {
	public final String id;
	public String label;
	public boolean canBeClosed;
	public boolean ephemeral;
	public MenuItem menuBar;
	boolean isOpen;
	private ImWindowType windowType;
	public PanelStyle style;

	public Panel(String id, String label) {
		this.id = id;
		this.label = label;
		this.canBeClosed = true;
		this.ephemeral = false;
		this.menuBar = null;
		this.isOpen = false;
		this.windowType = ImWindowType.FLOATING;
		this.style = PanelStyle.NORMAL;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
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

		if (menuBar != null) {
			flags |= ImGuiWindowFlags.MenuBar;
		}

		return flags;
	}

	public void postSetup(ImGraphics graphics, boolean menuOpen) {
	}

	public final boolean handle(ImGraphics graphics) {
		int flags = setup(graphics);
		ImGuiUtils.BOOLEAN.set(true);

		if (style != PanelStyle.NORMAL && windowType != ImWindowType.DOCKED) {
			flags |= ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.AlwaysAutoResize;

			if (windowType == ImWindowType.ATTACHED && style == PanelStyle.GLASS) {
				flags |= ImGuiWindowFlags.NoBackground | ImGuiWindowFlags.NoDecoration;
			}

			ImGui.setNextWindowSizeConstraints(0F, 0F, 600F, graphics.mc.getWindow().getHeight() - 80F);
		} else {
			ImGui.setNextWindowSizeConstraints(160F, 90F, Float.MAX_VALUE, Float.MAX_VALUE);
		}

		var title = getLabel() + "###" + getId();
		boolean menuOpen = canBeClosed ? ImGui.begin(title, ImGuiUtils.BOOLEAN, flags) : ImGui.begin(title, flags);
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

		windowType = ImWindowType.get(graphics.mc.getWindow().getWindow());
		ImGui.end();

		return !isOpen;
	}

	public void content(ImGraphics graphics) {
	}

	public void postContent(ImGraphics graphics) {
	}

	public void tick() {
	}
}
