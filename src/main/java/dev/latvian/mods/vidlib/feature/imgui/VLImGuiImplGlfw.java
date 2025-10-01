package dev.latvian.mods.vidlib.feature.imgui;

import imgui.ImGui;
import imgui.callback.ImStrConsumer;
import imgui.callback.ImStrSupplier;
import imgui.glfw.ImGuiImplGlfw;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class VLImGuiImplGlfw extends ImGuiImplGlfw {
	public final Minecraft mc;

	public VLImGuiImplGlfw(Minecraft mc) {
		this.mc = mc;
	}

	@Override
	public boolean init(long windowId, boolean installCallbacks) {
		var v = super.init(windowId, installCallbacks);
		var io = ImGui.getIO();

		io.setGetClipboardTextFn(new ImStrSupplier() {
			@Override
			public String get() {
				if (mc.getWindow().getWindow() == windowId) {
					return mc.keyboardHandler.getClipboard();
				}

				var clipboardString = GLFW.glfwGetClipboardString(windowId);
				return clipboardString != null ? clipboardString : "";
			}
		});

		io.setSetClipboardTextFn(new ImStrConsumer() {
			@Override
			public void accept(final String text) {
				if (mc.getWindow().getWindow() == windowId) {
					mc.keyboardHandler.setClipboard(text);
				} else {
					GLFW.glfwSetClipboardString(windowId, text);
				}
			}
		});

		return v;
	}
}
