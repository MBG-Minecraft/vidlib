package dev.latvian.mods.vidlib.feature.imgui;

import imgui.ImGui;

public class JVMThreadsPanel extends Panel {
	public static final JVMThreadsPanel INSTANCE = new JVMThreadsPanel();

	private JVMThreadsPanel() {
		super("jvm-threads", "JVM Threads");
	}

	@Override
	public int setup(ImGraphics graphics) {
		ImGui.setNextWindowSizeConstraints(0F, 0F, Float.MAX_VALUE, 1000F);
		return super.setup(graphics);
	}

	@Override
	public void content(ImGraphics graphics) {
		for (var entry : Thread.getAllStackTraces().entrySet().stream().sorted((o1, o2) -> o1.getKey().getName().compareToIgnoreCase(o2.getKey().getName())).toList()) {
			var name = entry.getKey().getName();
			graphics.pushStack();

			if (entry.getKey().isDaemon()) {
				graphics.setText(ImColorVariant.BLUE);
			}

			name += " [" + entry.getKey().getState() + "]";

			if (graphics.collapsingHeader(name + "###thread-" + entry.getKey().threadId(), 0)) {
				graphics.popStack();
				ImGui.textWrapped(entry.getKey().getClass().getName());
				graphics.stackTrace("", entry.getValue());
			} else {
				graphics.popStack();
			}
		}
	}
}
